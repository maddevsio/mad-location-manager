package com.example.lezh1k.sensordatacollector.Loggers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.example.lezh1k.sensordatacollector.Commons;
import com.example.lezh1k.sensordatacollector.Filters.Coordinates;
import com.example.lezh1k.sensordatacollector.Filters.GPSAccKalmanFilter;
import com.example.lezh1k.sensordatacollector.Filters.GeoHash;
import com.example.lezh1k.sensordatacollector.Filters.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by lezh1k on 1/8/18.
 */

public class KalmanDistanceLogger implements SensorEventListener, LocationListener {

    private GPSAccKalmanFilter m_kalmanFilter = null;
    private SensorDataEventLoopTask m_eventLoopTask = null;
    private List<Sensor> m_lstSensors = new ArrayList<Sensor>();
    private SensorManager m_sensorManager;
    private boolean m_inProgress = false;
    double m_distance = 0.0;

    /*accelerometer + rotation vector*/
    private float[] R = new float[16];
    private float[] RI = new float[16];
    private float[] accAxis = new float[4];
    private float[] linAcc = new float[4];

    /*gps*/
    private LocationManager m_locationManager = null;
    private Context m_context = null;

    final double accDev = 1.0;

    class SensorGpsDataItem implements Comparable<SensorGpsDataItem> {
        double timestamp;
        double gpsLat;
        double gpsLon;
        double gpsAlt;
        double absNorthAcc;
        double absEastAcc;
        double absUpAcc;
        double speed;
        double course;
        double posErr;
        double velErr;

        static final double NOT_INITIALIZED = -1.0;

        public SensorGpsDataItem(double timestamp,
                                 double gpsLat, double gpsLon, double gpsAlt,
                                 double absNorthAcc, double absEastAcc, double absUpAcc,
                                 double speed, double course,
                                 double posErr, double velErr) {
            this.timestamp = timestamp;
            this.gpsLat = gpsLat;
            this.gpsLon = gpsLon;
            this.gpsAlt = gpsAlt;
            this.absNorthAcc = absNorthAcc;
            this.absEastAcc = absEastAcc;
            this.absUpAcc = absUpAcc;
            this.speed = speed;
            this.course = course;
            this.posErr = posErr;
            this.velErr = velErr;
        }

        @Override
        public int compareTo(@NonNull SensorGpsDataItem o) {
            return (int) (this.timestamp - o.timestamp);
        }
    }

    private Queue<SensorGpsDataItem> m_sensorDataQueue =
            new PriorityBlockingQueue<SensorGpsDataItem>();

    private String m_lastResultsString = "No info";
    public String getLastResultsString() {
        return m_lastResultsString;
    }

    class SensorDataEventLoopTask extends AsyncTask {

        boolean needTerminate = false;
        long deltaTMs;
        double llat, llon;

        SensorDataEventLoopTask(long deltaTMs) {
            this.deltaTMs = deltaTMs;
        }

