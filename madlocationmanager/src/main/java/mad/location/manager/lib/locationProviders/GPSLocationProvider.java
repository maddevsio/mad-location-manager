package mad.location.manager.lib.locationProviders;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;


import mad.location.manager.lib.Services.Settings;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;

public class GPSLocationProvider {
    private LocationManager m_locationManager;
    private LocationListener locationListener;
    private GpsStatus.Listener gpsListener;
    public GPSLocationProvider(Context context,LocationListener locationListener,GpsStatus.Listener gpsListener  ){
        m_locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        this.locationListener = locationListener;
        this.gpsListener = gpsListener;
    }
    @RequiresPermission(ACCESS_FINE_LOCATION)
    public void startLocationUpdates(Settings m_settings, HandlerThread thread){
        m_locationManager.removeGpsStatusListener(gpsListener);
        m_locationManager.addGpsStatusListener(gpsListener);
        m_locationManager.removeUpdates(locationListener);
        if (m_settings.onlyGpsSensor) {
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    m_settings.gpsMinTime, m_settings.gpsMinDistance, locationListener);
        } else {
            thread.start();
            Criteria criteria = new Criteria();
            criteria.setSpeedRequired(true);
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            m_locationManager.requestLocationUpdates(m_settings.gpsMinTime, m_settings.gpsMinDistance, criteria, locationListener, thread.getLooper());
        }
    }

    public void stop(){
        m_locationManager.removeGpsStatusListener(gpsListener);
        m_locationManager.removeUpdates(locationListener);
    }
    @RequiresPermission(ACCESS_FINE_LOCATION)
    public GpsStatus getGpsStatus(GpsStatus m_gpsStatus) {
        return m_locationManager.getGpsStatus(m_gpsStatus);
    }

    public boolean isProviderEnabled(String gpsProvider) {
        return m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
