package com.example.mlmexample.loggers;

import android.hardware.SensorManager;

import com.elvishew.xlog.XLog;
import com.example.mlmexample.sensors.ENUAccelerometerSensor;


public class ENUAccelerometerLogger extends ENUAccelerometerSensor {
    public ENUAccelerometerLogger(SensorManager sensor_manager) {
        super(sensor_manager);
    }

    @Override
    public void onENUReceived(double ts, float east, float north, float up) {
        String fmt = "1 %f:::%.8f %.8f %.8f";
        String msg = String.format(java.util.Locale.US, fmt, ts, east, north, up);
        XLog.i(msg);
    }
}
