package com.example.lezh1k.sensordatacollector.SensorDataProvider;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.lezh1k.sensordatacollector.Commons;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lezh1k on 12/20/17.
 */

public class SensorCalibrator implements SensorEventListener {

    private DeviationCalculator m_dcLinearAcceleration;
    private DeviationCalculator m_dcAbsLinearAcceleration;
    private List<Sensor> m_lstSensors = new ArrayList<Sensor>();
    private SensorManager m_sensorManager;

    public DeviationCalculator getDcLinearAcceleration() {
        return m_dcLinearAcceleration;
    }

    public DeviationCalculator getDcAbsLinearAcceleration() {
        return m_dcAbsLinearAcceleration;
    }

    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };

    public SensorCalibrator(SensorManager sensorManager) {
        m_dcAbsLinearAcceleration = new DeviationCalculator(500, 3);
        m_dcLinearAcceleration = new DeviationCalculator(500, 3);

        m_sensorManager = sensorManager;
        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.d(Commons.AppName, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }
    }

    public void reset() {
        m_dcAbsLinearAcceleration.reset();
        m_dcLinearAcceleration.reset();
    }

    boolean inProgress = false;

    public boolean isInProgress() {
        return inProgress;
    }

    public boolean start() {
        inProgress = true;
        for (Sensor sensor : m_lstSensors) {
            if (!m_sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME)) {
                return false;
            }
        }
        return true;
    }

    public void stop() {
        inProgress = false;
        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.unregisterListener(this, sensor);
        }
    }

    private float[] R = new float[16];
    private float[] RI = new float[16];
    private float[] accAxis = new float[4];
    private float[] linAcc = new float[4];

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linAcc, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(accAxis, 0, RI,
                        0, linAcc, 0);
                m_dcLinearAcceleration.Measure(event.values);
                m_dcAbsLinearAcceleration.Measure(accAxis);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(R, event.values);
                android.opengl.Matrix.invertM(RI, 0, R, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
