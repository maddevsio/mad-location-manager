package mad.location.manager.lib.locationProviders;

import android.content.Context;
import android.os.HandlerThread;

import androidx.annotation.RequiresPermission;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import mad.location.manager.lib.Services.KalmanLocationService;
import mad.location.manager.lib.Services.Settings;

public class FusedLocationProvider {
    private FusedLocationProviderClient m_fusedLocationProviderClient;
    private LocationRequest m_locationRequest;
    LocationCallback locationCallback;
    public FusedLocationProvider(Context context, LocationCallback locationCallback) {
        this.m_fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.locationCallback = locationCallback;
    }
    @RequiresPermission(
            anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}
    )
    public void startLocationUpdates(Settings m_settings, HandlerThread thread) {
        m_locationRequest = LocationRequest.create();
        m_locationRequest.setInterval(m_settings.gpsMinTime);
        m_locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        m_fusedLocationProviderClient.requestLocationUpdates(m_locationRequest,
                locationCallback,
                thread.getLooper());
    }

    public void stop() {
        m_fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
