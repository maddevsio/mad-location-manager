package mad.location.manager.lib.locationProviders;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.location.LocationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import mad.location.manager.lib.Interfaces.LocationServiceStatusInterface;
import mad.location.manager.lib.Services.KalmanLocationService;
import mad.location.manager.lib.Services.Settings;

public class FusedLocationProvider {
    private FusedLocationProviderClient m_fusedLocationProviderClient;
    private LocationRequest m_locationRequest;
    LocationSettingsRequest.Builder builder;
    SettingsClient client;
    Task<LocationSettingsResponse> task;
    Context context;
    LocationProviderCallback m_locationProvider;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location loc : locationResult.getLocations()) {
                m_locationProvider.onLocationAvailable(loc);
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            builder =new LocationSettingsRequest.Builder()
                    .addLocationRequest(m_locationRequest);
            client = LocationServices.getSettingsClient(context);
            task = client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    m_locationProvider.locationAvailabilityChanged(true);
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    m_locationProvider.locationAvailabilityChanged(false);
                }
            });

        }
    };

    public FusedLocationProvider(Context context,LocationProviderCallback m_locationProvider ) {
        this.m_fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.context = context;
        this.m_locationProvider = m_locationProvider;
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

    public boolean isProviderEnabled() {
        return LocationManagerCompat.isLocationEnabled((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
    }
}

