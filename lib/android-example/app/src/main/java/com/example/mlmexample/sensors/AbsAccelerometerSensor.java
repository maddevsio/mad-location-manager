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
import java.util.concurrent.atomic.AtomicBoolean;

public class AbsAccelerometerSensor extends ISensor implements SensorEventListener {

    private static final String TAG = "AccelerationLogger";
    private final List<Sensor> m_lst_sensors = new ArrayList<>();
    private final SensorManager m_sensor_manager;
    private final WindowManager m_window_manager;
    private final AtomicBoolean m_got_rotation_vector = new AtomicBoolean(false);

    private static final int[] sensor_types = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR
    };

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
        m_got_rotation_vector.compareAndSet(true, false);
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

    protected final float[] R = new float[16];  // rotation matrix from rotation_vector
    protected final float[] RM = new float[16];  // rotation matrix adjusted to display orientation
    protected final float[] RI = new float[16]; // inverted adjusted rotation matrix
    protected final float[] acc_enu = new float[4]; // world coordinates (east/north/up)
    protected final float[] lin_acc = {0.f, 0.f, 0.f, 1.f};

    public float[] ENU() {
        return acc_enu.clone();
    }

    protected void onENUReceived(double ts, float east, float north, float up) {
        float x = east;
        float y = north;
        float z = up;
        String fmt = "1 %f:::%f %f %f";
        String msg = String.format(java.util.Locale.US, fmt, ts, x, y, z);
        System.out.println(msg);
    }

    private void adjustForDisplayRotation(float[] originalMatrix, float[] outputMatrix) {
        int rotation = m_window_manager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: // Portrait
                System.arraycopy(originalMatrix, 0, outputMatrix, 0, originalMatrix.length);
                break;
            case Surface.ROTATION_90: // Landscape (Left)
                SensorManager.remapCoordinateSystem(originalMatrix,
                        SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outputMatrix);
                break;
            case Surface.ROTATION_180: // Portrait (Upside Down)
                SensorManager.remapCoordinateSystem(originalMatrix,
                        SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, outputMatrix);
                break;
            case Surface.ROTATION_270: // Landscape (Right)
                SensorManager.remapCoordinateSystem(originalMatrix,
                        SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, outputMatrix);
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double ts = android.os.SystemClock.elapsedRealtime() / 1000.;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (!m_got_rotation_vector.get()) {
                    break;
                }
                System.arraycopy(event.values, 0, lin_acc, 0, 3);
                lin_acc[3] = 1.f;
                android.opengl.Matrix.multiplyMV(acc_enu, 0, RI, 0, lin_acc, 0);
                onENUReceived(ts, acc_enu[0], acc_enu[1], acc_enu[2]);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                m_got_rotation_vector.compareAndSet(false, true);
                SensorManager.getRotationMatrixFromVector(R, event.values);
                adjustForDisplayRotation(R, RM);
                // invert matrix to get ENU
                android.opengl.Matrix.invertM(RI, 0, RM, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}