package com.example.mlmexample.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AbsAccelerometerSensor implements SensorEventListener, ISensor {

    private static final String TAG = "AccelerationLogger";
    private final List<Sensor> m_lst_sensors = new ArrayList<>();
    private final SensorManager m_sensor_manager;
    private boolean m_got_rotation_vector = false;

    private static final int[] sensor_types = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
//            Sensor.TYPE_ACCELEROMETER,
    };

    public AbsAccelerometerSensor(SensorManager sensor_manager) {
        m_sensor_manager = sensor_manager;
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
        m_got_rotation_vector = false;
        for (Sensor sensor : m_lst_sensors) {
            if (!m_sensor_manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)) {
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

    protected final float[] R = new float[16];  // rotation matrix from rotation_vector
    protected final float[] RI = new float[16]; // inverted adjusted rotation matrix
    protected final float[] acc_world = new float[4]; // NED axis
    protected final float[] lin_acc = {0.f, 0.f, 0.f, 1.f};

    public void onNEDReceived(double ts, float north, float east, float down) {
        // north = acc_world[0] --> Y
        // east = acc_world[1] --> X
        // down = acc_world[2] --> Z
        // however lib waits tuple (x, y, z)
        String msg = String.format(java.util.Locale.US,
                "1 %f:::%f %f %f",
                ts,
                east,
                north,
                down);
        System.out.println(msg);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double ts = android.os.SystemClock.elapsedRealtime() / 1000.;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (!m_got_rotation_vector) {
                    break;
                }
                System.arraycopy(event.values, 0, lin_acc, 0, 3);
                lin_acc[3] = 1.f; // not necessary
                android.opengl.Matrix.multiplyMV(acc_world, 0, RI, 0, lin_acc, 0);
                onNEDReceived(ts, acc_world[0], acc_world[1], acc_world[2]);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                m_got_rotation_vector = true;
//                SensorManager.getRotationMatrixFromVector(R, event.values);
//                android.opengl.Matrix.invertM(RI, 0, R, 0);
                SensorManager.getRotationMatrixFromVector(RI, event.values);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}
