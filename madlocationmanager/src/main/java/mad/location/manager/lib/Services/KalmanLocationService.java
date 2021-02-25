package mad.location.manager.lib.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.util.Log;

import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import mad.location.manager.lib.Commons.Coordinates;
import mad.location.manager.lib.Commons.GeoPoint;
import mad.location.manager.lib.Commons.SensorGpsDataItem;
import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.Filters.GPSAccKalmanFilter;
import mad.location.manager.lib.Interfaces.LocationServiceInterface;
import mad.location.manager.lib.Interfaces.LocationServiceStatusInterface;
import mad.location.manager.lib.Loggers.GeohashRTFilter;
import mad.location.manager.lib.locationProviders.GPSCallback;
import mad.location.manager.lib.locationProviders.GPSLocationProvider;
import mad.location.manager.lib.locationProviders.FusedLocationProvider;
import mad.location.manager.lib.locationProviders.LocationProviderCallback;

public class KalmanLocationService extends Service
        implements SensorEventListener, GPSCallback, LocationProviderCallback {

    public static final String TAG = "mlm:Service";

    //region Location service implementation. Maybe we need to move it to some abstract class?*/
    protected List<LocationServiceInterface> m_locationServiceInterfaces;
    protected List<LocationServiceStatusInterface> m_locationServiceStatusInterfaces;

    protected Location m_lastLocation;

    protected ServiceStatus m_serviceStatus = ServiceStatus.SERVICE_STOPPED;

    @Override
    public void locationAvailabilityChanged(boolean isLocationAvailable) {
        m_gpsEnabled = isLocationAvailable;
        for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
            ilss.GPSEnabledChanged(m_gpsEnabled);
        }
    }

    @Override
    public void onLocationAvailable(Location location) {
        processLocation(location);
    }

    @Override
    public void gpsSatelliteCountChanged(int noOfSatellites) {
        if (noOfSatellites != 0) {
            this.m_activeSatellites = noOfSatellites;
            for (LocationServiceStatusInterface locationServiceStatusInterface : m_locationServiceStatusInterfaces) {
                locationServiceStatusInterface.GPSStatusChanged(this.m_activeSatellites);
            }
        }
    }

    public enum ServiceStatus {
        PERMISSION_DENIED(0),
        SERVICE_STOPPED(1),
        SERVICE_STARTED(2),
        HAS_LOCATION(3),
        SERVICE_PAUSED(4);

        int value;

        ServiceStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public boolean isSensorsEnabled() {
        return m_sensorsEnabled;
    }

    public boolean IsRunning() {
        return m_serviceStatus != ServiceStatus.SERVICE_STOPPED && m_serviceStatus != ServiceStatus.SERVICE_PAUSED && m_sensorsEnabled;
    }

    public void addInterface(LocationServiceInterface locationServiceInterface) {
        if (m_locationServiceInterfaces.add(locationServiceInterface) && m_lastLocation != null) {
            locationServiceInterface.locationChanged(m_lastLocation);
        }
    }

    public void addInterfaces(List<LocationServiceInterface> locationServiceInterfaces) {
        if (m_locationServiceInterfaces.addAll(locationServiceInterfaces) && m_lastLocation != null) {
            for (LocationServiceInterface locationServiceInterface : locationServiceInterfaces) {
                locationServiceInterface.locationChanged(m_lastLocation);
            }
        }
    }

    public void removeInterface(LocationServiceInterface locationServiceInterface) {
        m_locationServiceInterfaces.remove(locationServiceInterface);
    }

    public void removeStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        m_locationServiceStatusInterfaces.remove(locationServiceStatusInterface);
    }

    public void addStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        if (m_locationServiceStatusInterfaces.add(locationServiceStatusInterface)) {
            locationServiceStatusInterface.serviceStatusChanged(m_serviceStatus);
            locationServiceStatusInterface.GPSStatusChanged(m_activeSatellites);
            locationServiceStatusInterface.GPSEnabledChanged(m_gpsEnabled);
            locationServiceStatusInterface.lastLocationAccuracyChanged(m_lastLocationAccuracy);
        }
    }

    public void addStatusInterfaces(List<LocationServiceStatusInterface> locationServiceStatusInterfaces) {
        if (m_locationServiceStatusInterfaces.addAll(locationServiceStatusInterfaces)) {
            for (LocationServiceStatusInterface locationServiceStatusInterface : locationServiceStatusInterfaces) {
                locationServiceStatusInterface.serviceStatusChanged(m_serviceStatus);
                locationServiceStatusInterface.GPSStatusChanged(m_activeSatellites);
                locationServiceStatusInterface.GPSEnabledChanged(m_gpsEnabled);
                locationServiceStatusInterface.lastLocationAccuracyChanged(m_lastLocationAccuracy);
            }
        }
    }

    public Location getLastLocation() {
        return m_lastLocation;
    }

    /*Service implementation*/
    public class LocalBinder extends Binder {
        public KalmanLocationService getService() {
            return KalmanLocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stop();
        Log.d(TAG, "onTaskRemoved: " + rootIntent);
        m_locationServiceInterfaces.clear();
        m_locationServiceStatusInterfaces.clear();
        stopSelf();
    }
    //endregion

    private GeohashRTFilter m_geoHashRTFilter = null;

    public GeohashRTFilter getGeoHashRTFilter() {
        return m_geoHashRTFilter;
    }

    public static Settings defaultSettings =
            new Settings(
                    Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                    Utils.GPS_MIN_DISTANCE,
                    Utils.GPS_MIN_TIME,
                    Utils.SENSOR_POSITION_MIN_TIME,
                    Utils.GEOHASH_DEFAULT_PREC,
                    Utils.GEOHASH_DEFAULT_MIN_POINT_COUNT,
                    Utils.SENSOR_DEFAULT_FREQ_HZ,
                    null,
                    true,
                    true,
                    false,
                    Utils.DEFAULT_VEL_FACTOR,
                    Utils.DEFAULT_POS_FACTOR,
                    Settings.LocationProvider.GPS
            );

    private Settings m_settings;

    FusedLocationProvider fusedLocationProvider;
    GPSLocationProvider gpsLocationProvider;
    private PowerManager m_powerManager;
    private PowerManager.WakeLock m_wakeLock;

    private boolean m_gpsEnabled = false;
    private boolean m_sensorsEnabled = false;

    private int m_activeSatellites = 0;
    private float m_lastLocationAccuracy = 0;
    /**/
    private GPSAccKalmanFilter m_kalmanFilter;
    private SensorDataEventLoopTask m_eventLoopTask;
    private List<Sensor> m_lstSensors;
    private SensorManager m_sensorManager;
    private double m_magneticDeclination = 0.0;

    /*accelerometer + rotation vector*/
    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };

    private float[] rotationMatrix = new float[16];
    private float[] rotationMatrixInv = new float[16];
    private float[] absAcceleration = new float[4];
    private float[] linearAcceleration = new float[4];
    //!

    private Queue<SensorGpsDataItem> m_sensorDataQueue =
            new PriorityBlockingQueue<>();

    private final HandlerThread thread = new HandlerThread("kalmanThread");

    private void log2File(String format, Object... args) {
        if (m_settings.logger != null)
            m_settings.logger.log2file(format, args);
    }

    class SensorDataEventLoopTask extends AsyncTask {
        boolean needTerminate = false;
        long deltaTMs;
        KalmanLocationService owner;

        SensorDataEventLoopTask(long deltaTMs, KalmanLocationService owner) {
            this.deltaTMs = deltaTMs;
            this.owner = owner;
        }

        private void handlePredict(SensorGpsDataItem sdi) {
            log2File("%d%d KalmanPredict : accX=%f, accY=%f",
                    Utils.LogMessageType.KALMAN_PREDICT.ordinal(),
                    (long) sdi.getTimestamp(),
                    sdi.getAbsEastAcc(),
                    sdi.getAbsNorthAcc());
            m_kalmanFilter.predict(sdi.getTimestamp(), sdi.getAbsEastAcc(), sdi.getAbsNorthAcc());
        }

        private void handleUpdate(SensorGpsDataItem sdi) {
            double xVel = sdi.getSpeed() * Math.cos(sdi.getCourse());
            double yVel = sdi.getSpeed() * Math.sin(sdi.getCourse());
            log2File("%d%d KalmanUpdate : pos lon=%f, lat=%f, xVel=%f, yVel=%f, posErr=%f, velErr=%f",
                    Utils.LogMessageType.KALMAN_UPDATE.ordinal(),
                    (long) sdi.getTimestamp(),
                    sdi.getGpsLon(),
                    sdi.getGpsLat(),
                    xVel,
                    yVel,
                    sdi.getPosErr(),
                    sdi.getVelErr()
            );

            m_kalmanFilter.update(
                    sdi.getTimestamp(),
                    Coordinates.longitudeToMeters(sdi.getGpsLon()),
                    Coordinates.latitudeToMeters(sdi.getGpsLat()),
                    xVel,
                    yVel,
                    sdi.getPosErr(),
                    sdi.getVelErr()
            );
        }

        private Location locationAfterUpdateStep(SensorGpsDataItem sdi) {
            double xVel, yVel;
            Location loc = new Location(TAG);
            GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(),
                    m_kalmanFilter.getCurrentY());
            loc.setLatitude(pp.Latitude);
            loc.setLongitude(pp.Longitude);
            loc.setAltitude(sdi.getGpsAlt());
            xVel = m_kalmanFilter.getCurrentXVel();
            yVel = m_kalmanFilter.getCurrentYVel();
            double speed = Math.sqrt(xVel * xVel + yVel * yVel); //scalar speed without bearing
            loc.setBearing((float) sdi.getCourse());
            loc.setSpeed((float) speed);
            loc.setTime(System.currentTimeMillis());
            loc.setElapsedRealtimeNanos(System.nanoTime());
            loc.setAccuracy((float) sdi.getPosErr());

            if (m_geoHashRTFilter != null) {
                m_geoHashRTFilter.filter(loc);
            }

            return loc;
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected Object doInBackground(Object[] objects) {
            while (!needTerminate) {
                try {
                    Thread.sleep(deltaTMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue; //bad
                }

                SensorGpsDataItem sdi;
                double lastTimeStamp = 0.0;
                while ((sdi = m_sensorDataQueue.poll()) != null) {
                    if (sdi.getTimestamp() < lastTimeStamp) {
                        continue;
                    }
                    lastTimeStamp = sdi.getTimestamp();

                    //warning!!!
                    if (sdi.getGpsLat() == SensorGpsDataItem.NOT_INITIALIZED) {
                        handlePredict(sdi);
                    } else {
                        handleUpdate(sdi);
                        Location loc = locationAfterUpdateStep(sdi);
                        publishProgress(loc);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            onLocationChangedImp((Location) values[0]);
        }

        void onLocationChangedImp(Location location) {
            if (location == null || location.getLatitude() == 0 ||
                    location.getLongitude() == 0 ||
                    !location.getProvider().equals(TAG)) {
                return;
            }
            m_serviceStatus = ServiceStatus.HAS_LOCATION;
            m_lastLocation = location;
            m_lastLocationAccuracy = location.getAccuracy();
            for (LocationServiceInterface locationServiceInterface : m_locationServiceInterfaces) {
                locationServiceInterface.locationChanged(location);
            }
            for (LocationServiceStatusInterface locationServiceStatusInterface : m_locationServiceStatusInterfaces) {
                locationServiceStatusInterface.serviceStatusChanged(m_serviceStatus);
                locationServiceStatusInterface.lastLocationAccuracyChanged(m_lastLocationAccuracy);
                if (m_settings.provider == Settings.LocationProvider.GPS) {
                    m_activeSatellites = gpsLocationProvider.getGPSSatteliteCount();
                    locationServiceStatusInterface.GPSStatusChanged(m_activeSatellites);
                }
            }
        }
    }

    public KalmanLocationService() {
        m_locationServiceInterfaces = new ArrayList<>();
        m_locationServiceStatusInterfaces = new ArrayList<>();
        m_lstSensors = new ArrayList<Sensor>();
        m_eventLoopTask = null;
        reset(defaultSettings);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationProvider = new FusedLocationProvider(this,this);
        gpsLocationProvider = new GPSLocationProvider(this, this, this);
        m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        m_powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        m_wakeLock = m_powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        if (m_sensorManager == null) {
            m_sensorsEnabled = false;
            return; //todo handle somehow
        }

        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.d(TAG, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }
    }

    @Override
    public void onDestroy() {
        thread.quitSafely();
        super.onDestroy();
    }

    public void start() {
        m_wakeLock.acquire();
        m_sensorDataQueue.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            m_serviceStatus = ServiceStatus.PERMISSION_DENIED;
        } else {
            m_serviceStatus = ServiceStatus.SERVICE_STARTED;
            if (m_settings.provider == Settings.LocationProvider.GPS) {
                gpsLocationProvider.startLocationUpdates(m_settings, thread);
                m_gpsEnabled = gpsLocationProvider.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } else {
                fusedLocationProvider.startLocationUpdates(m_settings, thread);
                m_gpsEnabled = fusedLocationProvider.isProviderEnabled();
            }
            startEventLoop(m_gpsEnabled);

        }
        m_sensorsEnabled = true;
        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.unregisterListener(this, sensor);
            m_sensorsEnabled &= !m_sensorManager.registerListener(this, sensor,
                    Utils.hertz2periodUs(m_settings.sensorFrequencyHz));
        }


    }

    private void startEventLoop(boolean m_gpsEnabled) {
        for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
            ilss.serviceStatusChanged(m_serviceStatus);
            ilss.GPSEnabledChanged(m_gpsEnabled);
        }

        m_eventLoopTask = new SensorDataEventLoopTask(m_settings.positionMinTime, KalmanLocationService.this);
        m_eventLoopTask.needTerminate = false;
        m_eventLoopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stop() {
        if (m_wakeLock.isHeld())
            m_wakeLock.release();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            m_serviceStatus = ServiceStatus.SERVICE_STOPPED;
        } else {
            m_serviceStatus = ServiceStatus.SERVICE_PAUSED;
            if (m_settings.provider == Settings.LocationProvider.GPS) {
                gpsLocationProvider.stop();
            } else {
                fusedLocationProvider.stop();
            }
        }

        if (m_geoHashRTFilter != null) {
            m_geoHashRTFilter.stop();
        }

        m_sensorsEnabled = false;
        m_gpsEnabled = false;
        for (Sensor sensor : m_lstSensors)
            m_sensorManager.unregisterListener(this, sensor);

        for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
            ilss.serviceStatusChanged(m_serviceStatus);
            ilss.GPSEnabledChanged(m_gpsEnabled);
        }

        if (m_eventLoopTask != null) {
            m_eventLoopTask.needTerminate = true;
            m_eventLoopTask.cancel(true);
        }
        m_sensorDataQueue.clear();
    }

    public void reset(Settings settings) {

        m_settings = settings;
        m_kalmanFilter = null;

        if (m_settings.geoHashPrecision != 0 &&
                m_settings.geoHashMinPointCount != 0) {
            m_geoHashRTFilter = new GeohashRTFilter(m_settings.geoHashPrecision,
                    m_settings.geoHashMinPointCount);
        }
    }

    /*SensorEventListener methods implementation*/
    @Override
    public void onSensorChanged(SensorEvent event) {
        final int east = 0;
        final int north = 1;
        final int up = 2;

        long now = android.os.SystemClock.elapsedRealtimeNanos();
        long nowMs = Utils.nano2milli(now);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linearAcceleration, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(absAcceleration, 0, rotationMatrixInv,
                        0, linearAcceleration, 0);

                String logStr = String.format(Locale.ENGLISH, "%d%d abs acc: %f %f %f",
                        Utils.LogMessageType.ABS_ACC_DATA.ordinal(),
                        nowMs, absAcceleration[east], absAcceleration[north], absAcceleration[up]);
                log2File(logStr);

                if (m_kalmanFilter == null) {
                    break;
                }

                SensorGpsDataItem sdi = new SensorGpsDataItem(nowMs,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        absAcceleration[north],
                        absAcceleration[east],
                        absAcceleration[up],
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        m_magneticDeclination);
                m_sensorDataQueue.add(sdi);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                android.opengl.Matrix.invertM(rotationMatrixInv, 0, rotationMatrix, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*do nothing*/
    }

    private void processLocation(Location loc) {
        if (loc == null) return;
        if (m_settings.filterMockGpsCoordinates && loc.isFromMockProvider()) return;

        double x, y, xVel, yVel, posDev, course, speed;
        long timeStamp;
        speed = loc.getSpeed();
        course = loc.getBearing();
        x = loc.getLongitude();
        y = loc.getLatitude();
        xVel = speed * Math.cos(course);
        yVel = speed * Math.sin(course);
        posDev = loc.getAccuracy();
        timeStamp = Utils.nano2milli(loc.getElapsedRealtimeNanos());
        //WARNING!!! here should be speed accuracy, but loc.hasSpeedAccuracy()
        // and loc.getSpeedAccuracyMetersPerSecond() requares API 26
        double velErr = loc.getAccuracy() * 0.1;

        String logStr = String.format(Locale.ENGLISH, "%d%d GPS : pos lat=%f, lon=%f, alt=%f, hdop=%f, speed=%f, bearing=%f, sa=%f",
                Utils.LogMessageType.GPS_DATA.ordinal(),
                timeStamp, loc.getLatitude(),
                loc.getLongitude(), loc.getAltitude(), loc.getAccuracy(),
                loc.getSpeed(), loc.getBearing(), velErr);
        log2File(logStr);

        GeomagneticField f = new GeomagneticField(
                (float) loc.getLatitude(),
                (float) loc.getLongitude(),
                (float) loc.getAltitude(),
                timeStamp);
        m_magneticDeclination = f.getDeclination();

        if (m_kalmanFilter == null) {
            log2File("%d%d KalmanAlloc : lon=%f, lat=%f, speed=%f, course=%f, m_accDev=%f, posDev=%f",
                    Utils.LogMessageType.KALMAN_ALLOC.ordinal(),
                    timeStamp, x, y, speed, course, m_settings.accelerationDeviation, posDev);
            m_kalmanFilter = new GPSAccKalmanFilter(
                    m_settings.useGpsSpeed,
                    Coordinates.longitudeToMeters(x),
                    Coordinates.latitudeToMeters(y),
                    xVel,
                    yVel,
                    m_settings.accelerationDeviation,
                    posDev,
                    timeStamp,
                    m_settings.mVelFactor,
                    m_settings.mPosFactor);
            return;
        }

        SensorGpsDataItem sdi = new SensorGpsDataItem(
                timeStamp, loc.getLatitude(), loc.getLongitude(), loc.getAltitude(),
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                loc.getSpeed(),
                loc.getBearing(),
                loc.getAccuracy(),
                velErr,
                m_magneticDeclination);
        m_sensorDataQueue.add(sdi);
    }
}
