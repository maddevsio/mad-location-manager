package com.example.mlmexample.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LinAccelerometerSensor implements SensorEventListener, ISensor {

    private static final String TAG = "AccelerationLogger";
    private final List<Sensor> m_lst_sensors = new ArrayList<>();
    private final SensorManager m_sensor_manager;

    private boolean m_got_rotation_vector = false;

    private static final int[] sensor_types = {
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_ACCELEROMETER,
//            Sensor.TYPE_LINEAR_ACCELERATION,
    };

    public LinAccelerometerSensor(SensorManager sensor_manager) {
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

    public boolean start(int sensor_delay) {
        m_got_rotation_vector = false;
        for (Sensor sensor : m_lst_sensors) {
            if (!m_sensor_manager.registerListener(this, sensor, sensor_delay)) {
                Log.e(TAG, String.format("Couldn't registerListener %d", sensor.getType()));
                return false;
            }
        }
        return true;
    }

    public boolean start() {
        return start(SensorManager.SENSOR_DELAY_UI);
    }

    public boolean stop() {
        for (Sensor sensor : m_lst_sensors) {
            m_sensor_manager.unregisterListener(this, sensor);
        }
        return true;
    }

    // Adjust rotation matrix for display orientation

    protected final float[] R = new float[16];  // rotation matrix from rotation_vector
    protected final float[] RI = new float[16]; // inverted adjusted rotation matrix
    private final float[] gravity_phone_frame = new float[4];
    private final float[] gravity_world = {0.f, 0.f, 9.81f, 1.f};
    private final float[] lin_acc = {0.f, 0.f, 0.f, 1.f};

    public void onACCReceived(double ts, float X, float Y, float Z) {
        String msg = String.format(java.util.Locale.US,
                "1 %f:::%f %f %f",
                ts,
                X,
                Y,
                Z);
        System.out.println(msg);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double ts = android.os.SystemClock.elapsedRealtime() / 1000.;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                m_got_rotation_vector = true;
//                SensorManager.getRotationMatrixFromVector(R, event.values);
//                android.opengl.Matrix.invertM(RI, 0, R, 0);
                SensorManager.getRotationMatrixFromVector(RI, event.values);
                break;

            case Sensor.TYPE_ACCELEROMETER:
                if (!m_got_rotation_vector) {
                    break;
                }
                System.arraycopy(event.values, 0, lin_acc, 0, 3);
                android.opengl.Matrix.multiplyMV(gravity_phone_frame, 0, RI, 0, gravity_world, 0);
                for (int i = 0; i < 3; ++i) {
                    lin_acc[i] -= gravity_phone_frame[i];
                }
                onACCReceived(ts, lin_acc[0], lin_acc[1], lin_acc[2]);
                break;

            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (!m_got_rotation_vector) {
                    break;
                }
                System.arraycopy(event.values, 0, lin_acc, 0, 3);
                onACCReceived(ts, lin_acc[0], lin_acc[1], lin_acc[2]);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}
