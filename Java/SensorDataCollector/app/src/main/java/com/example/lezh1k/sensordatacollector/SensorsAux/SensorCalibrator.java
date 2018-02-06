package com.example.lezh1k.sensordatacollector.SensorsAux;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.lezh1k.sensordatacollector.CommonClasses.Commons;
import com.example.lezh1k.sensordatacollector.Filters.MeanFilter;

import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.elapsedRealtimeNanos;

/**
 * Created by lezh1k on 12/20/17.
 */

public class SensorCalibrator implements SensorEventListener {

    private DeviationCalculator m_dcLinearAcceleration;
    private DeviationCalculator m_dcAbsLinearAcceleration;
    private DeviationCalculator m_dcMeanLinearAcceleration;
    private List<Sensor> m_lstSensors = new ArrayList<Sensor>();
    private SensorManager m_sensorManager;
    private MeanFilter m_meanFilter = new MeanFilter(0.4f, 4);

    public DeviationCalculator getDcLinearAcceleration() {
        return m_dcLinearAcceleration;
    }
    public DeviationCalculator getDcAbsLinearAcceleration() {
        return m_dcAbsLinearAcceleration;
    }
    public DeviationCalculator getDcMeanLinearAcceleration() {return m_dcMeanLinearAcceleration; }

    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };

    public SensorCalibrator(SensorManager sensorManager) {
        final int measurementCalibrationCount = 1000;
        final int valuesCount = 3;
        m_dcAbsLinearAcceleration = new DeviationCalculator(measurementCalibrationCount, valuesCount);
        m_dcLinearAcceleration = new DeviationCalculator(measurementCalibrationCount, valuesCount);
        m_dcMeanLinearAcceleration = new DeviationCalculator(measurementCalibrationCount, valuesCount);

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
        m_dcMeanLinearAcceleration.reset();
    }

    boolean inProgress = false;
    public boolean isInProgress() {
        return inProgress;
    }

    public boolean start() {
        m_meanFilter.reset();
        inProgress = true;
        for (Sensor sensor : m_lstSensors) {
            if (!m_sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)) {
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

    public String getCalibrationStatus()  {
        return String.format("abs:%f%%, lin::%f%%, absmean::%f%%\n",
                m_dcAbsLinearAcceleration.getCompletePercentage(),
                m_dcLinearAcceleration.getCompletePercentage(),
                m_dcMeanLinearAcceleration.getCompletePercentage());
    }

    private float[] R = new float[16];
    private float[] RI = new float[16];
    private float[] accAxis = new float[4];
    private float[] linAcc = new float[4];

    private float[] tmp = new float[4];
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linAcc, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(accAxis, 0, RI,
                        0, linAcc, 0);
                m_dcLinearAcceleration.Measure(event.values);
                m_dcAbsLinearAcceleration.Measure(accAxis);
                long ts = elapsedRealtimeNanos();
                m_meanFilter.filter(accAxis, ts, tmp);
                m_dcMeanLinearAcceleration.Measure(tmp);
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
