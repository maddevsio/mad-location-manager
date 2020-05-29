package mad.location.manager.lib.location;

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

import mad.location.manager.lib.utils.Settings;

import static android.content.Context.LOCATION_SERVICE;

public class LocationDataManager implements GpsStatus.Listener, LocationListener {
    private static final String TAG = "LocationDataManager";

    private Context context;
    private LocationManager locationManager;
    private GpsStatus gpsStatus;
    private LocationCallback callback;

    private int gpsMinTime;
    private int gpsMinDistance;
    private boolean filterMockGpsCoordinates;

    private int activeSatellites;

    public LocationDataManager(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        gpsMinTime = Settings.DEFAULT_GPS_MIN_TIME;
        gpsMinDistance = Settings.DEFAULT_GPS_MIN_DISTANCE;
        filterMockGpsCoordinates = Settings.DEFAULT_FILTER_MOCK_COORDINATES;

        activeSatellites = 0;
    }

    public LocationDataManager setCallback(LocationCallback callback) {
        this.callback = callback;
        return this;
    }

    public LocationDataManager setGpsMinTime(int gpsMinTime) {
        this.gpsMinTime = gpsMinTime;
        return this;
    }

    public LocationDataManager setGpsMinDistance(int gpsMinDistance) {
        this.gpsMinDistance = gpsMinDistance;
        return this;
    }

    public LocationDataManager setFilterMockGpsCoordinates(boolean filterMockGpsCoordinates) {
        this.filterMockGpsCoordinates = filterMockGpsCoordinates;
        return this;
    }

    public boolean start() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "ACCESS_FINE_LOCATION permission denied");
            return false;
        }

        if (locationManager == null) {
            Log.e(TAG, "Location manager is null");
            return false;
        }

        locationManager.removeGpsStatusListener(this);
        locationManager.removeUpdates(this);

        locationManager.addGpsStatusListener(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsMinTime, gpsMinDistance, this);

        return true;
    }

    public void stop() {
        locationManager.removeGpsStatusListener(this);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            gpsStatus = locationManager.getGpsStatus(gpsStatus);

            int activeSatellites = 0;
            if (gpsStatus != null) {
                for (GpsSatellite satellite : gpsStatus.getSatellites()) {
                    activeSatellites += satellite.usedInFix() ? 1 : 0;
                }
            }

            if (activeSatellites != 0 && this.activeSatellites != activeSatellites) {
                this.activeSatellites = activeSatellites;

                if (callback != null) {
                    callback.GPSStatusChanged(activeSatellites);
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        if (filterMockGpsCoordinates && location.isFromMockProvider()) {
            return;
        }

        if (callback != null) {
            callback.locationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //do nothing
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER) && callback != null) {
            callback.GPSEnabledChanged(true);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER) && callback != null) {
            callback.GPSEnabledChanged(false);
        }
    }
}
