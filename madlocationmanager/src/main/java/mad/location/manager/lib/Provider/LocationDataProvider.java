package mad.location.manager.lib.Provider;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import mad.location.manager.lib.Interfaces.ILocationDataProvider;
import mad.location.manager.lib.Services.ServiceStatus;
import mad.location.manager.lib.Services.Settings;

public class LocationDataProvider implements LocationListener, ILocationDataProvider.Provider, GpsStatus.Listener {

    private final String TAG = LocationDataProvider.class.getName();

    private ILocationDataProvider.Client client;
    private LocationManager m_locationManager;
    private GpsStatus m_gpsStatus;

    private boolean m_gpsEnabled = false;
    private Context context;

    public LocationDataProvider (ILocationDataProvider.Client client, Context context) {
        m_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.client = client;
        this.context = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        client.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        /*  */
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            m_gpsEnabled = true;
            client.onProviderEnabled(m_gpsStatus, m_gpsEnabled);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            m_gpsEnabled = false;
            client.onProviderDisabled(m_gpsStatus, m_gpsEnabled);
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_gpsStatus = m_locationManager.getGpsStatus(m_gpsStatus);
        }

        int activeSatellites = 0;
        if (m_gpsStatus != null) {
            for (GpsSatellite satellite : m_gpsStatus.getSatellites()) {
                activeSatellites += satellite.usedInFix() ? 1 : 0;
            }

            client.activeSatellites(activeSatellites);
        }
    }

    @Override
    public void start(Settings m_settings, ServiceStatus status) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            status = ServiceStatus.PERMISSION_DENIED;
        } else {
            status = ServiceStatus.SERVICE_STARTED;
            m_locationManager.removeGpsStatusListener(this);
            m_locationManager.addGpsStatusListener(this);
            m_locationManager.removeUpdates(this);
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    m_settings.getGpsMinTime(), m_settings.getGpsMinDistance(), this );
        }
    }

    @Override
    public void stop(ServiceStatus status) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            status = ServiceStatus.SERVICE_STOPPED;
        } else {
            status = ServiceStatus.SERVICE_PAUSED;
            m_locationManager.removeGpsStatusListener(this);
            m_locationManager.removeUpdates(this);
        }
    }
}
