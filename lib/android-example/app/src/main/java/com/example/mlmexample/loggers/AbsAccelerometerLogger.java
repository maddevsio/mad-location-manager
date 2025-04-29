package com.example.mlmexample.loggers;

import android.hardware.SensorManager;
import android.view.WindowManager;

import com.elvishew.xlog.XLog;
import com.example.mlmexample.sensors.AbsAccelerometerSensor;


public class AbsAccelerometerLogger extends AbsAccelerometerSensor {
    public AbsAccelerometerLogger(SensorManager sensor_manager) {
        super(sensor_manager);
    }

    @Override
    public void onNEDReceived(double ts, float north, float east, float down) {
        // north = acc_world[0] --> Y
        // east = acc_world[1] --> X
        // down = acc_world[2] --> Z
        // however lib waits tuple (x, y, z)
        String msg = String.format(java.util.Locale.US,
                "1 %f:::%f %f %f", ts, east, north, down);
        XLog.i(msg);
    }
}
