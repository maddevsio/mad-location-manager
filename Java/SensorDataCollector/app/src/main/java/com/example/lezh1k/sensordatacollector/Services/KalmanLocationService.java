package com.example.lezh1k.sensordatacollector.Services;

import android.Manifest;
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
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.lezh1k.sensordatacollector.CommonClasses.Coordinates;
import com.example.lezh1k.sensordatacollector.CommonClasses.GeoPoint;
import com.example.lezh1k.sensordatacollector.CommonClasses.SensorGpsDataItem;
import com.example.lezh1k.sensordatacollector.Filters.GPSAccKalmanFilter;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceInterface;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceStatusInterface;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by lezh1k on 1/11/18.
 */

public class KalmanLocationService extends Service
        implements SensorEventListener, LocationListener, GpsStatus.Listener {

    private static final String TAG = "KalmanLocationService";
    private static final long UpdateMinTime = 1000;
    private static final float UpdateMinDistance = 0;

    public static final int PermissionDenied = 0;
    public static final int StartLocationUpdates = 1;
    public static final int HaveLocation = 2;
    public static final int Paused = 3;

    private LocationManager m_locationManager;
    private List<LocationServiceInterface> m_locationServiceInterfaces;
    private List<LocationServiceStatusInterface> m_locationServiceStatusInterfaces;

    private boolean m_gpsEnabled = false;
    private boolean m_sensorsEnabled = false;

    private int m_serviceStatus = PermissionDenied;
    private int m_activeSatellites = 0;

    private float m_lastLocationAccuracy = 0;
    private GpsStatus m_gpsStatus;

    private Location m_lastLocation;
    private List<Location> m_track;

    /**/
    private GPSAccKalmanFilter m_kalmanFilter;
    private SensorDataEventLoopTask m_eventLoopTask;
    private List<Sensor> m_lstSensors;
    private SensorManager m_sensorManager;
    private boolean m_inProgress = false;
    private double m_magneticDeclination = 0.0;
    private double accDev = 1.0;

    /*accelerometer + rotation vector*/
    private float[] R = new float[16];
    private float[] RI = new float[16];
    private float[] accAxis = new float[4];
    private float[] linAcc = new float[4];
    /*gps*/

    private Queue<SensorGpsDataItem> m_sensorDataQueue =
            new PriorityBlockingQueue<SensorGpsDataItem>();

    class SensorDataEventLoopTask extends AsyncTask {
        boolean needTerminate = false;
        long deltaTMs;
        KalmanLocationService owner;
        SensorDataEventLoopTask(long deltaTMs, KalmanLocationService owner) {
            this.deltaTMs = deltaTMs;
            this.owner = owner;
        }

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
                while ((sdi = m_sensorDataQueue.poll()) != null) {
                    //warning!!!
                    if (sdi.getGpsLat() == SensorGpsDataItem.NOT_INITIALIZED) {
                        m_kalmanFilter.predict(sdi.getTimestamp(), sdi.getAbsEastAcc(), sdi.getAbsNorthAcc());
                    } else {
                        double xVel = sdi.getSpeed() * Math.cos(sdi.getCourse());
                        double yVel = sdi.getSpeed() * Math.sin(sdi.getCourse());
                        m_kalmanFilter.update(
                                sdi.getTimestamp(),
                                Coordinates.longitudeToMeters(sdi.getGpsLon()),
                                Coordinates.latitudeToMeters(sdi.getGpsLat()),
                                xVel,
                                yVel,
                                sdi.getPosErr(),
                                sdi.getVelErr());

                        Location loc = new Location(TAG);
                        GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(), m_kalmanFilter.getCurrentY());
                        loc.setLatitude(pp.Latitude);
                        loc.setLongitude(pp.Longitude);
                        loc.setAltitude(sdi.getGpsAlt());
                        xVel = m_kalmanFilter.getCurrentX();
                        yVel = m_kalmanFilter.getCurrentY();
                        double speed = Math.sqrt(xVel*xVel + yVel*yVel); //scalar speed without bearing
                        //todo calculate bearing!
                        loc.setSpeed((float) speed);
                        loc.setTime((long) sdi.getTimestamp());
                        loc.setAccuracy((float) sdi.getPosErr());
                        onLocationChangedImp(loc);
                    }
                }
            }
            return null;
        }

        void onLocationChangedImp(Location location) {
            Log.d(TAG, "onLocationChanged: " + location + " " + m_locationServiceInterfaces.size());

            if (location != null && location.getLatitude() != 0 &&
                    location.getLongitude() != 0 &&
                    location.getProvider().equals(TAG)) {

                m_serviceStatus = HaveLocation;
                m_lastLocation = location;
                m_lastLocationAccuracy = location.getAccuracy();

                if (ActivityCompat.checkSelfPermission(owner, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    m_gpsStatus = m_locationManager.getGpsStatus(m_gpsStatus);
                }

                int activeSatellites = 0;
                if (m_gpsStatus != null) {
                    for (GpsSatellite satellite : m_gpsStatus.getSatellites()) {
                        activeSatellites += satellite.usedInFix() ? 1 : 0;
                    }
                    m_activeSatellites = activeSatellites;
                }

                m_track.add(location);

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

//        @Override
//        protected void onProgressUpdate(Object... values) {
//        }
    }

    public KalmanLocationService() {
        m_locationServiceInterfaces = new ArrayList<>();
        m_locationServiceStatusInterfaces = new ArrayList<>();
        m_track = new ArrayList<>();
        m_lstSensors = new ArrayList<Sensor>();
        m_eventLoopTask = null;
        m_kalmanFilter = null;
    }

    public void reset() {
        m_kalmanFilter = null;
        m_track.clear();
    }

    public List<Location> getTrack() {
        return m_track;
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

    public void removeStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        m_locationServiceStatusInterfaces.remove(locationServiceStatusInterface);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m_locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void start() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            m_serviceStatus = PermissionDenied;
        } else {
            m_serviceStatus = StartLocationUpdates;
            m_locationManager.removeGpsStatusListener(this);
            m_locationManager.addGpsStatusListener(this);
            m_locationManager.removeUpdates(this);
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UpdateMinTime, UpdateMinDistance, this );
        }

        m_sensorsEnabled = true;
        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.unregisterListener(this, sensor);
            m_sensorsEnabled &= !m_sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        m_gpsEnabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
            ilss.serviceStatusChanged(m_serviceStatus);
            ilss.GPSEnabledChanged(m_gpsEnabled);
        }

        if (m_eventLoopTask != null) {
            m_eventLoopTask.cancel(true);
        }

        m_eventLoopTask = new SensorDataEventLoopTask(500, this);
        m_eventLoopTask.needTerminate = false;
        m_eventLoopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        m_sensorDataQueue.clear();
    }

    public void stop() {
        m_sensorDataQueue.clear();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            m_serviceStatus = PermissionDenied;
        } else {
            m_serviceStatus = Paused;
            m_locationManager.removeGpsStatusListener(this);
            m_locationManager.removeUpdates(this);
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
            m_eventLoopTask.cancel(true);
        }
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
        Log.d(TAG, "onTaskRemoved: " + rootIntent);

        m_locationServiceInterfaces.clear();
        m_locationServiceStatusInterfaces.clear();
        stopSelf();
    }

    /*SensorEventListener methods implementation*/
    @Override
    public void onSensorChanged(SensorEvent event) {
        final int east = 0;
        final int north = 1;
        final int up = 2;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linAcc, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(accAxis, 0, RI,
                        0, linAcc, 0);
                if (m_kalmanFilter == null)
                    break;
                long now = System.currentTimeMillis();
                SensorGpsDataItem sdi = new SensorGpsDataItem(now,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        accAxis[north],
                        accAxis[east],
                        accAxis[up],
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        m_magneticDeclination);
                m_sensorDataQueue.add(sdi);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(R, event.values);
                android.opengl.Matrix.invertM(RI, 0, R, 0);
                break;
        }
    }

