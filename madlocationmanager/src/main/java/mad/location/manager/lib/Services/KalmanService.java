package mad.location.manager.lib.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import mad.location.manager.lib.Commons.Coordinates;
import mad.location.manager.lib.Commons.GeoPoint;
import mad.location.manager.lib.Commons.SensorGpsDataItem;
import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.Filters.GPSAccKalmanFilter;
import mad.location.manager.lib.Interfaces.ILocationDataProvider;
import mad.location.manager.lib.Interfaces.ISensorDataProvider;
import mad.location.manager.lib.Interfaces.LocationServiceInterface;
import mad.location.manager.lib.Interfaces.LocationServiceStatusInterface;
import mad.location.manager.lib.Loggers.GeohashRTFilter;
import mad.location.manager.lib.Provider.LocationDataProvider;
import mad.location.manager.lib.Provider.SensorDataProvider;

public class KalmanService extends Service implements ISensorDataProvider.Client, ILocationDataProvider.Client {

    public static final String TAG = KalmanService.class.getName();

    public static Settings m_settings;
    private ISensorDataProvider.Provider sensorProvider;
    private ILocationDataProvider.Provider locationProvider;

    private GpsStatus m_gpsStatus;

    protected List<LocationServiceInterface> m_locationServiceInterfaces;
    protected List<LocationServiceStatusInterface> m_locationServiceStatusInterfaces;

    public static ServiceStatus m_serviceStatus = ServiceStatus.SERVICE_STOPPED;

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

    private GPSAccKalmanFilter m_kalmanFilter;
    private SensorDataEventLoopTask m_eventLoopTask;
    private List<Sensor> m_lstSensors;
    private double m_magneticDeclination = 0.0;

    class SensorDataEventLoopTask extends AsyncTask {
        boolean needTerminate = false;
        long deltaTMs;
        KalmanService owner;

        SensorDataEventLoopTask(long deltaTMs, KalmanService owner) {
            this.deltaTMs = deltaTMs;
            this.owner = owner;
        }

        private void handlePredict(SensorGpsDataItem sdi) {
            m_kalmanFilter.predict(sdi.getTimestamp(), sdi.getAbsEastAcc(), sdi.getAbsNorthAcc());
        }

        private void handleUpdate(SensorGpsDataItem sdi) {
            double xVel = sdi.getSpeed() * Math.cos(sdi.getCourse());
            double yVel = sdi.getSpeed() * Math.sin(sdi.getCourse());
            m_kalmanFilter.update(sdi.getTimestamp(), Coordinates.longitudeToMeters(sdi.getGpsLon()), Coordinates.latitudeToMeters(sdi.getGpsLat()), xVel, yVel, sdi.getPosErr(), sdi.getVelErr()
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

            if (ActivityCompat.checkSelfPermission(owner, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                m_gpsStatus = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).getGpsStatus(m_gpsStatus);
            }

            int activeSatellites = 0;
            if (m_gpsStatus != null) {
                for (GpsSatellite satellite : m_gpsStatus.getSatellites()) {
                    activeSatellites += satellite.usedInFix() ? 1 : 0;
                }
                m_activeSatellites = activeSatellites;
            }

            for (LocationServiceInterface locationServiceInterface : m_locationServiceInterfaces) {
                locationServiceInterface.locationChanged(location);
            }
            for (LocationServiceStatusInterface locationServiceStatusInterface : m_locationServiceStatusInterfaces) {
                locationServiceStatusInterface.serviceStatusChanged(m_serviceStatus);
                locationServiceStatusInterface.lastLocationAccuracyChanged(m_lastLocationAccuracy);
                locationServiceStatusInterface.GPSStatusChanged(m_activeSatellites);
            }

        }
    }

    private Queue<SensorGpsDataItem> m_sensorDataQueue =
            new PriorityBlockingQueue<>();

    private int m_activeSatellites = 0;
    private float m_lastLocationAccuracy = 0;

    private PowerManager m_powerManager;
    private PowerManager.WakeLock m_wakeLock;

    private boolean m_gpsEnabled = false;
    private boolean m_sensorsEnabled = false;
    protected Location m_lastLocation;
    private GeohashRTFilter m_geoHashRTFilter = null;

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
                    Utils.DEFAULT_VEL_FACTOR,
                    Utils.DEFAULT_POS_FACTOR
            );

    public void reset(Settings settings) {
        m_settings = settings;
        m_kalmanFilter = null;

        if (m_settings.geoHashPrecision != 0 &&
                m_settings.geoHashMinPointCount != 0) {
            m_geoHashRTFilter = new GeohashRTFilter(m_settings.geoHashPrecision,
                    m_settings.geoHashMinPointCount);
        }
    }

    public KalmanService() {
        m_locationServiceInterfaces = new ArrayList<>();
        m_locationServiceStatusInterfaces = new ArrayList<>();
        m_lstSensors = new ArrayList<Sensor>();
        m_eventLoopTask = null;
        reset(defaultSettings);
    }

    public void start() {
        m_wakeLock.acquire();
        m_sensorDataQueue.clear();
        m_sensorsEnabled = true;
        m_gpsEnabled = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
        sensorProvider.start();
        locationProvider.start(defaultSettings, m_serviceStatus);

        m_eventLoopTask = new SensorDataEventLoopTask(m_settings.positionMinTime, this);
        m_eventLoopTask.needTerminate = false;
        m_eventLoopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stop() {
        sensorProvider.stop();
        locationProvider.stop(m_serviceStatus);

        if (m_wakeLock.isHeld())
            m_wakeLock.release();

        if (m_geoHashRTFilter != null) {
            m_geoHashRTFilter.stop();
        }

        m_sensorsEnabled = false;
        m_gpsEnabled = false;

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

    @Override
    public void onCreate() {
        super.onCreate();
        m_powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        m_wakeLock = m_powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        sensorProvider = new SensorDataProvider(this, this);
        locationProvider = new LocationDataProvider(this, this);
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

    public class LocalBinder extends Binder {
        public KalmanService getService() {
            return KalmanService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void absAccelerationDate(float[] absAcceleration) {
        final int east = 0;
        final int north = 1;
        final int up = 2;

        long now = android.os.SystemClock.elapsedRealtimeNanos();
        long nowMs = Utils.nano2milli(now);

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
    }

    @Override
    public void rotationMatrixInv(float[] rotationMatrixInv, float[] rotationMatrix) {

    }

    @Override
    public void onLocationChanged(Location loc) {
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

        GeomagneticField f = new GeomagneticField(
                (float)loc.getLatitude(),
                (float)loc.getLongitude(),
                (float)loc.getAltitude(),
                timeStamp);
        m_magneticDeclination = f.getDeclination();

        if (m_kalmanFilter == null) {
            m_kalmanFilter = new GPSAccKalmanFilter(
                    false, //todo move to settings
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

    @Override
    public void onProviderEnabled(GpsStatus gpsStatus, boolean gpsEnable) {
        m_gpsEnabled = true;
        for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
            ilss.GPSEnabledChanged(m_gpsEnabled);
        }
    }

    @Override
    public void onProviderDisabled(GpsStatus gpsStatus, boolean gpsEnable) {
        m_gpsEnabled = false;
        for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
            ilss.GPSEnabledChanged(m_gpsEnabled);
        }
    }

    @Override
    public void activeSatellites(int satellite) {
        this.m_activeSatellites = satellite;
        for (LocationServiceStatusInterface locationServiceStatusInterface : m_locationServiceStatusInterfaces) {
            locationServiceStatusInterface.GPSStatusChanged(this.m_activeSatellites);
        }
    }
}
