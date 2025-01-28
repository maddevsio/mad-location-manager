package com.example.mlmexample.loggers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.List;


public class AbsAccelerometerLogger implements SensorEventListener, IDataLogger {

    private static final String TAG = "AccelerationLogger";
    private final List<Sensor> m_lst_sensors = new ArrayList<>();
    private final SensorManager m_sensor_manager;
    private final WindowManager m_window_manager;

    private static final int[] sensor_types = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };

    public AbsAccelerometerLogger(SensorManager sensor_manager, WindowManager window_manager) {
        m_sensor_manager = sensor_manager;
        m_window_manager = window_manager;
        for (Integer st : sensor_types) {
            Sensor sensor = m_sensor_manager.getDefaultSensor(st);
            if (sensor == null) {
                Log.e(TAG, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lst_sensors.add(sensor);
        }
    }

    public boolean start() {
        for (Sensor sensor : m_lst_sensors) {
            if (!m_sensor_manager.registerListener(this, sensor, 200)) {
                Log.e(TAG, String.format("Couldn't registerListener %d", sensor.getType()));
                return false;
            }
        }
        return true;
    }

    public boolean stop() {
        for (Sensor sensor : m_lst_sensors) {
            m_sensor_manager.unregisterListener(this, sensor);
        }
        return true;
    }

    // Adjust rotation matrix for display orientation
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

    private final float[] R = new float[16];  // rotation matrix from rotation_vector
    private final float[] RA = new float[16]; // R adjusted to display orientation matrix
    private final float[] RI = new float[16]; // inverted adjusted rotation matrix
    private final float[] acc_world = new float[4]; // NED axis
    private final float[] acc_device = new float[4]; // device axis

    private final float[] m_acc_world_tmp = new float[4];
    public float[] AccWorld() {
        System.arraycopy(acc_world, 0, m_acc_world_tmp, 0, 4);
        return m_acc_world_tmp;
    }

    private final float[] m_acc_raw_tmp = new float[4];
    public float[] AccRaw() {
        System.arraycopy(acc_device, 0, m_acc_raw_tmp, 0, 4);
        return m_acc_raw_tmp;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, acc_device, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(acc_world, 0, RI, 0, acc_device, 0);

                double ts = android.os.SystemClock.elapsedRealtime() / 1000.;
                // north = acc_world[0] --> Y
                // east = acc_world[1] --> X
                // down = acc_world[2] --> Z
                // however lib waits tuple (x, y, z)
                String msg = String.format(java.util.Locale.US, "1 %f:::%f %f %f", ts, acc_world[1], acc_world[0], acc_world[2]);
                XLog.i(msg);

                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(R, event.values);
                adjustForDisplayRotation(R, RA);
                android.opengl.Matrix.invertM(RI, 0, RA, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}
