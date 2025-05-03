package com.example.mlmexample.loggers;

import android.hardware.SensorManager;
import android.view.WindowManager;

import com.elvishew.xlog.XLog;
import com.example.mlmexample.sensors.AbsAccelerometerSensor;


public class AbsAccelerometerLogger extends AbsAccelerometerSensor {
    public AbsAccelerometerLogger(SensorManager sensor_manager, WindowManager window_manager) {
        super(sensor_manager, window_manager);
    }

    @Override
    public void onENUReceived(double ts, float east, float north, float up) {
        float x = east;
        float y = north;
        float z = up;
        String fmt = "1 %f:::%f %f %f";
        String msg = String.format(java.util.Locale.US, fmt, ts, x, y, z);
        XLog.i(msg);
    }
}
