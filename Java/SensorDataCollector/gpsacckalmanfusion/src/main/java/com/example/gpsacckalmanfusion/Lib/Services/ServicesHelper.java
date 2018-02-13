package com.example.gpsacckalmanfusion.Lib.Services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.gpsacckalmanfusion.Lib.Interfaces.LocationServiceInterface;
import com.example.gpsacckalmanfusion.Lib.Interfaces.LocationServiceStatusInterface;
import com.example.gpsacckalmanfusion.Lib.Interfaces.SimpleTempCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lezh1k on 2/13/18.
 */

public class ServicesHelper {
    public static ServicesHelper instance = new ServicesHelper();

    //Location Service
    private boolean connectingLocationService = false;
    private LocationService locationService;
    private List<SimpleTempCallback<LocationService>> locationServiceRequests = new ArrayList<>();

    private List<LocationServiceInterface> locationServiceInterfaces = new ArrayList<>();
    private List<LocationServiceStatusInterface> locationServiceStatusInterfaces = new ArrayList<>();

    public static void addLocationServiceInterface(LocationServiceInterface locationServiceInterface) {
        if (!instance.locationServiceInterfaces.contains(locationServiceInterface)) {
            instance.locationServiceInterfaces.add(locationServiceInterface);
            if (instance.locationService != null) {
                instance.locationService.addInterface(locationServiceInterface);
            }
        }
    }

    public static void removeLocationServiceInterface(LocationServiceInterface locationServiceInterface) {
        instance.locationServiceInterfaces.remove(locationServiceInterface);
        if (instance.locationService != null) {
            instance.locationService.removeInterface(locationServiceInterface);
        }
    }

    public static void addLocationServiceStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        if (!instance.locationServiceStatusInterfaces.contains(locationServiceStatusInterface)) {
            instance.locationServiceStatusInterfaces.add(locationServiceStatusInterface);
            if (instance.locationService != null) {
                instance.locationService.addStatusInterface(locationServiceStatusInterface);
            }
        }
    }

    public static void removeLocationServiceStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        instance.locationServiceStatusInterfaces.remove(locationServiceStatusInterface);
        if (instance.locationService != null) {
            instance.locationService.removeStatusInterface(locationServiceStatusInterface);
        }
    }

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            connectingLocationService = false;
            locationService = ((LocationService.LocalBinder) service).getService();
            if (!locationServiceRequests.isEmpty()) {
                for (SimpleTempCallback<LocationService> callback : locationServiceRequests) {
                    if (callback != null) {
                        callback.onCall(locationService);
                    }
                }
                locationServiceRequests.clear();
            }

            if (locationServiceInterfaces != null && !locationServiceInterfaces.isEmpty()) {
                locationService.addInterfaces(locationServiceInterfaces);
            }
            if (locationServiceStatusInterfaces != null && !locationServiceStatusInterfaces.isEmpty()) {
                locationService.addStatusInterfaces(locationServiceStatusInterfaces);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            connectingLocationService = false;
            locationService = null;
        }
    };

    public static void getLocationService(Context context, SimpleTempCallback<LocationService> callback) {
        if (instance.locationService != null) {
            if (callback != null) {
                callback.onCall(instance.locationService);
            }
        } else {
            if (callback != null) {
                instance.locationServiceRequests.add(callback);
            }
            if (!instance.connectingLocationService) {
                instance.connectingLocationService = true;
                Intent serviceIntent = new Intent(context.getApplicationContext(),
                        KalmanLocationService.class);
                context.getApplicationContext().bindService(serviceIntent, instance.locationServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

}
