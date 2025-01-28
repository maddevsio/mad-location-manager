package com.example.mlmexample.loggers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import android.Manifest;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.elvishew.xlog.XLog;

public class GPSLogger implements LocationListener, IDataLogger {
    private LocationManager m_locationManager = null;
    private Context m_context = null;

    public GPSLogger(LocationManager locationManager, Context ctx) {
        m_locationManager = locationManager;
        m_context = ctx;
    }

    @Override
    public void onLocationChanged(@NonNull Location loc) {
        double speedAccuracyMpS = 0.1 * loc.getAccuracy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && loc.hasAccuracy()) {
            speedAccuracyMpS = loc.getSpeedAccuracyMetersPerSecond();
        }

//       &rec.data.gps.location.latitude,
//       &rec.data.gps.location.longitude,
//       &rec.data.gps.location.altitude,
//       &rec.data.gps.location.error,
//       &rec.data.gps.speed.value,
//       &rec.data.gps.speed.azimuth,
//       &rec.data.gps.speed.error);

        double ts = android.os.SystemClock.elapsedRealtime() / 1000.;
        String msg = String.format(java.util.Locale.US, "4 %f:::%f %f %f %f %f %f %f",
                ts,
                loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), loc.getAccuracy(), // location
                loc.getSpeed(), loc.getBearing(), speedAccuracyMpS); // speed
        XLog.i(msg);
    }

    @Override
    public boolean start() {
        if (m_locationManager == null)
            return false;
        if (ActivityCompat.checkSelfPermission(m_context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        m_locationManager.removeUpdates(this);
        m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 0, this);
        return true;
    }

    @Override
    public boolean stop() {
        if (m_locationManager == null)
            return false;
        m_locationManager.removeUpdates(this);
        return true;
    }
}
