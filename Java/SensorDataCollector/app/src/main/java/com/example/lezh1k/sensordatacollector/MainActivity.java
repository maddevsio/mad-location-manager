package com.example.lezh1k.sensordatacollector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.example.lezh1k.sensordatacollector.Loggers.AccelerationLogger;
import com.example.lezh1k.sensordatacollector.Loggers.GPSDataLogger;
import com.example.lezh1k.sensordatacollector.SensorDataProvider.DeviationCalculator;
import com.example.lezh1k.sensordatacollector.Loggers.SensorRawDataLogger;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.GSASentence;
import net.sf.marineapi.nmea.sentence.GSVSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.VTGSentence;
import net.sf.marineapi.nmea.util.Position;

import java.io.File;


enum InitSensorErrorFlag {
    SUCCESS(0),
    SENSOR_MANAGER_ERR(1),
    MISSED_ACCELEROMETER(1 << 1),
    MISSED_GYROSCOPE(1<<2),
    MISSED_MAGNETOMETER(1<<3),
    MISSED_ROTATION_SENSOR(1<<4),
    MISSED_LIN_ACCELEROMETER(1 << 5);

    public final long flag;
    InitSensorErrorFlag(long statusFlagValue) {
        this.flag = statusFlagValue;
    }

    public static String toString(long val) {
        String res = "";
        if (val == SUCCESS.flag) return "Success";
        if ((val & SENSOR_MANAGER_ERR.flag) != 0)
            res += "Sensor manager error";
        if ((val & MISSED_ACCELEROMETER.flag) != 0)
            res += "Missed accelerometer";
        if ((val & MISSED_LIN_ACCELEROMETER.flag) != 0)
            res += "Missed linear accelerometer";
        if ((val & MISSED_GYROSCOPE.flag) != 0)
            res += "Missed gyroscope";
        if ((val & MISSED_MAGNETOMETER.flag) != 0)
            res += "Missed magnetometer";
        if ((val & MISSED_ROTATION_SENSOR.flag) != 0)
            res += "Missed rotation sensor";
        return res;
    }
}

