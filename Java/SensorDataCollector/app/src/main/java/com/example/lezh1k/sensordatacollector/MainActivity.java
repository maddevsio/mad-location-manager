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
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private enum InitSensorErrorFlag {
        SUCCESS(0),
        SENSOR_MANAGER_ERR(1),
        MISSED_ACCELEROMETER(1 << 1),
        MISSED_GYROSCOPE(1<<2),
        MISSED_MAGNETOMETER(1<<3);

        private final long flag;
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

    private LocationManager m_locationManager;
    private SensorManager m_sensorManager;
    private Sensor m_accelerometer;
    private Sensor m_gyroscope;
    private Sensor m_magnetometer;

    private TextView m_tvStatus;
    private TextView m_tvAccelerometerData;
    private TextView m_tvGyroscopeData;
    private TextView m_tvMagnetometerData;
    private TextView m_tvLocationData;

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

    String sensorDesctiption(Sensor s) {
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
        m_tvAccelerometerData = (TextView) findViewById(R.id.tvAccelerometerData);
        m_tvGyroscopeData = (TextView) findViewById(R.id.tvGyroscopeData);
        m_tvMagnetometerData = (TextView) findViewById(R.id.tvMagnetometerData);
        m_tvLocationData = (TextView) findViewById(R.id.tvLocationData);

        long ir = initSensors();
        m_tvStatus.setText(InitSensorErrorFlag.toString(ir));
        if (ir != InitSensorErrorFlag.SUCCESS.flag) {
            return;
        }

        m_tvAccelerometerData.setText(sensorDesctiption(m_accelerometer));
        m_tvGyroscopeData.setText(sensorDesctiption(m_gyroscope));
        m_tvMagnetometerData.setText(sensorDesctiption(m_magnetometer));
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
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
            GeomagneticField gf = new GeomagneticField(m_locationManager.getLastKnownLocation())
        }

        m_sensorManager.registerListener(this, m_accelerometer, 20000);
        m_sensorManager.registerListener(this, m_gyroscope, 20000);
        m_sensorManager.registerListener(this, m_magnetometer, 2000);

    }

    /*********************************************************/

    @Override
    public void onLocationChanged(Location location) {
        m_tvLocationData.setText(location.toString());
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        TextView tv = null;
        String format = null;
        if (sensorEvent.sensor == m_magnetometer) {
            format = "Acc = %d\nMx = %f, My = %f, Mz = %f\n";
            tv = m_tvAccelerometerData;
        } else if (sensorEvent.sensor == m_accelerometer) {
            format = "Acc = %d\nAx = %f, Ay = %f, Az = %f\n";
            tv = m_tvAccelerometerData;
        } else if (sensorEvent.sensor == m_gyroscope) {
            format = "Acc = %d\nAx = %f, Ay = %f, Az = %f\n";
            tv = m_tvGyroscopeData;
        }
        if (tv == null) return;
        tv.setText(String.format(format, sensorEvent.accuracy,
                sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
