package com.example.lezh1k.sensordatacollector.v4;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

public class LocationProviderService extends Service {

    private final String TAG = getClass().getName();
    protected final Service locationService = LocationProviderService.this;

    private LocationListener mListener = null;

    private FusedLocationProviderClient mFusedLocationClient;

    private boolean isDeviceStill = false;
    private Location mLastKnownLocation = null;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public LocationProviderService getService() {
            return (LocationProviderService) locationService;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        getLastLocationSharedPreferences();

        setupSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
        setLastLocationSharedPreferences();

        try {
            unregisterReceiver(mDeviceGPSStatusReceiver);
            mFusedLocationClient.removeLocationUpdates(mFusedLocationUpdateCallback);
            unregisterReceiver(mActivityRecognitionReceiver);
        } catch (Exception e) {
            // safely ignore if the receivers are already unregistered
        }
    }

    private void setupSelf() {
        registerReceiver(mDeviceGPSStatusReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    private boolean isDeviceGPSEnabled() {
        LocationManager mLocationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));

        return mLocationManager != null && mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean hasGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
        return resultCode == ConnectionResult.SUCCESS;
    }

    @RequiresPermission(allOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    private void registerToFusedLocationProvider() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000L);
        locationRequest.setSmallestDisplacement(3f);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkLocationSettings(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {

            if (task.isSuccessful() && task.getResult() != null) {
                onNewLocationFromFusedProvider(task.getResult());
            } else {
                Log.e(TAG, "could not get lastlocation from fusedLocationProvider");
            }
        });

        initLocationCallback();
        mFusedLocationClient.requestLocationUpdates(locationRequest, mFusedLocationUpdateCallback, null)
                .addOnFailureListener(exception -> Log.e(TAG, exception.getMessage()));

    }

    private void checkLocationSettings(@NonNull LocationRequest locationRequest) {
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest
                .Builder()
                .addLocationRequest(locationRequest)
                .build();

        LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(locationSettingsRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.v(TAG, "checkLocationSettings successfull");
                    } else {
                        Log.v(TAG, "checkLocationSettings failed");
                    }
                })
                .addOnFailureListener(exception -> {
                    if (exception instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        if (mListener != null)
                            mListener.onRequestHighAccuracyLocationSettings(((ResolvableApiException) exception));
                    }
                });
    }

    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    private void registerToActivityRecognition() {

        // ignored BuildConfig.FLAVOR ConstantConditionIf

        Intent intent = new Intent(getApplicationContext(), ActivityRecognitionService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        ActivityRecognitionClient mActivityRecognitionClient = ActivityRecognition.getClient(this);

        mActivityRecognitionClient.requestActivityUpdates(
                1000L, // CONSTANT
                pendingIntent
        );

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActivityRecognitionService.ACTIVITY_RECOGNITION_INTENT_FLAG);
        registerReceiver(mActivityRecognitionReceiver, intentFilter);
    }

    private final BroadcastReceiver mDeviceGPSStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                if (isDeviceGPSEnabled()) {
                    mListener.onDeviceGPSEnabled();
                } else {
                    mListener.onDeviceGPSDisabled();
                }
            }
        }
    };

    private final BroadcastReceiver mActivityRecognitionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isDeviceStill = intent.getStringExtra("Activity").equals(ActivityRecognitionService.STILL);
        }
    };

    private LocationCallback mFusedLocationUpdateCallback;

    private void initLocationCallback(){
        mFusedLocationUpdateCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        onNewLocationFromFusedProvider(location);

                        Log.d(TAG, "accur: " + location.getAccuracy() + " => " + location.getLatitude() + " ; " + location.getLongitude());
                    }
                }
            }
        };
    }

    private void onNewLocationFromFusedProvider(@NonNull Location pLocation) {
        Log.v(TAG, "received new location data from ${pLocation.provider}");
        boolean isNewLocationValid = true;

        if (mLastKnownLocation != null) {
            Log.v(TAG, "fining new location data and testing against validity criteria");
            Location finedLocation = fineLocation(pLocation);
            isNewLocationValid = testLocationAgainstValidityCriteria(mLastKnownLocation, finedLocation);
        }

        if (isNewLocationValid) {
            Log.v(TAG, "new location data is valid. updating lastKnownLocation and notifying listeners");
            mLastKnownLocation = pLocation;
            notifyAppLocationListener(mLastKnownLocation);
        }
    }

    private Location fineLocation(@NonNull Location location) {
        return MovingAverage.getInstance().calcMovingAverage(location);
    }

    private boolean testLocationAgainstValidityCriteria(@NonNull Location oldLocation, @NonNull Location newLocation) {

        if(isDeviceStill) return false;

        boolean isBetterLocation = IsBetterLocation.getInstance().isBetterLocation(newLocation, oldLocation);
        if (!isBetterLocation && MovingAverage.getInstance().currentNumOfSamples > 1) {
            return false;
        }

        return true;
    }

    private void notifyAppLocationListener(Location location) {
        mListener.onNewLocation(location);
    }

    public interface LocationListener {

        void onLocationTrackingStarted();

        void onLocationTrackingStopped();

        void onNewLocation(@NonNull Location location);

        void onDeviceGPSEnabled();

        void onDeviceGPSDisabled();

        void onMissingGooglePlayServices();

        void onRequestHighAccuracyLocationSettings(@NonNull ResolvableApiException exception);

    }

    private void getLastLocationSharedPreferences() {
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(getApplication());
        float latitude = preferenceManager.getFloat("LATITUDE", 0.0f);
        float longitude = preferenceManager.getFloat("LONGITUDE", 0.0f);
        Location location = new Location("sharedpreferences");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        if (mLastKnownLocation == null && latitude != 0.0f && longitude != 0.0f) {
            mLastKnownLocation = location;
        }
    }

    private void setLastLocationSharedPreferences() {
        if (mLastKnownLocation != null) {
            SharedPreferences.Editor preferencesManager = PreferenceManager.getDefaultSharedPreferences(getApplication()).edit();
            preferencesManager.putFloat("LATITUDE", (float) mLastKnownLocation.getLatitude());
            preferencesManager.putFloat("LONGITUDE", (float) mLastKnownLocation.getLongitude());
            preferencesManager.apply();
        }
    }

    @RequiresPermission(allOf = {
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
    })
    public void startLocationTracking(@NonNull LocationListener listener) {
        mListener = listener;

        if (!isDeviceGPSEnabled()) {
            mListener.onDeviceGPSDisabled();
            return;
        }

        if (!hasGooglePlayServices()) {
            mListener.onMissingGooglePlayServices();
            return;
        }

        registerToFusedLocationProvider();
        registerToActivityRecognition();

        mListener.onLocationTrackingStarted();

    }

    public void stopLocationTracking() {

        //
        if (mListener != null) {
            mListener.onLocationTrackingStopped();
        }
        mListener = null;

        setLastLocationSharedPreferences();

        try {
            unregisterReceiver(mDeviceGPSStatusReceiver);
            mFusedLocationClient.removeLocationUpdates(mFusedLocationUpdateCallback);
        } catch (Exception e) {
            // safely ignore if the receivers are already unregistered
        }
    }

}
