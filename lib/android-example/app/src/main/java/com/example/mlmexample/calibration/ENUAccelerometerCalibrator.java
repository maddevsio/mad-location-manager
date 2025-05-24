package com.example.mlmexample.calibration;

import android.hardware.SensorManager;

import com.example.mlmexample.sensors.ENUAccelerometerSensor;

// It seems this is totally wrong approach and we need to calibrate Linear Accelerometer
public class ENUAccelerometerCalibrator extends ENUAccelerometerSensor {
    private final int m_calibration_measurements_number;
    private int m_calibration_measurements_count = 0;
    private double north_offset = 0.;
    private double east_offset = 0.;
    private double up_offset = 0.;

    public ENUAccelerometerCalibrator(SensorManager sensor_manager) {
        this(sensor_manager, 1000);
    }

    public ENUAccelerometerCalibrator(SensorManager sensor_manager, int calibration_measurements_number) {
        super(sensor_manager);
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
        up_offset += up / (double) m_calibration_measurements_number;
    }

    public double NorthOffset() {
        return north_offset;
    }

    public double EastOffset() {
        return east_offset;
    }

    public double UpOffset() {
        return up_offset;
    }
}