//    private FusedLocationProviderClient mFusedLocationClient;
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*do nothing*/
        ;
    }

    /*LocationListener methods implementation*/
    @Override
    public void onLocationChanged(Location loc) {
        double x, y, xVel, yVel, posDev;
        long timeStamp;
        x = loc.getLongitude();
        y = loc.getLatitude();
        xVel = loc.getSpeed() * Math.cos(loc.getBearing());
        yVel = loc.getSpeed() * Math.sin(loc.getBearing());
        posDev = loc.getAccuracy();
        timeStamp = System.currentTimeMillis();

        if (m_kalmanFilter == null) {
            m_kalmanFilter = new GPSAccKalmanFilter(
                    Coordinates.longitudeToMeters(x),
                    Coordinates.latitudeToMeters(y),
                    xVel,
                    yVel,
                    accDev,
                    posDev,
                    timeStamp);
            return;
        }

        //WARNING!!! here should be speed accuracy, but loc.hasSpeedAccuracy()
        // and loc.getSpeedAccuracyMetersPerSecond() requare API 26
        double velErr = loc.getAccuracy() * 0.1;
        GeomagneticField f = new GeomagneticField(
                (float)loc.getLatitude(),
                (float)loc.getLongitude(),
                (float)loc.getAltitude(),
                timeStamp);
        m_magneticDeclination = f.getDeclination();

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
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            m_gpsEnabled = true;
            for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
                ilss.GPSEnabledChanged(m_gpsEnabled);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);

        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            m_gpsEnabled = false;
            for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
                ilss.GPSEnabledChanged(m_gpsEnabled);
            }
        }
    }

    /*GpsStatus.Listener implementation. do we really need this? */
    @Override
    public void onGpsStatusChanged(int event) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_gpsStatus = m_locationManager.getGpsStatus(m_gpsStatus);
        }

        int activeSatellites = 0;
        if (m_gpsStatus != null) {
            for (GpsSatellite satellite : m_gpsStatus.getSatellites()) {
                activeSatellites += satellite.usedInFix() ? 1 : 0;
            }

            if (activeSatellites != 0) {
                this.m_activeSatellites = activeSatellites;
                for (LocationServiceStatusInterface locationServiceStatusInterface : m_locationServiceStatusInterfaces) {
                    locationServiceStatusInterface.GPSStatusChanged(this.m_activeSatellites);
                }
            }
        }
    }
}
