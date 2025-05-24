package com.example.mlmexample.loggers;

import android.hardware.SensorManager;

import com.elvishew.xlog.XLog;
import com.example.mlmexample.sensors.RawENUSensor;

public class RawENULogger extends RawENUSensor {
    public RawENULogger(SensorManager sensor_manager) {
        super(sensor_manager);
    }

    @Override
    protected void onRawENUReceived(double ts, float[] acc, float[] q) {
        float w = q[0], x = q[1], y = q[2], z = q[3];
        String fmt = "5 %f:::%f %f %f %f %f %f %f";
        String msg = String.format(java.util.Locale.US, fmt, ts, acc[0], acc[1], acc[2], w, x, y, z);
        XLog.i(msg);
    }
}
