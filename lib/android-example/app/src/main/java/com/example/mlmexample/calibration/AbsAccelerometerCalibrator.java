package com.example.mlmexample.calibration;

import android.hardware.SensorManager;
import android.view.WindowManager;

import com.example.mlmexample.sensors.AbsAccelerometerSensor;

// It seems this is totally wrong approach and we need to calibrate Linear Accelerometer
public class AbsAccelerometerCalibrator extends AbsAccelerometerSensor {
    private final int m_calibration_measurements_number;
    private int m_calibration_measurements_count = 0;

    private double north_offset = 0.;
    private double east_offset = 0.;
    private double down_offset = 0.;

    public AbsAccelerometerCalibrator(SensorManager sensor_manager, WindowManager window_manager) {
        this(sensor_manager, window_manager,  1000);
    }

    public AbsAccelerometerCalibrator(SensorManager sensor_manager, WindowManager window_manager, int calibration_measurements_number) {
        super(sensor_manager, window_manager);
        m_calibration_measurements_number = calibration_measurements_number;
    }


    @Override
    public void onENUReceived(double ts, float east, float north, float up) {
        if (m_calibration_measurements_count == m_calibration_measurements_number) {
            stop();
            return;
        }

        ++m_calibration_measurements_count;
        east_offset += east / (double) m_calibration_measurements_number;
        north_offset += north / (double) m_calibration_measurements_number;
        down_offset += up / (double) m_calibration_measurements_number;
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