package com.example.mlmexample.calibration;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.example.mlmexample.sensors.LinAccelerometerSensor;

public class LinAccelerometerCalibrator extends LinAccelerometerSensor {
    private static final String TAG = "LinAccelerometerCalibrator";
    private int m_calibration_measurements_number;
    private int m_calibration_measurements_count = 0;
    private boolean m_is_in_progress = false;
    private double offset_X = 0.;
    private double offset_Y = 0.;
    private double offset_Z = 0.;

    public LinAccelerometerCalibrator(SensorManager sensor_manager) {
        this(sensor_manager, 1000);
    }

    public LinAccelerometerCalibrator(SensorManager sensor_manager, int calibration_measurements_number) {
        super(sensor_manager);
        m_calibration_measurements_number = calibration_measurements_number;
    }

    @Override
    public boolean start() {
        m_is_in_progress = true;
        return super.start();
    }

    @Override
    public boolean start(int sensor_delay) {
        m_is_in_progress = true;
        return super.start(sensor_delay);
    }

    @Override
    public boolean stop() {
        m_is_in_progress = false;
        return super.stop();
    }

    public boolean IsInProgress() {
        return m_is_in_progress;
    }

    public double Offset_X() {
        return offset_X;
    }

    public double Offset_Y() {
        return offset_Y;
    }

    public double Offset_Z() {
        return offset_Z;
    }

    @Override
    public void onACCReceived(double ts, float X, float Y, float Z) {
        if (m_calibration_measurements_count == m_calibration_measurements_number) {
            m_is_in_progress = false;
            return;
        }

        ++m_calibration_measurements_count;
        offset_X += X / (double) m_calibration_measurements_number;
        offset_Y += Y / (double) m_calibration_measurements_number;
        offset_Z += Z / (double) m_calibration_measurements_number;
        // -0.287931 0.027557, 0.014905
        //
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
