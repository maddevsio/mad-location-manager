package mad.location.manager.lib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import mad.location.manager.lib.Interfaces.LocationListener;
import mad.location.manager.lib.utils.BaseService;

public class MadLocationManagerService extends BaseService {
    private static final int NOTIFICATION_ID = 515;
    private static final String NOTIFICATION_CHANNEL_ID = "MLM_Service";

    private MadLocationManager madLocationManager;
    private NotificationManager notificationManager;
    private Notification notification;


    @Override
    public void onCreate() {
        super.onCreate();

        madLocationManager = new MadLocationManager(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = getDefaultNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID, "MLM service", NotificationManager.IMPORTANCE_LOW));

            startForeground(NOTIFICATION_ID, notification);
        }
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public MadLocationManagerService addRawLocationListeners(LocationListener rawLocationListener) {
        madLocationManager.addRawLocationListeners(rawLocationListener);
        return this;
    }

    public MadLocationManagerService removeRawLocationListeners(LocationListener rawLocationListener) {
        madLocationManager.removeRawLocationListeners(rawLocationListener);
        return this;
    }

    public MadLocationManagerService addKalmanFilteredLocationListeners(LocationListener kalmanFilteredLocationListener) {
        madLocationManager.addKalmanFilteredLocationListeners(kalmanFilteredLocationListener);
        return this;
    }

    public MadLocationManagerService removeKalmanFilteredLocationListeners(LocationListener kalmanFilteredLocationListener) {
        madLocationManager.removeKalmanFilteredLocationListeners(kalmanFilteredLocationListener);
        return this;
    }

    public MadLocationManagerService addGeoHashFilteredLocationListeners(LocationListener geoHashFilteredLocationListener) {
        madLocationManager.addGeoHashFilteredLocationListeners(geoHashFilteredLocationListener);
        return this;
    }

    public MadLocationManagerService removeGeoHashFilteredLocationListeners(LocationListener geoHashFilteredLocationListener) {
        madLocationManager.removeGeoHashFilteredLocationListeners(geoHashFilteredLocationListener);
        return this;
    }

    /** Important! Before using MLM, make sure your application has the necessary permissions
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION}
     **/
    public void start() {
        madLocationManager.start();
    }

    public void stop() {
        madLocationManager.stop();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stop();
        stopSelf();
        stopForeground(true);
    }

    private Notification getDefaultNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("This application using MLM");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
        }

        return builder.build();
    }
}
