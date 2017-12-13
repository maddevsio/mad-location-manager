package com.example.lezh1k.sensordatacollector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.GSASentence;
import net.sf.marineapi.nmea.sentence.GSVSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.VTGSentence;
import net.sf.marineapi.nmea.util.Position;

enum InitSensorErrorFlag {
    SUCCESS(0),
    SENSOR_MANAGER_ERR(1),
    MISSED_ACCELEROMETER(1 << 1),
    MISSED_GYROSCOPE(1<<2),
    MISSED_MAGNETOMETER(1<<3),
    MISSED_LIN_ACCELEROMETER(1 << 4);

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
        return res;
    }
}
/*****************************************************************/

public class MainActivity extends AppCompatActivity
        implements SensorEventListener, GpsStatus.NmeaListener {

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

        //We can use MadgwickAHRS here. But we need gyroscope data in this case.
        //If we need - we will use it. Now use openGL matrices
        float[] R  = new float[16];
        float[] RI = new float[16]; //inverse
        float[] I  = new float[16]; //inclinations

        float[] gravity     = new float[4];
        float[] geomagnetic = new float[4];
        float[] linAcc      = new float[4];

        float[] accAxis = new float[4];
        float[] velAxis = new float[4];

        GPSAccKalmanFilter kfLon = null;
        GPSAccKalmanFilter kfLat = null;
        GPSAccKalmanFilter kfAlt = null;

        double filteredLon = 0.0;
        double filteredLat = 0.0;
        double filteredAlt = 0.0;
        double filteredSpeed = 0.0;
        float[] filteredSpeedAxis = new float[4];

        static final int east = 0;
        static final int north = 1;
        static final int up = 2;

        static final double latLonStandardDeviation = 2.0; // +/- 1m, increased for safety
        static final double altitudeStandardDeviation = 3.518522417151836;

        float llat = 0.0f, llon = 0.0f, lalt= 0.0f;
        @Override
        protected void onProgressUpdate(Object... values) {
            long timeStamp = System.currentTimeMillis();
            System.arraycopy(m_accelerometerValues, 0, gravity, 0, 3);
            System.arraycopy(m_magnetometerValues, 0, geomagnetic, 0, 3);
            System.arraycopy(m_linearAccelerometerValues, 0, linAcc, 0, 3);

            if (!SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                //todo log
                return;
            }

            //we have to invert matrix here because of using openGL.
            android.opengl.Matrix.invertM(RI, 0, R, 0);
            android.opengl.Matrix.multiplyMV(accAxis, 0, RI, 0, linAcc, 0);

            //correct using magnetic declination
            /*  new_north = N * cos(mag_offset) + E * sin(mag_offset)
                new_east = E * cos(mag_offset) - N * sin(mag_offset)*/
            accAxis[north] = (float) (accAxis[north] * Math.cos(m_declination) + accAxis[east] * Math.sin(m_declination));
            accAxis[east] = (float) (accAxis[east] * Math.cos(m_declination) - accAxis[north] * Math.sin(m_declination));
            for (int i = 0; i < accAxis.length; ++i)
                accAxis[i] = Math.round(accAxis[i] / 1e-2) * 1e-2f; //2 digits after point - santimeter/sec^2

            velAxis[east] = (float) (m_gpsSpeed*Math.cos(m_gpsCourse));
            velAxis[north] = (float) (m_gpsSpeed*Math.sin(m_gpsCourse));

            if (kfLon == null && kfLat == null && kfAlt == null) {
                if (m_linAccDeviationCalculator.isM_calculated()) {
                    if (m_gpsLat != 0.0f && m_gpsLon != 0.0f && m_gpsAlt != 0.0) {
                        kfLon = new GPSAccKalmanFilter(Coordinates.LongitudeToMeters(m_gpsLon),
                                velAxis[east],
                                latLonStandardDeviation,
                                m_linAccDeviationCalculator.getSigmas()[east],
                                timeStamp);
                        kfLat = new GPSAccKalmanFilter(Coordinates.LatitudeToMeters(m_gpsLat),
                                velAxis[north],
                                latLonStandardDeviation,
                                m_linAccDeviationCalculator.getSigmas()[north],
                                timeStamp);
                        kfAlt = new GPSAccKalmanFilter(m_gpsAlt,
                                velAxis[up],
                                altitudeStandardDeviation,
                                m_linAccDeviationCalculator.getSigmas()[up],
                                timeStamp);
                    }
                }
            } else {
                kfLon.Predict(timeStamp, accAxis[east]);
                kfLat.Predict(timeStamp, accAxis[north]);
                kfAlt.Predict(timeStamp, accAxis[up]);

                if (m_gpsLat != 0.0f && m_gpsLon != 0.0f && m_gpsAlt != 0.0f) {
                    kfLon.Update(Coordinates.LongitudeToMeters(m_gpsLon),
                            velAxis[east],
                            0.0,
                            m_gpsHorizontalDOP * 0.01);
                    kfLat.Update(Coordinates.LatitudeToMeters(m_gpsLat),
                            velAxis[north],
                            0.0,
                            m_gpsHorizontalDOP * 0.01);
                    kfAlt.Update(m_gpsAlt,
                            velAxis[up],
                            0.0,
                            m_gpsVerticalDOP * 0.01);
                    llat = m_gpsLat;
                    llon = m_gpsLon;
                    lalt = m_gpsAlt;
                    m_gpsLat = m_gpsLon = m_gpsAlt = 0.0f;
                }

                GeoPoint predictedPoint = Coordinates.MetersToGeoPoint(kfLon.getCurrentPosition(),
                        kfLat.getCurrentPosition());

                double predictedVE, predictedVN;
                predictedVE = kfLon.getCurrentVelocity();
                predictedVN = kfLat.getCurrentVelocity();
                double resultantV = Math.sqrt(Math.pow(predictedVE, 2.0) + Math.pow(predictedVN, 2.0));

                filteredLat = predictedPoint.Latitude;
                filteredLon = predictedPoint.Longitude;
                filteredAlt = kfAlt.getCurrentPosition();
                filteredSpeed = resultantV;
                filteredSpeedAxis[east] = (float) predictedVE;
                filteredSpeedAxis[north] = (float) predictedVN;
                filteredSpeedAxis[up] = (float) kfAlt.getCurrentVelocity();
            }

            String str = String.format("" +
                            "MDecl:%f\n" +
                            "Lat:%f\nLon:%f\nAlt:%f\n" +
                            "Acc: E%.2f,N=%.2f,U=%.2f\n" +
                            "LAcc: X%.2f,N=%.2f,U=%.2f\n" +
                            "Vel: E=%.2f,N=%.2f,U:%.2f\n" +
                            "FLat:%f\nFLon:%f\nFAlt:%f\n" +
                            "FSpeed: E:%f,N:%f,ТU:%f",
                    m_declination,
                    llat, llon, lalt,
                    accAxis[east], accAxis[north], accAxis[up],
                    linAcc[0], linAcc[1], linAcc[2],
                    velAxis[east], velAxis[north], velAxis[up],
                    filteredLat, filteredLon, filteredAlt,
                    filteredSpeedAxis[east], filteredSpeedAxis[north], filteredSpeedAxis[up]);
            m_tvLocationData.setText(str);
        }
    }
    /*********************************************************/

    private LocationManager m_locationManager = null;
    private SensorManager m_sensorManager = null;
    private RefreshTask m_refreshTask = null;

    private Sensor m_grAccelerometer = null;
    private Sensor m_linAccelerometer = null;
    private Sensor m_gyroscope = null;
    private Sensor m_magnetometer = null;

    private DeviationCalculator m_accDeviationCalculator =
            new DeviationCalculator(120, 3, "acc");
    private DeviationCalculator m_linAccDeviationCalculator =
            new DeviationCalculator(120, 3, "linAcc");
    private DeviationCalculator m_gyrDeviationCalculator =
            new DeviationCalculator(120, 3, "gyr");
    private DeviationCalculator m_magDeviationCalculator =
            new DeviationCalculator(60, 3, "mag");

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

    private float m_accelerometerValues[] = new float[3];
    private float m_linearAccelerometerValues[] = new float[3];
    private float m_gyroscopeValues[] = new float[3];
    private float m_magnetometerValues[] = new float[3];

    private float m_declination = 0.0f;
    private float m_gpsLat = 0.0f;
    private float m_gpsLon = 0.0f;
    private float m_gpsAlt = 0.0f;
    private float m_gpsSpeed = 0.0f;
    private float m_gpsCourse = 0.0f;
    private float m_gpsPositionDOP = 100.0f; //max
    private float m_gpsVerticalDOP = 100.0f; //max
    private float m_gpsHorizontalDOP = 100.0f; //max

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

        m_grAccelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_linAccelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        m_gyroscope = m_sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        m_magnetometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Sensor toCheck[] = {m_grAccelerometer, m_linAccelerometer, m_gyroscope, m_magnetometer};
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

    protected void onPause() {
        super.onPause();
        m_sensorManager.unregisterListener(this);
        m_locationManager.removeNmeaListener(this);
        m_refreshTask.needTerminate = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_locationManager.removeNmeaListener(this);
            m_locationManager.addNmeaListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (m_locationManager == null) return;
        if (m_sensorManager == null) return;
        if (m_refreshTask == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            m_locationManager.removeNmeaListener(this);
            m_locationManager.addNmeaListener(this);
        }

        m_sensorManager.registerListener(this, m_grAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        m_sensorManager.registerListener(this, m_linAccelerometer, SensorManager.SENSOR_DELAY_GAME);
//        m_sensorManager.registerListener(this, m_gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        m_sensorManager.registerListener(this, m_magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        m_tvAccelerometer.setText("Accelerometer :\n" + sensorDescription(m_grAccelerometer));
        m_tvLinAccelerometer.setText("LinAccelerometer :\n" + sensorDescription(m_linAccelerometer));
        m_tvMagnetometer.setText("Magnetometer :\n" + sensorDescription(m_magnetometer));
//        m_tvGyroscope.setText("Gyroscope :\n" + sensorDescription(m_gyroscope));
        m_refreshTask.needTerminate = false;
        m_refreshTask.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_tvStatus = (TextView) findViewById(R.id.tvStatus);
        m_tvAccelerometer = (TextView) findViewById(R.id.tvAccelerometer);
        m_tvAccelerometerData = (TextView) findViewById(R.id.tvAccelerometerData);
        m_tvLinAccelerometer = (TextView) findViewById(R.id.tvLinAccelerometer);
        m_tvLinAccelerometerData = (TextView) findViewById(R.id.tvLinAccelerometerData);
//        m_tvGyroscope = (TextView) findViewById(R.id.tvGyroscope);
//        m_tvGyroscopeData = (TextView) findViewById(R.id.tvGyroscopeData);
        m_tvMagnetometer = (TextView) findViewById(R.id.tvMagnetometer);
        m_tvMagnetometerData = (TextView) findViewById(R.id.tvMagnetometerData);
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
            m_refreshTask = new RefreshTask(5);
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
                    m_gpsPositionDOP = (float) gsa.getPositionDOP();
                    m_gpsVerticalDOP = (float) gsa.getVerticalDOP();
                    m_gpsHorizontalDOP = (float) gsa.getHorizontalDOP();
                case "gga":
                    GGASentence gga = (GGASentence) s;
                    pos = gga.getPosition();
                    m_gpsHorizontalDOP = (float) gga.getHorizontalDOP();
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
            m_gpsLat = (float) pos.getLatitude();
            m_gpsLon = (float) pos.getLongitude();
            m_gpsAlt = (float) pos.getAltitude();
            GeomagneticField gf = new GeomagneticField(m_gpsLat, m_gpsLon,
                    m_gpsAlt, System.currentTimeMillis());
            m_declination = gf.getDeclination();
        }

        if (speed != null && course != null) {
            m_gpsCourse = course.floatValue();
            m_gpsSpeed = (float) Commons.KnotsPerHour2MeterPerSecond(speed.floatValue());
        }
    }
    /*********************************************************/

    class Integrator {
        float[][] data;
        int m, n, k;
        long dt;

        public Integrator(int m, int n) {
            this.m = m;
            this.n = n;
            data = new float[n][m];
            k = 0;
        }

        public boolean iterate(float[] mes) {
            if (k < n) {
                System.arraycopy(mes, 0, data[k++], 0, m);
                return false;
            } else {
                dt = System.currentTimeMillis();
                k = 0;
                return true;
            }
        }
    }

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
                System.arraycopy(event.values, 0, m_magnetometerValues, 0, 3);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                format = "Acc = %d, Ax = %f, Ay = %f, Az = %f\n";
                tv = m_tvAccelerometerData;
                dc = m_accDeviationCalculator;
                Commons.LowPassFilterArr(0.3f, m_accelerometerValues, event.values);
//                System.arraycopy(event.values, 0, m_accelerometerValues, 0, 3);
                values = m_accelerometerValues;
                break;
            case Sensor.TYPE_GYROSCOPE:
                format = "Acc = %d, Ax = %f, Ay = %f, Az = %f\n";
                tv = m_tvGyroscopeData;
                dc = m_gyrDeviationCalculator;
                System.arraycopy(event.values, 0, m_gyroscopeValues, 0, 3);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                format = "Acc = %d, Afb = %f, Alr = %f, Aud = %f\n";
                dc = m_linAccDeviationCalculator;
                tv = m_tvLinAccelerometerData;
                System.arraycopy(event.values, 0, m_linearAccelerometerValues, 0, 3);
                break;
        }

        if (dc != null) {
            dc.Measure(event.values[0], event.values[1], event.values[2]);
        }

        if (tv != null) {
            tv.setText(String.format(format, event.accuracy,
                    values[0], values[1], values[2]) + //todo change this
                    dc.sigmasToStr());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    /*********************************************************/
}
