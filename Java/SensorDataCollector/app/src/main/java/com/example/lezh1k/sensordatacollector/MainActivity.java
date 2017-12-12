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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

//@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity
        implements /*LocationListener, */SensorEventListener, GpsStatus.NmeaListener {

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        handleNmeaReceived(timestamp, nmea);
    }

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
        //If we need - we will use it.
        float[] R  = new float[16];
        float[] RI = new float[16];
        float[] I  = new float[16];

        float[] gravity     = new float[3];
        float[] geomagnetic = new float[3];
        float[] linAcc      = new float[4];

        float[] eAcc    = new float[4];
        float[] velAxis = new float[3];

        @Override
        protected void onProgressUpdate(Object... values) {
            if (m_accData == null || m_magData == null) return;
            System.arraycopy(m_accData, 0, gravity, 0, 3);
            System.arraycopy(m_magData, 0, geomagnetic, 0, 3);
            System.arraycopy(m_linAccData, 0, linAcc, 0, 3);

            if (!SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                //todo something
                return;
            }
            //we have to invert matrix here because of using opengl.
            android.opengl.Matrix.invertM(RI, 0, R, 0);
            android.opengl.Matrix.multiplyMV(eAcc, 0, RI, 0, linAcc, 0);

            //correct using magnetic declination
            /*  new_north = N * cos(mag_offset) + E * sin(mag_offset)
                new_east = E * cos(mag_offset) - N * sin(mag_offset)*/
            eAcc[1] = (float) (eAcc[1] * Math.cos(m_decl) + eAcc[0] * Math.sin(m_decl));
            eAcc[0] = (float) (eAcc[0] * Math.cos(m_decl) - eAcc[1] * Math.sin(m_decl));
            for (int i = 0; i < 3; ++i) {
                eAcc[i] = Math.round(eAcc[i] / 1e-2) * 1e-2f; //2 digits after point - santimeter/sec^2
            }

            if (m_gpsCourse != 361.0f) {
                velAxis[0] = (float) (m_gpsSpeed*Math.cos(m_gpsCourse));
                velAxis[1] = (float) (m_gpsSpeed*Math.sin(m_gpsCourse));
            }

            String str = String.format("" +
                            "MDecl:%f\n" +
                            "Lat:%f\nLon:%f\nAlt:%f\n" +
                            "AccE=%.2f,AccN=%.2f,AccD=%.2f\n" +
                            "VelE=%.2f,VelN=%.2f,VelD=%.2f\n" +
                            "Gps speed : %f\nGps accuracy : %f\n",
                    m_decl,
                    m_currentLat, m_currentLon, m_currentAlt,
                    eAcc[0], eAcc[1], eAcc[2],
                    velAxis[0], velAxis[1], velAxis[2],
                    m_gpsSpeed, m_gpsAccuracy);
            m_tvLocationData.setText(str);
        }
    }
    /*********************************************************/


    private LocationManager m_locationManager;
    private SensorManager m_sensorManager;
    private Sensor m_grAccelerometer;
    private Sensor m_linAccelerometer;
    private Sensor m_gyroscope;
    private Sensor m_magnetometer;

    private TextView m_tvStatus;
    private TextView m_tvAccelerometer;
    private TextView m_tvAccelerometerData;
    private TextView m_tvLinAccelerometer;
    private TextView m_tvLinAccelerometerData;
    private TextView m_tvGyroscope;
    private TextView m_tvGyroscopeData;
    private TextView m_tvMagnetometer;
    private TextView m_tvMagnetometerData;
    private TextView m_tvLocationData;

    private float m_accData[] = new float[3];
    private float m_linAccData[] = new float[3];
    private float m_gyrData[] = new float[3];
    private float m_magData[] = new float[3];

    private float m_decl = 0.0f;
    private float m_currentLat = 0.0f;
    private float m_currentLon = 0.0f;
    private float m_currentAlt = 0.0f;
    private float m_gpsSpeed = 0.0f;
    private float m_gpsCourse = 361.0f; //unreal value
    private float m_gpsAccuracy = 0.0f;

    private RefreshTask m_refreshTask;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_tvStatus = (TextView) findViewById(R.id.tvStatus);
        m_tvAccelerometer = (TextView) findViewById(R.id.tvAccelerometer);
        m_tvAccelerometerData = (TextView) findViewById(R.id.tvAccelerometerData);
        m_tvLinAccelerometer = (TextView) findViewById(R.id.tvLinAccelerometer);
        m_tvLinAccelerometerData = (TextView) findViewById(R.id.tvLinAccelerometerData);
        m_tvGyroscope = (TextView) findViewById(R.id.tvGyroscope);
        m_tvGyroscopeData = (TextView) findViewById(R.id.tvGyroscopeData);
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
//            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
            m_locationManager.addNmeaListener(this);
        }

        m_tvAccelerometer.setText("Accelerometer :\n" + sensorDescription(m_grAccelerometer));
        m_tvMagnetometer.setText("Magnetometer :\n" + sensorDescription(m_magnetometer));
        m_tvGyroscope.setText("Gyroscope :\n" + sensorDescription(m_gyroscope));
        m_tvLinAccelerometer.setText("LinAccelerometer :\n" + sensorDescription(m_linAccelerometer));

        m_sensorManager.registerListener(this, m_grAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        m_sensorManager.registerListener(this, m_gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        m_sensorManager.registerListener(this, m_magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        m_sensorManager.registerListener(this, m_linAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        m_refreshTask = new RefreshTask(10);
        m_refreshTask.execute();
    }
    /*********************************************************/

    private void handleNmeaReceived(long timeStamp, String nmea) {

        nmea = nmea.substring(0, nmea.length()-1); //remove last \n
        Sentence s = SentenceFactory.getInstance().createParser(nmea);
        Position pos = null;
        Double speed = null;
        Double speedKmh = null;
        Double course = null;

        try {
            switch (s.getSentenceId().toLowerCase()) {
                case "gsa":
                    GSASentence gsa = (GSASentence) s;
//                    gsa.getHorizontalDOP()
                case "gga":
                    GGASentence gga = (GGASentence) s;
                    pos = gga.getPosition();
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
                    speedKmh = vtg.getSpeedKmh();
                    //vtg.getMagneticCourse();
                    break;
            }
        } catch (Exception exc) {
            //nothing
        }

        if (pos != null) {
            m_currentLat = (float) pos.getLatitude();
            m_currentLon = (float) pos.getLongitude();
            m_currentAlt = (float) pos.getAltitude();
            GeomagneticField gf = new GeomagneticField(m_currentLat, m_currentLon,
                    m_currentAlt, System.currentTimeMillis());
            m_decl = gf.getDeclination();
        }

        if (course != null) {
            m_gpsCourse = course.floatValue();
        }

        if (speed != null) {
            m_gpsSpeed = (float) Commons.MilesPerHour2MeterPerSecond(speed.floatValue());
            if (m_gpsSpeed > 0.0f)
                m_tvStatus.setText(String.format("%f",m_gpsSpeed));
        }

        if (speedKmh != null) {
            m_gpsSpeed = speedKmh.floatValue() * (1000.0f / 3600.0f);
        }
    }

//    @Override
//    public void onLocationChanged(Location lkl) {
////        m_currentLat = (float) lkl.getLatitude();
////        m_currentLon = (float) lkl.getLongitude();
////        m_currentAlt = (float) lkl.getAltitude();
////        m_gpsSpeed = lkl.getSpeed();
////        m_gpsAccuracy = lkl.getAccuracy();
////        GeomagneticField gf = new GeomagneticField(m_currentLat, m_currentLon,
////                m_currentAlt, System.currentTimeMillis());
////        m_decl = gf.getDeclination();
//    }

//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String s) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String s) {
//    }

    DeviationCalculator accDeviationCalculator = new DeviationCalculator(500, 3);
    DeviationCalculator linAccDeviationCalculator = new DeviationCalculator(500, 3);
    DeviationCalculator gyrDeviationCalculator = new DeviationCalculator(200, 3);
    DeviationCalculator magDeviationCalculator = new DeviationCalculator(150, 3);

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
                dc = magDeviationCalculator;
                System.arraycopy(event.values, 0, m_magData, 0, 3);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                format = "Acc = %d, Ax = %f, Ay = %f, Az = %f\n";
                tv = m_tvAccelerometerData;
                dc = accDeviationCalculator;
                Commons.LowPassFilterArr(0.2f, m_accData, event.values);
                values = m_accData;
                break;
            case Sensor.TYPE_GYROSCOPE:
                format = "Acc = %d, Ax = %f, Ay = %f, Az = %f\n";
                tv = m_tvGyroscopeData;
                dc = gyrDeviationCalculator;
                System.arraycopy(event.values, 0, m_gyrData, 0, 3);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                format = "Acc = %d, Ae = %f, An = %f, Au = %f\n";
                dc = linAccDeviationCalculator;
                tv = m_tvLinAccelerometerData;
                Commons.LowPassFilterArr(0.2f, m_linAccData, event.values);
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
}
