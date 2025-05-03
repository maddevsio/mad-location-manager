package com.example.mlmexample.loggers;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import com.elvishew.xlog.XLog;
import com.example.mlmexample.sensors.GPSSensor;

public class GPSLogger extends GPSSensor {
    public GPSLogger(LocationManager locationManager, Context ctx) {
        super(locationManager, ctx);
    }

    @Override
    public void onGPSReceived(Location loc) {
        double speedAccuracyMpS = 0.1 * loc.getAccuracy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && loc.hasAccuracy()) {
            speedAccuracyMpS = loc.getSpeedAccuracyMetersPerSecond();
        }
        // &rec.data.gps.location.latitude,
        // &rec.data.gps.location.longitude,
        // &rec.data.gps.location.altitude,
        // &rec.data.gps.location.error,
        // &rec.data.gps.speed.value,
        // &rec.data.gps.speed.azimuth,
        // &rec.data.gps.speed.error);
        double ts = android.os.SystemClock.elapsedRealtime() / 1000.;
        String fmt = "4 %f:::%.8f %.8f %.8f %.8f %.8f %.8f %.8f";
        String msg = String.format(java.util.Locale.US,
                fmt,
                ts,
                loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), loc.getAccuracy(),
                loc.getSpeed(), loc.getBearing(), speedAccuracyMpS);
        XLog.i(msg);
    }
}
