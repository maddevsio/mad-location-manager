package com.example.mlmexample.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.WindowManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AbsAccelerometerSensor extends ISensor implements SensorEventListener {

    private static final String TAG = "AccelerationLogger";
    private final List<Sensor> m_lst_sensors = new ArrayList<>();
    private final SensorManager m_sensor_manager;
    private final WindowManager m_window_manager;
    private static final int[] sensor_types = {Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_ROTATION_VECTOR};

    public AbsAccelerometerSensor(SensorManager sensor_manager, WindowManager mWindowManager) {
        m_sensor_manager = sensor_manager;
        m_window_manager = mWindowManager;
        for (Integer st : sensor_types) {
            Sensor sensor = m_sensor_manager.getDefaultSensor(st);
            if (sensor == null) {
                Log.e(TAG, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lst_sensors.add(sensor);
        }
    }

    @Override
    protected boolean onStart() {
        for (Sensor sensor : m_lst_sensors) {
            if (!m_sensor_manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)) {
                Log.e(TAG, String.format("Couldn't registerListener %d", sensor.getType()));
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean onStop() {
        for (Sensor sensor : m_lst_sensors) {
            m_sensor_manager.unregisterListener(this, sensor);
        }
        return true;
    }

    protected final float[] qDev2World = new float[4];
    protected final float[] R = new float[9];  // rotation matrix from rotation_vector
    protected final float[] RM = new float[9]; // rotation matrix adjusted to display orientation
    protected final float[] RI = new float[9]; // inverted rotation matrix
    protected final float[] enu_acc = {0.f, 0.f, 0.f}; // world coordinates (east/north/up)
    protected final float[] lin_acc = {0.f, 0.f, 0.f};

    // For sensor timing synchronization
    private long m_last_rotation_ts = 0;
    private volatile boolean m_is_rotation_valid = false;
    private static final long MAX_SENSOR_TIMESTAMP_DELTA_NS = 5 * 1000_000; // 5ms in nanoseconds

    public float[] ENU() {
        return enu_acc.clone();
    }

    protected void onENUReceived(double ts, float east, float north, float up) {
        float x = east;
        float y = north;
        float z = up;
        String fmt = "1 %f:::%f %f %f";
        String msg = String.format(java.util.Locale.US, fmt, ts, x, y, z);
        System.out.println(msg);
    }

    // --- tiny helpers (works faster than opengl)
    private static void transpose3x3(float[] m, float[] out) {
        out[0] = m[0];  out[1] = m[3];  out[2] = m[6];
        out[3] = m[1];  out[4] = m[4];  out[5] = m[7];
        out[6] = m[2];  out[7] = m[5];  out[8] = m[8];
    }

    private static void multiply3x3Vec(float[] m, float[] v, float[] out) {
        out[0] = m[0]*v[0] + m[1]*v[1] + m[2]*v[2];
        out[1] = m[3]*v[0] + m[4]*v[1] + m[5]*v[2];
        out[2] = m[6]*v[0] + m[7]*v[1] + m[8]*v[2];
    }

    private static void quatRotateVec(float[] q, float[] v, float[] out) {
        // q = (w, U); qV = (0, v)
        // out = qvq(-1)
        // qv=(−u⋅v,wv+u×v)  - see quaternion multipllication formula
        // q(−1)=(w,−u)
        final float w = q[0], x = q[1], y = q[2], z = q[3];

        // t = 2 * cross(q.xyz, v)
        final float t0 = 2f * (y * v[2] - z * v[1]);
        final float t1 = 2f * (z * v[0] - x * v[2]);
        final float t2 = 2f * (x * v[1] - y * v[0]);

        // out = v + w * t + cross(q.xyz, t)
        out[0] = v[0] + w * t0 + (y * t2 - z * t1);
        out[1] = v[1] + w * t1 + (z * t0 - x * t2);
        out[2] = v[2] + w * t2 + (x * t1 - y * t0);
    }

    private void handleLinearAcceleration(SensorEvent event) {
        if (!m_is_rotation_valid) {
            return; // ignore this
        }

        if (Math.abs(event.timestamp - m_last_rotation_ts) > MAX_SENSOR_TIMESTAMP_DELTA_NS) {
            return; // Skip this acceleration update as rotation might be outdated
        }

        double ts = android.os.SystemClock.elapsedRealtime() / 1000.;
        System.arraycopy(event.values, 0, lin_acc, 0, 3);
        // NEW VERSION
        quatRotateVec(qDev2World, lin_acc, enu_acc);
        // OLD VERSION
        // multiply3x3Vec(RI, lin_acc, enu_acc);
        onENUReceived(ts, enu_acc[0], enu_acc[1], enu_acc[2]);
    }

    private void handleRotationVector(SensorEvent event) {
        long ts = event.timestamp;
        // NEW VERSION
        SensorManager.getQuaternionFromVector(qDev2World, event.values);

        // OLD VERSION
        // SensorManager.getRotationMatrixFromVector(R, event.values);
        // invert matrix to get ENU
        // for rotation (orthogonal) matrix invert = transpose
        // transpose3x3(R, RI);

        // Update timestamp and validity
        m_last_rotation_ts = ts;
        m_is_rotation_valid = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                handleLinearAcceleration(event);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                handleRotationVector(event);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    private void adjustForDisplayRotation(float[] originalMatrix, float[] outputMatrix) {
        int rotation = m_window_manager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: // Portrait
                System.arraycopy(originalMatrix, 0, outputMatrix, 0, originalMatrix.length);
                break;
            case Surface.ROTATION_90: // Landscape (Left)
                SensorManager.remapCoordinateSystem(originalMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outputMatrix);
                break;
            case Surface.ROTATION_180: // Portrait (Upside Down)
                SensorManager.remapCoordinateSystem(originalMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, outputMatrix);
                break;
            case Surface.ROTATION_270: // Landscape (Right)
                SensorManager.remapCoordinateSystem(originalMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, outputMatrix);
                break;
        }
    }
}