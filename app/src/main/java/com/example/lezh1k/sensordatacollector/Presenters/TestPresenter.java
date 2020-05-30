package com.example.lezh1k.sensordatacollector.Presenters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.lezh1k.sensordatacollector.Interfaces.TestInterface;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mad.location.manager.lib.Interfaces.LocationListener;
import mad.location.manager.lib.MadLocationManagerService;
import mad.location.manager.lib.logger.Logger;
import mad.location.manager.lib.utils.ServiceHelper;

public class TestPresenter {
    private static final String TAG = "TestPresenter";

    private TestInterface viewInterface;
    private Context context;

    private MadLocationManagerService service;

    private LocationListener rawLocationListener;
    private LocationListener kalmanFilteredLocationListener;
    private LocationListener geoHashFilteredLocationListener;

    private List<Location> rawLocations;
    private List<Location> kalmanFilteredLocations;
    private List<Location> geoHashFilteredLocations;

    public TestPresenter(Context context, TestInterface viewInterface) {
        this.viewInterface = viewInterface;
        this.context = context;

        rawLocations = new ArrayList<>();
        kalmanFilteredLocations = new ArrayList<>();
        geoHashFilteredLocations = new ArrayList<>();

        ServiceHelper.getService(context, MadLocationManagerService.class, value -> {
            service = value;

            value.addRawLocationListeners(rawLocationListener)
                    .addKalmanFilteredLocationListeners(kalmanFilteredLocationListener)
                    .addGeoHashFilteredLocationListeners(geoHashFilteredLocationListener);
        });

        rawLocationListener = location -> {
            rawLocations.add(location);
            viewInterface.showCurrentPosition(new LatLng(location));
            viewInterface.updateRoute();
        };
        kalmanFilteredLocationListener = location -> {
            kalmanFilteredLocations.add(location);
            viewInterface.updateRoute();
        };
        geoHashFilteredLocationListener = location -> {
            geoHashFilteredLocations.add(location);
            viewInterface.updateRoute();
        };

    }

    public List<LatLng> getRawLocations() {
        List<LatLng> locations = new ArrayList<>();

        for (Location location : rawLocations) {
            locations.add(new LatLng(location));
        }
        return locations;
    }

    public List<LatLng> getKalmanFilteredLocations() {
        List<LatLng> locations = new ArrayList<>();

        for (Location location : kalmanFilteredLocations) {
            locations.add(new LatLng(location));
        }

        return locations;
    }

    public List<LatLng> getGeoHashFilteredLocations() {
        List<LatLng> locations = new ArrayList<>();

        for (Location location : geoHashFilteredLocations) {
            locations.add(new LatLng(location));
        }

        return locations;
    }

    public void startService() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            viewInterface.requestPermissions();
        } else {
            if (service != null) {
                service.start();
            }
        }
    }

    public void stopService() {
        if (service != null) {
            service.stop();
        }
    }

    public void enableLog() {
        Logger.setEnabled(true);
    }

    public void disableLog() {
        Logger.setEnabled(false);
    }

    public void share() {
        Logger.setEnabled(false);
        Intent intent = Logger.getShareIntent(context, "com.example.lezh1k.sensordatacollector.provider");

        if (intent != null) {
            viewInterface.startActivity(intent);
        }
    }

    public void deleteLog() {
        File file = Logger.getFile();
        if (file != null && file.exists() && file.delete()) {
            Log.d(TAG, "File deleted");
        }
    }

    public void onDestroy() {
        if (service != null) {
            service.removeRawLocationListeners(rawLocationListener)
                    .removeKalmanFilteredLocationListeners(kalmanFilteredLocationListener)
                    .removeGeoHashFilteredLocationListeners(geoHashFilteredLocationListener);
        }
    }
}
