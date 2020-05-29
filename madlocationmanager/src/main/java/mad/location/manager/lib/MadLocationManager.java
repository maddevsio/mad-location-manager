package mad.location.manager.lib;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mad.location.manager.lib.Interfaces.LocationListener;
import mad.location.manager.lib.Interfaces.SimpleTempCallback;
import mad.location.manager.lib.location.LocationCallback;
import mad.location.manager.lib.location.LocationDataManager;
import mad.location.manager.lib.sensor.SensorCallback;
import mad.location.manager.lib.sensor.SensorDataManager;
import mad.location.manager.lib.logger.Logger;
import mad.location.manager.lib.utils.Settings;

public class MadLocationManager implements LocationCallback, SensorCallback, SimpleTempCallback<Location> {
    private static final String TAG = "MadLocationManager";
    public static final String MLM_PROVIDER = "MLM_PROVIDER";

    private Settings settings;
    private LocationDataManager locationDataManager;
    private SensorDataManager sensorDataManager;
    private MadDataHandler handler;

    private Location lastRawLocation;
    private Location lastKalmanFilteredLocation;
    private Location lastGeoHashFilteredLocation;

    private List<LocationListener> rawLocationListeners;
    private List<LocationListener> kalmanFilteredLocationListeners;
    private List<LocationListener> geoHashFilteredLocationListeners;

    private boolean started;

    public MadLocationManager(Context context) {
        this(context, new Settings());
    }

    public MadLocationManager(Context context, Settings settings) {
        locationDataManager = new LocationDataManager(context)
                .setCallback(this);

        sensorDataManager = new SensorDataManager(context)
                .setCallback(this);

        handler = new MadDataHandler()
                .setCallback(this);

        Logger.init(context);

        updateSettings(settings);

        rawLocationListeners = new ArrayList<>();
        kalmanFilteredLocationListeners = new ArrayList<>();
        geoHashFilteredLocationListeners = new ArrayList<>();

        started = false;
    }

    public Settings getSettings() {
        return settings;
    }

    public void updateSettings(Settings settings) {
        this.settings = settings;

        locationDataManager.setGpsMinTime(settings.getGpsMinTime())
                .setGpsMinDistance(settings.getGpsMinDistance())
                .setFilterMockGpsCoordinates(settings.isFilterMockGpsCoordinates());

        sensorDataManager.setSensorFrequencyHz(settings.getSensorFrequencyHz());

        handler.setAccelerationDeviation(settings.getAccelerationDeviation())
                .setVelFactor(settings.getVelFactor())
                .setPosFactor(settings.getPosFactor())
                .setPositionMinTime(settings.getPositionMinTime());

        Logger.setEnabled(settings.isLoggerEnabled());
    }

    public MadLocationManager addRawLocationListeners(LocationListener rawLocationListener) {
        if (rawLocationListener != null && rawLocationListeners.add(rawLocationListener) && lastRawLocation != null) {
            rawLocationListener.onLocationChanged(lastRawLocation);
        }
        return this;
    }

    public MadLocationManager removeRawLocationListeners(LocationListener rawLocationListener) {
        rawLocationListeners.remove(rawLocationListener);
        return this;
    }

    public MadLocationManager addKalmanFilteredLocationListeners(LocationListener kalmanFilteredLocationListener) {
        if (kalmanFilteredLocationListener != null && kalmanFilteredLocationListeners.add(kalmanFilteredLocationListener) && lastKalmanFilteredLocation != null) {
            kalmanFilteredLocationListener.onLocationChanged(lastKalmanFilteredLocation);
        }
        return this;
    }

    public MadLocationManager removeKalmanFilteredLocationListeners(LocationListener kalmanFilteredLocationListener) {
        kalmanFilteredLocationListeners.remove(kalmanFilteredLocationListener);
        return this;
    }

    public MadLocationManager addGeoHashFilteredLocationListeners(LocationListener geoHashFilteredLocationListener) {
        if (geoHashFilteredLocationListener != null && geoHashFilteredLocationListeners.add(geoHashFilteredLocationListener) && lastGeoHashFilteredLocation != null) {
            geoHashFilteredLocationListener.onLocationChanged(lastGeoHashFilteredLocation);
        }
        return this;
    }

    public MadLocationManager removeGeoHashFilteredLocationListeners(LocationListener geoHashFilteredLocationListener) {
        geoHashFilteredLocationListeners.remove(geoHashFilteredLocationListener);
        return this;
    }

    /** Important! Before using MLM, make sure your application has the necessary permissions
     * {@link android.Manifest.permission#ACCESS_FINE_LOCATION}
     **/
    public void start() {
        if (!started && locationDataManager.start()) {
            if (sensorDataManager.start()) {
                Log.d(TAG, "MLM started successfully!");
            } else {
                Log.d(TAG, "MLM started without sensor data filter");
            }

            handler.start();

            started = true;
        } else {
            Log.e(TAG, "MLM didn't started");
        }
    }

    public void stop() {
        locationDataManager.stop();
        sensorDataManager.stop();
        handler.stop();

        started = false;
    }

    @Override
    public void locationChanged(Location location) {//raw gps location data
        lastRawLocation = location;

        handler.locationChanged(location);

        Iterator<LocationListener> iterator = rawLocationListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onLocationChanged(location);
        }
    }

    @Override
    public void GPSStatusChanged(int activeSatellites) {

    }

    @Override
    public void GPSEnabledChanged(boolean enabled) {

    }

    @Override
    public void onABSAccelerationChanged(float[] absAcceleration) {
        handler.absAccelerationChanged(absAcceleration);
    }

    @Override
    public void onCall(Location location) {//Filtered location data
        lastKalmanFilteredLocation = location;

        //add geoHash filter

        Iterator<LocationListener> iterator = kalmanFilteredLocationListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onLocationChanged(location);
        }
    }
}
