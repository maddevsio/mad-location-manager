package com.example.lezh1k.sensordatacollector.Services;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy;
import com.elvishew.xlog.printer.file.naming.FileNameGenerator;
import com.example.lezh1k.sensordatacollector.CommonClasses.Commons;
import com.example.lezh1k.sensordatacollector.CommonClasses.Coordinates;
import com.example.lezh1k.sensordatacollector.CommonClasses.GeoPoint;
import com.example.lezh1k.sensordatacollector.CommonClasses.SensorGpsDataItem;
import com.example.lezh1k.sensordatacollector.Filters.GPSAccKalmanFilter;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceInterface;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceStatusInterface;
import com.example.lezh1k.sensordatacollector.Loggers.KalmanDistanceLogger;
import com.example.lezh1k.sensordatacollector.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by lezh1k on 1/11/18.
 */

public class KalmanLocationService extends LocationService
        implements SensorEventListener, LocationListener, GpsStatus.Listener {

    private KalmanDistanceLogger m_kalmanDistanceLogger = null;

    public KalmanDistanceLogger getDistanceLogger() {
        return m_kalmanDistanceLogger;
    }

    /**/
    private static final String TAG = "KalmanLocationService";

    public static final int PermissionDenied = 0;
    private LocationManager m_locationManager;

    private boolean m_gpsEnabled = false;
    private boolean m_sensorsEnabled = false;

    private int m_activeSatellites = 0;
    private float m_lastLocationAccuracy = 0;
    private GpsStatus m_gpsStatus;

    /**/
    private GPSAccKalmanFilter m_kalmanFilter;
    private SensorDataEventLoopTask m_eventLoopTask;
    private List<Sensor> m_lstSensors;
    private SensorManager m_sensorManager;
    private double m_magneticDeclination = 0.0;
    private double accDev = 0.8;

    /*accelerometer + rotation vector*/
    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };

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
        LocationService owner;
        SensorDataEventLoopTask(long deltaTMs, LocationService owner) {
            this.deltaTMs = deltaTMs;
            this.owner = owner;
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
                while ((sdi = m_sensorDataQueue.poll()) != null) {
                    //warning!!!
                    if (sdi.getGpsLat() == SensorGpsDataItem.NOT_INITIALIZED) {
                        XLog.i("%d%d KalmanPredict : accX=%f, accY=%f",
                                Commons.LogMessageType.KALMAN_PREDICT.ordinal(),
                                (long)sdi.getTimestamp(),
                                sdi.getAbsEastAcc(),
                                sdi.getAbsNorthAcc());
                        m_kalmanFilter.predict(sdi.getTimestamp(), sdi.getAbsEastAcc(), sdi.getAbsNorthAcc());
                    } else {
                        double xVel = sdi.getSpeed() * Math.cos(sdi.getCourse());
                        double yVel = sdi.getSpeed() * Math.sin(sdi.getCourse());
                        XLog.i("%d%d KalmanUpdate : pos lon=%f, lat=%f, xVel=%f, yVel=%f, posErr=%f, velErr=%f",
                                Commons.LogMessageType.KALMAN_UPDATE.ordinal(),
                                (long)sdi.getTimestamp(),
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
                                sdi.getPosErr()
                        );

                        Location loc = new Location(TAG);
                        GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(),
                                m_kalmanFilter.getCurrentY());
                        loc.setLatitude(pp.Latitude);
                        loc.setLongitude(pp.Longitude);
                        loc.setAltitude(sdi.getGpsAlt());
                        xVel = m_kalmanFilter.getCurrentXVel();
                        yVel = m_kalmanFilter.getCurrentYVel();
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
    }

    public KalmanLocationService() {
        m_lstSensors = new ArrayList<Sensor>();
        m_eventLoopTask = null;
        reset();
        this.accDev = 1.0;
    }

    private String xLogFolderPath;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    class ChangableFileNameGenerator implements FileNameGenerator {
        private String fileName;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public ChangableFileNameGenerator() {
        }
        @Override
        public boolean isFileNameChangeable() {
            return true;
        }
        @Override
        public String generateFileName(int logLevel, long timestamp) {
            return fileName;
        }
    }

    ChangableFileNameGenerator xLogFileNameGenerator = new ChangableFileNameGenerator();
    public void initXlogPrintersFileName() {
        sdf.setTimeZone(TimeZone.getDefault());
        String dateStr = sdf.format(System.currentTimeMillis());
        String fileName = dateStr;
        for (int i = 0; i < 10000; ++i) {
            fileName = String.format("%s_%d", dateStr, i);
            File f = new File(xLogFolderPath, fileName);
            if (!f.exists())
                break;
        }
        xLogFileNameGenerator.setFileName(fileName);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m_locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        File esd = Environment.getExternalStorageDirectory();
        String storageState = Environment.getExternalStorageState();
        if (storageState != null && storageState.equals(Environment.MEDIA_MOUNTED)) {
            xLogFolderPath = String.format("%s/%s/", esd.getAbsolutePath(), Commons.AppName);
            Printer androidPrinter = new AndroidPrinter();             // Printer that print the log using android.util.Log
            initXlogPrintersFileName();
            Printer xLogFilePrinter = new FilePrinter
                    .Builder(xLogFolderPath)
                    .fileNameGenerator(xLogFileNameGenerator)
                    .backupStrategy(new FileSizeBackupStrategy(1024 * 1024 * 100)) //100MB for backup files
                    .build();
            XLog.init(LogLevel.ALL, androidPrinter, xLogFilePrinter);
        } else {
            //todo set some status
        }

        if (m_sensorManager == null) {
            m_sensorsEnabled = false;
            return; //todo handle somehow
        }

        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.d(Commons.AppName, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }

        m_kalmanDistanceLogger = new KalmanDistanceLogger();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void start() {
        m_sensorDataQueue.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            m_serviceStatus = PermissionDenied;
        } else {
            m_serviceStatus = StartLocationUpdates;
            m_locationManager.removeGpsStatusListener(this);
            m_locationManager.addGpsStatusListener(this);
            m_locationManager.removeUpdates(this);
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Commons.GPS_MIN_TIME, Commons.GPS_MIN_DISTANCE, this );
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

        m_kalmanDistanceLogger.reset();
        m_eventLoopTask = new SensorDataEventLoopTask(500, this);
        m_eventLoopTask.needTerminate = false;
        m_eventLoopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stop() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            m_serviceStatus = ServiceStopped;
        } else {
            m_serviceStatus = ServicePaused;
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
            m_eventLoopTask.needTerminate = true;
            m_eventLoopTask.cancel(true);
        }
        m_sensorDataQueue.clear();
    }

    public void reset() {
        m_kalmanFilter = null;
        m_track.clear();
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*do nothing*/
    }

    /*LocationListener methods implementation*/
    @Override
    public void onLocationChanged(Location loc) {
        double x, y, xVel, yVel, posDev, course, speed;
        long timeStamp;
        speed = loc.getSpeed();
        course = loc.getBearing();
        x = loc.getLongitude();
        y = loc.getLatitude();
        xVel = speed * Math.cos(course);
        yVel = speed * Math.sin(course);
        posDev = loc.getAccuracy();
        timeStamp = System.currentTimeMillis();

        GeomagneticField f = new GeomagneticField(
                (float)loc.getLatitude(),
                (float)loc.getLongitude(),
                (float)loc.getAltitude(),
                timeStamp);
        m_magneticDeclination = f.getDeclination();

        if (m_kalmanFilter == null) {
            XLog.i("%d%d KalmanAlloc : lon=%f, lat=%f, speed=%f, course=%f, accDev=%f, posDev=%f",
                    Commons.LogMessageType.KALMAN_ALLOC.ordinal(),
                    timeStamp, x, y, speed, course, accDev, posDev);
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
