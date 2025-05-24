package com.example.mlmexample.sensors;

import android.hardware.SensorManager;

public class ENUAccelerometerSensor extends RawENUSensor {
    protected final float[] enu_acc = {0.f, 0.f, 0.f}; // world coordinates (east/north/up)
    public ENUAccelerometerSensor(SensorManager sensor_manager) {
        super(sensor_manager);
    }

    public float[] ENU() {
        return enu_acc.clone();
    }

    protected void onENUReceived(double ts, float east, float north, float up) {
        String fmt = "1 %f:::%f %f %f";
        String msg = String.format(java.util.Locale.US, fmt, ts, east, north, up);
        System.out.println(msg);
    }

    private static void quatRotateVec(float[] q, float[] v, float[] out) {
        // q = (w, U); qV = (0, v)
        // out = qvq(-1)
        // qv=(−u⋅v,wv+u×v)  - see quaternion multiplication formula
        // q(−1)=(w,−u)
        final float w = q[0], x = q[1], y = q[2], z = q[3];

        // t = 2 * cross(q.xyz, v)
        final float t0 = 2f * (y * v[2] - z * v[1]);
        final float t1 = 2f * (z * v[0] - x * v[2]);
        final float t2 = 2f * (x * v[1] - y * v[0]);

        // out = v + w * t + cross(q.xyz, t)
        out[0] = v[0] + w * t0 + (y * t2 - z * t1);
        out[1] = v[1] + w * t1 + (z * t0 - x * t2);
        out[2] = v[2] + w * t2 + (x * t1 - y * t0);
    }

    @Override
    protected void onRawENUReceived(double ts, float[] acc, float[] q) {
        quatRotateVec(q, acc, enu_acc);
        onENUReceived(ts, enu_acc[0], enu_acc[1], enu_acc[2]);
    }
}