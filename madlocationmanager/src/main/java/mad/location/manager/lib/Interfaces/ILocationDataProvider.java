package mad.location.manager.lib.Interfaces;

import android.hardware.SensorEvent;
import android.location.GpsStatus;
import android.location.Location;

import mad.location.manager.lib.Services.ServiceStatus;
import mad.location.manager.lib.Services.Settings;

public interface ILocationDataProvider {
    interface Provider {
        void start(Settings m_settings, ServiceStatus status);
        void stop(ServiceStatus status);
    }

    interface Client {
        void onLocationChanged(Location location);
        void onProviderEnabled(GpsStatus gpsStatus, boolean gpsEnable);
        void onProviderDisabled(GpsStatus gpsStatus, boolean gpsEnable);
        void activeSatellites(int satellite);
    }
}
