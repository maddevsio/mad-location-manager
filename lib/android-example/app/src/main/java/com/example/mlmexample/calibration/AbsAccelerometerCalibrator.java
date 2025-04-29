package com.example.mlmexample.calibration;

import android.hardware.SensorManager;
import android.view.WindowManager;

import com.example.mlmexample.sensors.AbsAccelerometerSensor;

// It seems this is totally wrong approach and we need to calibrate Linear Accelerometer
public class AbsAccelerometerCalibrator extends AbsAccelerometerSensor {
    private final int m_calibration_measurements_number;
    private int m_calibration_measurements_count = 0;
    private boolean m_is_in_progress = false;

    private double north_offset = 0.;
    private double east_offset = 0.;
    private double down_offset = 0.;

    public AbsAccelerometerCalibrator(SensorManager sensor_manager) {
        this(sensor_manager,  1000);
    }

    public AbsAccelerometerCalibrator(SensorManager sensor_manager, int calibration_measurements_number) {
        super(sensor_manager);
        m_calibration_measurements_number = calibration_measurements_number;
    }

    @Override
    public boolean start() {
        m_is_in_progress = super.start();
        return m_is_in_progress;
    }

    @Override
    public boolean stop() {
        m_is_in_progress = false;
        return super.stop();
    }

    @Override
    public void onNEDReceived(double ts, float north, float east, float down) {
        if (m_calibration_measurements_count == m_calibration_measurements_number) {
            m_is_in_progress = false;
            return;
        }

        ++m_calibration_measurements_count;
        north_offset += north / (double) m_calibration_measurements_number;
        east_offset += east / (double) m_calibration_measurements_number;
        down_offset += down / (double) m_calibration_measurements_number;
    }

    public boolean IsInProgress() {
        return m_is_in_progress;
    }

    public double NorthOffset() {
        return north_offset;
    }

    public double EastOffset() {
        return east_offset;
    }

    public double DownOffset() {
        return down_offset;
    }
}