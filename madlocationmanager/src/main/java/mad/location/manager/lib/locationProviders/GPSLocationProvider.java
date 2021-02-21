package mad.location.manager.lib.locationProviders;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;


import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mad.location.manager.R;
import mad.location.manager.lib.Interfaces.LocationServiceStatusInterface;
import mad.location.manager.lib.Services.Settings;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;

public class GPSLocationProvider implements LocationListener {
    private final LocationManager m_locationManager;
    private final LocationProviderCallback locationProviderCallback;
    private final GPSCallback gpsCallback;
    private final Context context;
    private GpsStatus m_gpsStatus;
    private int gpsSatteliteCount = 0;
    String MSG_KEY = "satelliteCount";
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            gpsSatteliteCount = bundle.getInt(MSG_KEY);
            gpsCallback.gpsSatelliteCountChanged(gpsSatteliteCount);
        }
    };
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(int event) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                m_gpsStatus = m_locationManager.getGpsStatus(m_gpsStatus);
                int activeSatellites = 0;
                if (m_gpsStatus != null) {
                    for (GpsSatellite satellite : m_gpsStatus.getSatellites()) {
                        activeSatellites += satellite.usedInFix() ? 1 : 0;
                    }
                    gpsSatteliteCount = activeSatellites;

                    if (activeSatellites != 0) {
                        gpsCallback.gpsSatelliteCountChanged(activeSatellites);
                    }
                }
            }
        }
    };
    @RequiresApi(Build.VERSION_CODES.N)
    private final GnssStatus.Callback gnssStatus = new GnssStatus.Callback() {
        /**
         * Called periodically to report GNSS satellite status.
         *
         * @param status the current status of all satellites.
         */
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt(MSG_KEY, status.getSatelliteCount());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    };

    public GPSLocationProvider(Context context, LocationProviderCallback locationProviderCallback, GPSCallback gpsCallback) {
        m_locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        this.locationProviderCallback = locationProviderCallback;
        this.context = context;
        this.gpsCallback = gpsCallback;
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    public void startLocationUpdates(Settings m_settings, HandlerThread thread) {
        m_locationManager.removeGpsStatusListener(gpsListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            m_locationManager.registerGnssStatusCallback(executorService, gnssStatus);
        } else {
            m_locationManager.addGpsStatusListener(gpsListener);
        }
        m_locationManager.removeUpdates(this);
        if (m_settings.onlyGpsSensor) {
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    m_settings.gpsMinTime, m_settings.gpsMinDistance, this);
        } else {
            thread.start();
            Criteria criteria = new Criteria();
            criteria.setSpeedRequired(true);
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            m_locationManager.requestLocationUpdates(m_settings.gpsMinTime, m_settings.gpsMinDistance, criteria, this, thread.getLooper());
        }
    }

    public void stop() {
        m_locationManager.removeGpsStatusListener(gpsListener);
        m_locationManager.removeUpdates(this);
    }

    public int getGPSSatteliteCount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return gpsSatteliteCount;
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                m_gpsStatus = m_locationManager.getGpsStatus(m_gpsStatus);
                int activeSatellites = 0;
                if (m_gpsStatus != null) {
                    for (GpsSatellite satellite : m_gpsStatus.getSatellites()) {
                        activeSatellites += satellite.usedInFix() ? 1 : 0;
                    }

                    return activeSatellites;
                }
            }
        }
        return 0;
    }


    public boolean isProviderEnabled(String gpsProvider) {
        return m_locationManager.isProviderEnabled(gpsProvider);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationProviderCallback.onLocationAvailable(location);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            locationProviderCallback.locationAvailabilityChanged(false);
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            locationProviderCallback.locationAvailabilityChanged(true);
        }
    }
}
