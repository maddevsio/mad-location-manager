package mad.location.manager.lib.Services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import mad.location.manager.lib.Interfaces.LocationServiceInterface;
import mad.location.manager.lib.Interfaces.LocationServiceStatusInterface;
import mad.location.manager.lib.Interfaces.SimpleTempCallback;

/**
 * Created by lezh1k on 2/13/18.
 */

public class ServicesHelper {
    public static ServicesHelper instance = new ServicesHelper();

    //Location Service
    private boolean connectingLocationService = false;
    private KalmanLocationService kalmanLocationService;
    private List<SimpleTempCallback<KalmanLocationService>> locationServiceRequests = new ArrayList<>();

    private List<LocationServiceInterface> locationServiceInterfaces = new ArrayList<>();
    private List<LocationServiceStatusInterface> locationServiceStatusInterfaces = new ArrayList<>();

    public static void addLocationServiceInterface(LocationServiceInterface locationServiceInterface) {
        if (!instance.locationServiceInterfaces.contains(locationServiceInterface)) {
            instance.locationServiceInterfaces.add(locationServiceInterface);
            if (instance.kalmanLocationService != null) {
                instance.kalmanLocationService.addInterface(locationServiceInterface);
            }
        }
    }

    public static void removeLocationServiceInterface(LocationServiceInterface locationServiceInterface) {
        instance.locationServiceInterfaces.remove(locationServiceInterface);
        if (instance.kalmanLocationService != null) {
            instance.kalmanLocationService.removeInterface(locationServiceInterface);
        }
    }

    public static void addLocationServiceStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        if (!instance.locationServiceStatusInterfaces.contains(locationServiceStatusInterface)) {
            instance.locationServiceStatusInterfaces.add(locationServiceStatusInterface);
            if (instance.kalmanLocationService != null) {
                instance.kalmanLocationService.addStatusInterface(locationServiceStatusInterface);
            }
        }
    }

    public static void removeLocationServiceStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        instance.locationServiceStatusInterfaces.remove(locationServiceStatusInterface);
        if (instance.kalmanLocationService != null) {
            instance.kalmanLocationService.removeStatusInterface(locationServiceStatusInterface);
        }
    }

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            connectingLocationService = false;
            kalmanLocationService = ((KalmanLocationService.LocalBinder) service).getService();
            if (!locationServiceRequests.isEmpty()) {
                for (SimpleTempCallback<KalmanLocationService> callback : locationServiceRequests) {
                    if (callback != null) {
                        callback.onCall(kalmanLocationService);
                    }
                }
                locationServiceRequests.clear();
            }

            if (locationServiceInterfaces != null && !locationServiceInterfaces.isEmpty()) {
                kalmanLocationService.addInterfaces(locationServiceInterfaces);
            }
            if (locationServiceStatusInterfaces != null && !locationServiceStatusInterfaces.isEmpty()) {
                kalmanLocationService.addStatusInterfaces(locationServiceStatusInterfaces);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            connectingLocationService = false;
            kalmanLocationService = null;
        }
    };

    public static void getLocationService(Context context, SimpleTempCallback<KalmanLocationService> callback) {
        if (instance.kalmanLocationService != null) {
            if (callback != null) {
                callback.onCall(instance.kalmanLocationService);
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

    public static KalmanLocationService getLocationService() {
        return instance.kalmanLocationService;
    }
}
