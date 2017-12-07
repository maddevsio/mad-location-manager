package com.example.lezh1k.sensordatacollector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

enum InitSensorErrorFlag {
    SUCCESS(0),
    SENSOR_MANAGER_ERR(1),
    MISSED_ACCELEROMETER(1 << 1),
    MISSED_GYROSCOPE(1<<2),
    MISSED_MAGNETOMETER(1<<3);

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
        if ((val & MISSED_GYROSCOPE.flag) != 0)
            res += "Missed gyroscope";
        if ((val & MISSED_MAGNETOMETER.flag) != 0)
            res += "Missed magnetometer";
        return res;
    }
}
/*****************************************************************/

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private LocationManager m_locationManager;
    private SensorManager m_sensorManager;
    private Sensor m_accelerometer;
    private Sensor m_gyroscope;
    private Sensor m_magnetometer;

    private TextView m_tvStatus;
    private TextView m_tvAccelerometer;
    private TextView m_tvAccelerometerData;
    private TextView m_tvGyroscope;
    private TextView m_tvGyroscopeData;
    private TextView m_tvMagnetometer;
    private TextView m_tvMagnetometerData;
    private TextView m_tvLocationData;

    private float m_accData[] = new float[3];
    private float m_gyrData[] = new float[3];
    private float m_magData[] = new float[3];

    private long initSensors() {
        m_sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (m_sensorManager == null) {
            return InitSensorErrorFlag.SENSOR_MANAGER_ERR.flag;
        }

        m_accelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_gyroscope = m_sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        m_magnetometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Sensor toCheck[] = { m_accelerometer, m_gyroscope, m_magnetometer};
        InitSensorErrorFlag toCheckRes[] = { InitSensorErrorFlag.MISSED_ACCELEROMETER,
                InitSensorErrorFlag.MISSED_GYROSCOPE,
                InitSensorErrorFlag.MISSED_MAGNETOMETER};

        long result = InitSensorErrorFlag.SUCCESS.flag;
        for (int i = 0; i < 3; ++i) {
            if (toCheck[i] != null) continue;
            result |= toCheckRes[i].flag;
        }
        return result;
    }

    String sensorDescription(Sensor s) {
        String res = "";
        res += "Res : " + s.getResolution() + "\n";
        res += "Min Del : " + s.getMinDelay() + "\n";
        return res;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_tvStatus = (TextView) findViewById(R.id.tvStatus);
        m_tvAccelerometer = (TextView) findViewById(R.id.tvAccelerometer);
        m_tvAccelerometerData = (TextView) findViewById(R.id.tvAccelerometerData);
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

        //todo move somewhere from here this request
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            m_tvStatus.setText("Have no ACCESS_FINE_LOCATION permission.");
            return;
        } else {
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
        }
        m_tvAccelerometer.setText("Accelerometer :\n" + sensorDescription(m_accelerometer));
        m_tvMagnetometer.setText("Magnetometer :\n" + sensorDescription(m_magnetometer));
        m_tvGyroscope.setText("Gyroscope :\n" + sensorDescription(m_gyroscope));

        m_sensorManager.registerListener(this, m_accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        m_sensorManager.registerListener(this, m_gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        m_sensorManager.registerListener(this, m_magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /*********************************************************/

    @Override
    public void onLocationChanged(Location lkl) {
        GeomagneticField gf = new GeomagneticField((float)lkl.getLatitude(),
                (float)lkl.getLongitude(), (float)lkl.getAltitude(), System.currentTimeMillis());
        m_tvLocationData.setText(String.format("MDecl:%f\nLat:%f,Lon:%f,Alt:%f",
                gf.getDeclination(),
                lkl.getLatitude(),
                lkl.getLongitude(),
                lkl.getAltitude()));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
    }

    /*********************************************************/
    class Calibration {
        static final int MeasurementCalibrationCount = 1000;
        static final double SigmaNotInitialized = -999999.0;

        double sigmaX = SigmaNotInitialized;
        double sigmaY = SigmaNotInitialized;
        double sigmaZ = SigmaNotInitialized;
        double measurementsX[] = new double[MeasurementCalibrationCount];
        double measurementsY[] = new double[MeasurementCalibrationCount];
        double measurementsZ[] = new double[MeasurementCalibrationCount];
        private int count = 0;

        private double calculateSigma(double sigma, double[] calibrations) {
            if (sigma != SigmaNotInitialized) return sigma;
            double sum;
            sum = sigma = 0.0;
            for (int i = 0; i < MeasurementCalibrationCount; ++i) {
                sum += calibrations[i];
            }
            sum /= MeasurementCalibrationCount;

            for (int i = 0; i < MeasurementCalibrationCount; ++i) {
                sigma += Math.pow(calibrations[i] - sum, 2.0);
            }

            sigma /= MeasurementCalibrationCount;
            return sigma;
        }

        void Measure(double x, double y, double z) {
            if (count < MeasurementCalibrationCount) {
                measurementsX[count] = x;
                measurementsY[count] = y;
                measurementsZ[count] = z;
                ++count;
            } else {
                sigmaX = calculateSigma(sigmaX, measurementsX);
                sigmaY = calculateSigma(sigmaY, measurementsY);
                sigmaZ = calculateSigma(sigmaZ, measurementsZ);
            }
        }
    }

    Calibration accCalibration = new Calibration();
    Calibration gyrCalibration = new Calibration();
    Calibration magCalibration = new Calibration();

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView tv = null;
        String format = null;
        Calibration cl = null;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD :
                format = "Acc = %d, Mx = %f, My = %f, Mz = %f\n";
                tv = m_tvMagnetometerData;
                cl = magCalibration;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                format = "Acc = %d, Ax = %f, Ay = %f, Az = %f\n";
                tv = m_tvAccelerometerData;
                cl = accCalibration;
                break;
            case Sensor.TYPE_GYROSCOPE:
                format = "Acc = %d, Ax = %f, Ay = %f, Az = %f\n";
                tv = m_tvGyroscopeData;
                cl = gyrCalibration;
                break;
        }

        if (tv == null) return;
        if (cl == null) return;
        cl.Measure(event.values[0], event.values[1], event.values[2]);
        tv.setText(String.format(format, event.accuracy,
                event.values[0], event.values[1], event.values[2]) +
                String.format("Sx : %f, Sy = %f, Sz = %f", cl.sigmaX, cl.sigmaY, cl.sigmaZ));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
