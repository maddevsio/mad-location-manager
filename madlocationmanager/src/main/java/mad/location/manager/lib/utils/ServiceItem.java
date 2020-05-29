package mad.location.manager.lib.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import mad.location.manager.lib.Interfaces.SimpleTempCallback;

class ServiceItem<T extends BaseService> {
    private Class<T> classType;
    private BaseService service;
    private ServiceConnection serviceConnection;
    private List<SimpleTempCallback<T>> serviceRequests;

    private boolean connectingService;

    ServiceItem(Class<T> classType) {
        this.classType = classType;

        serviceRequests = new ArrayList<>();

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder binder) {
                connectingService = false;
                service = ((T.BaseServiceBinder) binder).getService();
                if (!serviceRequests.isEmpty()) {
                    for (SimpleTempCallback<T> callback : serviceRequests) {
                        if (callback != null) {
                            callback.onCall(ServiceItem.this.classType.cast(service));
                        }
                    }
                    serviceRequests.clear();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                connectingService = false;
                service = null;
            }
        };
    }

    Class<T> getClassType() {
        return classType;
    }

    void getService(Context context, SimpleTempCallback<T> callback) {
        if (service != null) {
            if (callback != null) {
                callback.onCall(classType.cast(service));
            }
        } else {
            if (callback != null) {
                serviceRequests.add(callback);
            }
            if (!connectingService) {
                connectingService = true;
                Intent serviceIntent = new Intent(context.getApplicationContext(), classType);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.getApplicationContext().startForegroundService(serviceIntent);
                }

                context.getApplicationContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }
}