        private void calculateDistanceStep() {
            GeoPoint pp = Coordinates.MetersToGeoPoint(
                    m_kalmanFilter.getCurrentX(),
                    m_kalmanFilter.getCurrentY());

            String geo0, geo1;
            final int precision = 7;
            geo0 = GeoHash.Encode(llat, llon, precision);
            geo1 = GeoHash.Encode(pp.Latitude, pp.Longitude, precision);

            if (geo0.equals(geo1))
                return;

            double dd = Coordinates.geoDistanceMeters(llon, llat,
                    pp.Longitude, pp.Latitude);

            m_distance += dd;
            llat = pp.Latitude;
            llon = pp.Longitude;
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
                    if (sdi.gpsLat == SensorGpsDataItem.NOT_INITIALIZED) {
                        m_kalmanFilter.predict(sdi.timestamp, sdi.absEastAcc, sdi.absNorthAcc);
                    } else {
                        double xVel = sdi.speed * Math.cos(sdi.course);
                        double yVel = sdi.speed * Math.sin(sdi.course);
                        m_kalmanFilter.update(
                                sdi.timestamp,
                                Coordinates.LongitudeToMeters(sdi.gpsLon),
                                Coordinates.LatitudeToMeters(sdi.gpsLat),
                                xVel,
                                yVel,
                                sdi.posErr,
                                sdi.velErr);
                        calculateDistanceStep();
                    }
                }
            }
            return null;
        }
    }

    public KalmanDistanceLogger(SensorManager sensorManager,
                                LocationManager locationManager,
                                Context context) {
        int[] sensorTypes = {
                Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_ROTATION_VECTOR };

        this.m_sensorManager = sensorManager;
        this.m_locationManager = locationManager;
        this.m_context = context;

        m_sensorManager = sensorManager;
        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.e(Commons.AppName, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }
    }

    public boolean start() {
        if (m_locationManager == null)
            return false;

        if (ActivityCompat.checkSelfPermission(m_context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        m_locationManager.removeUpdates(this);
        final int gpsMinTime = 1000;
        final int gpsMinDistance = 0;
        m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                gpsMinTime, gpsMinDistance, this);

        for (Sensor sensor : m_lstSensors) {
            if (!m_sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME)) {
                XLog.e("Couldn't register listener : %d", sensor.getType());
                return false;
            }
        }

        if (m_eventLoopTask != null) {
            m_eventLoopTask.cancel(true);
        }

        m_eventLoopTask = new SensorDataEventLoopTask(500);
        m_eventLoopTask.needTerminate = false;
        m_eventLoopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        m_sensorDataQueue.clear();
        m_distance = 0.0;
        return m_inProgress = true;
    }

    public void stop() {
        if (m_locationManager != null) {
            m_locationManager.removeUpdates(this);
        }

        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.unregisterListener(this, sensor);
        }

        m_inProgress = false;

        if (m_eventLoopTask != null) {
            m_eventLoopTask.needTerminate = true;
        }
    }

    public double getDistance() {
        return m_distance;
    }

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
                /*todo use magnetic declination for acceleration course correction*/
                if (m_kalmanFilter != null) {
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
                            SensorGpsDataItem.NOT_INITIALIZED);
                    m_sensorDataQueue.add(sdi);
                }
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(R, event.values);
                android.opengl.Matrix.invertM(RI, 0, R, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*do nothing*/
    }

    @Override
    public void onLocationChanged(Location loc) {

        double x, y, xVel, yVel, posDev, timeStamp;
        x = loc.getLongitude();
        y = loc.getLatitude();
        xVel = loc.getSpeed() * Math.cos(loc.getBearing());
        yVel = loc.getSpeed() * Math.sin(loc.getBearing());
        posDev = loc.getAccuracy();
        timeStamp = System.currentTimeMillis();

        if (m_kalmanFilter == null) {
            m_kalmanFilter = new GPSAccKalmanFilter(
                    Coordinates.LongitudeToMeters(x),
                    Coordinates.LatitudeToMeters(y),
                    xVel,
                    yVel,
                    accDev,
                    posDev,
                    timeStamp);

            if (m_eventLoopTask != null) {
                m_eventLoopTask.llat = y;
                m_eventLoopTask.llon = x; //hack
            }
            return;
        }

        //WARNING!!! here should be speed accuracy, but min api level 26 for loc.hasSpeedAccuracy()
        //and loc.getSpeedAccuracyMetersPerSecond()
        double velErr = loc.getAccuracy() * 0.1;
        SensorGpsDataItem sdi = new SensorGpsDataItem(
          timeStamp, loc.getLatitude(), loc.getLongitude(), loc.getAltitude(),
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                loc.getSpeed(),
                loc.getBearing(),
                loc.getAccuracy(),
                velErr
        );
        m_sensorDataQueue.add(sdi);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        /*todo something*/
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (!provider.equals(LocationManager.GPS_PROVIDER))
            return;

        if (m_inProgress) {
            start();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (!provider.equals(LocationManager.GPS_PROVIDER))
            return;
        //!!! don't predict without GPS because of huge integration error related to accelerometer
        if (m_inProgress) {
            stop();
        }
    }
    ///////////////////////////////////////////////////////////////
}
