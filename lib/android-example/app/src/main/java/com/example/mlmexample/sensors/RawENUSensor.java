package com.example.mlmexample.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RawENUSensor extends ISensor implements SensorEventListener {
    private static final String TAG = "RawENUSensor";
    private final List<Sensor> m_lst_sensors = new ArrayList<>();
    private final SensorManager m_sensor_manager;
    private static final int[] sensor_types = {Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_ROTATION_VECTOR};
    public RawENUSensor(SensorManager sensor_manager) {
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
    protected final float[] lin_acc = {0.f, 0.f, 0.f};
    // For sensor timing synchronization
    private long m_last_rotation_ts = 0;
    private volatile boolean m_is_rotation_valid = false;
    private static final long MAX_SENSOR_TIMESTAMP_DELTA_NS = 4 * 1000_000; // 5ms in nanoseconds
    protected void onRawENUReceived(double ts, float[] acc, float[] q) {
        float w = q[0], x = q[1], y = q[2], z = q[3];
        String fmt = "5 %f:::%f %f %f %f %f %f %f";
        String msg = String.format(java.util.Locale.US, fmt, ts, acc[0], acc[1], acc[2], w, x, y, z);
        System.out.println(msg);
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
        onRawENUReceived(ts, lin_acc, qDev2World);
    }

    private void handleRotationVector(SensorEvent event) {
        long ts = event.timestamp;
        SensorManager.getQuaternionFromVector(qDev2World, event.values);
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
}