public class MainActivity extends AppCompatActivity
        implements SensorEventListener, LocationListener, GpsStatus.NmeaListener {

    class RefreshTask extends AsyncTask {
        boolean needTerminate = false;
        long deltaT;
        RefreshTask(long deltaTMs) {
            this.deltaT = deltaTMs;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            while (!needTerminate) {
                try {
                    Thread.sleep(deltaT);
                    publishProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            m_tvLocationData.setText(m_gma.debugString());
            m_tvStatus.setText(String.format("mf:%f,af:%f,laf:%f,gf:%f",
                    m_magDeviationCalculator.getFrequencyMean(),
                    m_accDeviationCalculator.getFrequencyMean(),
                    m_linAccDeviationCalculator.getFrequencyMean(),
                    m_gyrDeviationCalculator.getFrequencyMean()));
        }
    }
    /*********************************************************/

    private LocationManager m_locationManager = null;
    private SensorManager m_sensorManager = null;
    private RefreshTask m_refreshTask = null;

    private FilterGMA m_gma = null;
    private Sensor m_grAccelerometer = null;
    private Sensor m_linAccelerometer = null;
    private Sensor m_gyroscope = null;
    private Sensor m_magnetometer = null;
    private Sensor m_rotationSensor = null;

    private DeviationCalculator m_accDeviationCalculator = null;
    private DeviationCalculator m_linAccDeviationCalculator = null;
    private DeviationCalculator m_gyrDeviationCalculator = null;
    private DeviationCalculator m_magDeviationCalculator = null;

    private TextView m_tvStatus = null;
    private TextView m_tvAccelerometer = null;
    private TextView m_tvAccelerometerData = null;
    private TextView m_tvLinAccelerometer = null;
    private TextView m_tvLinAccelerometerData = null;
    private TextView m_tvGyroscope = null;
    private TextView m_tvGyroscopeData = null;
    private TextView m_tvMagnetometer = null;
    private TextView m_tvMagnetometerData = null;
    private TextView m_tvLocationData = null;

    private SensorRawDataLogger m_sensorRawDataLogger = null;
    private GPSDataLogger m_gpsDataLogger = null;
    private AccelerationLogger m_accDataLogger = null;

    static String sensorDescription(Sensor s) {
        String res = "";
        res += "Res : " + s.getResolution() + "\n";
        res += "Min Del : " + s.getMinDelay() + "\n";
        return res;
    }

    private long initSensors() {
        m_sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (m_sensorManager == null) {
            return InitSensorErrorFlag.SENSOR_MANAGER_ERR.flag;
        }

        m_grAccelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        m_linAccelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        m_rotationSensor = m_sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        m_gyroscope = m_sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        m_magnetometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Sensor toCheck[] = {m_grAccelerometer, m_linAccelerometer, m_rotationSensor,
                m_gyroscope, m_magnetometer};
        InitSensorErrorFlag toCheckRes[] = {
                InitSensorErrorFlag.MISSED_ACCELEROMETER,
                InitSensorErrorFlag.MISSED_LIN_ACCELEROMETER,
                InitSensorErrorFlag.MISSED_GYROSCOPE,
                InitSensorErrorFlag.MISSED_MAGNETOMETER};

        long result = InitSensorErrorFlag.SUCCESS.flag;
        for (int i = 0; i < toCheck.length; ++i) {
            if (toCheck[i] != null) continue;
            result |= toCheckRes[i].flag;
        }
        return result;
    }

    private static final int GpsMinTime = 1000;
    private static final int GpsMinDistance = 0;

    protected void onPause() {
        super.onPause();
        if (m_sensorRawDataLogger != null)
            m_sensorRawDataLogger.stop();
        if (m_gpsDataLogger != null)
            m_gpsDataLogger.stop();
        if (m_accDataLogger != null)
            m_accDataLogger.stop();
//        m_sensorManager.unregisterListener(this);
//        m_locationManager.removeNmeaListener(this);
//        m_locationManager.removeUpdates(this);
//        m_refreshTask.needTerminate = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_locationManager.removeNmeaListener(this);
            m_locationManager.addNmeaListener(this);
            m_locationManager.removeUpdates(this);
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    GpsMinTime, GpsMinDistance, this);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 120);
        }

        if (m_gpsDataLogger != null) {
            m_gpsDataLogger.start();
        }

        if (m_accDataLogger != null) {
            m_accDataLogger.start();
        }
//        if (m_sensorRawDataLogger != null)
//            m_sensorRawDataLogger.start();
//        if (m_locationManager == null) return;
//        if (m_sensorManager == null) return;
//        if (m_refreshTask == null) return;
//
//        m_accDeviationCalculator =
//                new DeviationCalculator(200, 3);
//        m_linAccDeviationCalculator =
//                new DeviationCalculator(200, 3);
//        m_gyrDeviationCalculator =
//                new DeviationCalculator(200, 3);
//        m_magDeviationCalculator =
//                new DeviationCalculator(100, 3);
//
//        m_gma = new FilterGMA(m_accDeviationCalculator, m_linAccDeviationCalculator,
//                m_gyrDeviationCalculator, m_magDeviationCalculator);
//
//        m_sensorManager.registerListener(this, m_grAccelerometer,
//                SensorManager.SENSOR_DELAY_GAME);
//        m_sensorManager.registerListener(this, m_linAccelerometer,
//                SensorManager.SENSOR_DELAY_GAME);
//        m_sensorManager.registerListener(this, m_rotationSensor,
//                SensorManager.SENSOR_DELAY_GAME);
//        m_sensorManager.registerListener(this, m_gyroscope,
//                SensorManager.SENSOR_DELAY_GAME);
//        m_sensorManager.registerListener(this, m_magnetometer,
//                SensorManager.SENSOR_DELAY_NORMAL);
//
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.ACCESS_FINE_LOCATION}, 100);
//        } else {
//            m_locationManager.removeNmeaListener(this);
//            m_locationManager.addNmeaListener(this);
//            m_locationManager.removeUpdates(this);
//            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                    GpsMinTime, GpsMinDistance, this);
//
//            Location lkl = m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if (lkl != null) {
//                m_gma.setGpsPosition(lkl.getLatitude(), lkl.getLongitude(), lkl.getAltitude());
//                m_gma.setGpsSpeed(lkl.getSpeed());
//            }
//        }
//
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] {
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
//        }
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] {
//                    Manifest.permission.READ_EXTERNAL_STORAGE}, 120);
//        }
//
//
//        m_tvLinAccelerometer.setText("LinAccelerometer :\n" + sensorDescription(m_linAccelerometer));
//        m_refreshTask.needTerminate = false;
//        m_refreshTask.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_tvStatus = (TextView) findViewById(R.id.tvStatus);
        m_tvLinAccelerometer = (TextView) findViewById(R.id.tvLinAccelerometer);
        m_tvLinAccelerometerData = (TextView) findViewById(R.id.tvLinAccelerometerData);
        m_tvLocationData = (TextView) findViewById(R.id.tvLocationData);

        long ir = initSensors();
        m_tvStatus.setText(InitSensorErrorFlag.toString(ir));
        if (ir != InitSensorErrorFlag.SUCCESS.flag) {
            return;
        }

        m_tvLocationData.setText("Don't know where we are");
        m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (m_locationManager == null) {
            m_tvStatus.setText("Couldn't get location manager");
            return;
        }

        if (m_refreshTask == null)
            m_refreshTask = new RefreshTask(30);

        if (m_sensorRawDataLogger == null)
            m_sensorRawDataLogger = new SensorRawDataLogger(m_sensorManager);

        if (m_gpsDataLogger == null)
            m_gpsDataLogger = new GPSDataLogger(m_locationManager, this);

        if (m_accDataLogger == null)
            m_accDataLogger = new AccelerationLogger(m_sensorManager);

        File esd = Environment.getExternalStorageDirectory();
        String storageState = Environment.getExternalStorageState();
        if (storageState != null && storageState.equals(Environment.MEDIA_MOUNTED)) {
            String logFolderPath = String.format("%s/%s/", esd.getAbsolutePath(), Commons.AppName);
            Printer androidPrinter = new AndroidPrinter();             // Printer that print the log using android.util.Log
            Printer filePrinter = new FilePrinter                      // Printer that print the log to the file system
                    .Builder(logFolderPath)                              // Specify the path to save log file
                    .fileNameGenerator(new DateFileNameGenerator()).build();
            XLog.init(LogLevel.ALL, androidPrinter, filePrinter);
            XLog.i("Started!!!");
        } else {
            System.exit(0); //MUOHOHO
        }
    }
    /*********************************************************/

    @Override
    public void onLocationChanged(Location loc) {
        m_gma.setGpsPosition(loc.getLatitude(), loc.getLongitude(), loc.getAltitude());
        m_gma.setGpsSpeed(loc.getSpeed());
        m_gma.setGpsHorizontalDop(loc.getAccuracy() / 100.0);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    /*********************************************************/

    @Override
    public void onNmeaReceived(long timestamp, String msg) {
        handleNMEAReceived(timestamp, msg);
    }

    private void handleNMEAReceived(long timeStamp, String msg) {
        for (int i = 0; i < 2; ++i) {
            char lc = msg.charAt(msg.length()-1);
            if (lc == '\r' || lc == '\n') //do we need to check '\r' ?
                msg = msg.substring(0, msg.length() - 1);
        }

        Position pos = null;
        Double speed = null;
        Double course = null;

        try {
            Sentence s = SentenceFactory.getInstance().createParser(msg);
            switch (s.getSentenceId().toLowerCase()) {
                case "gsa":
                    GSASentence gsa = (GSASentence) s;
                    m_gma.setGpsHorizontalDop(gsa.getHorizontalDOP());
                    m_gma.setGpsVerticalDop(gsa.getVerticalDOP());
                case "gga":
                    GGASentence gga = (GGASentence) s;
                    pos = gga.getPosition();
                    m_gma.setGpsHorizontalDop(gga.getHorizontalDOP());
                    break;
                case "gll":
                    GLLSentence gll = (GLLSentence) s;
                    pos = gll.getPosition();
                    break;
                case "rmc":
                    RMCSentence rmc = (RMCSentence) s;
                    pos = rmc.getPosition();
                    speed = rmc.getSpeed();
                    course = rmc.getCourse();
                    break;
                case "vtg":
                    VTGSentence vtg = (VTGSentence) s;
                    speed = vtg.getSpeedKnots();
                    course = vtg.getTrueCourse();
                    break;
                case "gsv":
                    GSVSentence gsv = (GSVSentence) s;
                    break;
                default:
                    //todo log messages that we don't handle for analyze
                    m_tvStatus.setText(s.getSentenceId());
                    break;
            }
        } catch (Exception exc) {
            //we use exception here because net.sf.marineapi uses
            //exceptions as result code %)
        }

        if (pos != null) {
            m_gma.setGpsPosition(pos.getLatitude(), pos.getLongitude(), pos.getAltitude());
        }

        if (speed != null && course != null) {
            m_gma.setGpsCourse(course.doubleValue());
            m_gma.setGpsSpeed(course.doubleValue());
        }
    }
    /*********************************************************/

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView tv = null;
        String format = null;
        DeviationCalculator dc = null;
        float values[] = event.values;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD :
                format = "Acc = %d, Mx = %f, My = %f, Mz = %f\n";
                tv = m_tvMagnetometerData;
                dc = m_magDeviationCalculator;
                m_gma.setGeomagnetic(event.values, dc);
                break;
            case Sensor.TYPE_GRAVITY:
                format = "Acc = %d, Ax = %f, Ay = %f, Az = %f\n";
                tv = m_tvAccelerometerData;
                dc = m_accDeviationCalculator;
                m_gma.setGravity(event.values, dc);
                break;
            case Sensor.TYPE_GYROSCOPE:
                format = "Acc = %d, Ax = %f, Ay = %f, Az = %f\n";
                tv = m_tvGyroscopeData;
                dc = m_gyrDeviationCalculator;
                m_gma.setGyroscope(event.values, dc);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                format = "Acc = %d, Afb = %f, Alr = %f, Aud = %f\n";
                dc = m_linAccDeviationCalculator;
                tv = m_tvLinAccelerometerData;

                if (dc.is_calculated()) {
                    m_gma.init(dc.getSigmas());
                }
                m_gma.setLinAcc(event.values);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                m_gma.setRotation(event.values);
                break;
        }

        if (dc != null) {
            dc.Measure(event.values);
        }

        if (tv != null) {
            tv.setText(String.format(format, event.accuracy,
                    values[0], values[1], values[2]) + //todo change this
                    dc.deviationInfoString());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    /*********************************************************/
}
