package com.example.mlmexample.calibration;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.example.mlmexample.sensors.LinAccelerometerSensor;

public class LinAccelerometerCalibrator extends LinAccelerometerSensor {
    private static final String TAG = "LinAccelerometerCalibrator";
    private final int m_calibration_measurements_number;
    private int m_calibration_measurements_count = 0;
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
            stop();
            return;
        }
        ++m_calibration_measurements_count;
        offset_X += X / (double) m_calibration_measurements_number;
        offset_Y += Y / (double) m_calibration_measurements_number;
        offset_Z += Z / (double) m_calibration_measurements_number;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}
