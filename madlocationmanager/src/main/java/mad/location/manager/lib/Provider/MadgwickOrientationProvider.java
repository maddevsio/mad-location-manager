package mad.location.manager.lib.Provider;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import mad.location.manager.lib.Commons.Matrix;
import mad.location.manager.lib.Commons.Quaternion;
import mad.location.manager.lib.Filters.MadgwickAHRS;
import mad.location.manager.lib.Interfaces.IOrientationProvider;
import mad.location.manager.lib.Interfaces.ISensorDataProvider;

public class MadgwickOrientationProvider implements IOrientationProvider, SensorEventListener {

    private Quaternion m_currentOrientationQ = new Quaternion(0.0f, 0.0f, 0.0f, 0.0f);
    private Matrix m_currentOrientationM;

    private MadgwickAHRS m_ahrs = new MadgwickAHRS(100.0f, 0.2f);
    private SensorManager m_sensorManager;
    private boolean m_sensorsEnabled;

    Sensor m_gyroscope;
    Sensor m_accelerometer;
    Sensor m_magnetometer;

    public MadgwickOrientationProvider(SensorManager sensorManager) {
        this.m_sensorManager = sensorManager;
        m_sensorsEnabled = true;

        m_gyroscope = m_sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        m_accelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_magnetometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (m_gyroscope == null || m_accelerometer == null)
            m_sensorsEnabled = false;
    }

    public boolean Enabled() {return m_sensorsEnabled;}

    @Override
    public Quaternion getQuaternion() {
        return m_currentOrientationQ;
    }

    @Override
    public Matrix getMatrix() {
        return null; //todo implement
    }

    @Override
    public boolean start() {
        boolean result = m_sensorsEnabled;
        Sensor lst[] = {m_accelerometer, m_gyroscope, m_magnetometer};
        for (Sensor s : lst) {
            m_sensorManager.unregisterListener(this, s);
            result &= m_sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
        }
        return result;
    }

    @Override
    public void stop() {
        Sensor lst[] = {m_accelerometer, m_gyroscope, m_magnetometer};
        for (Sensor s : lst) {
            m_sensorManager.unregisterListener(this, s);
        }
    }

    boolean m_accReady, m_gyrReady, m_magReady;
    float m_accData[] = new float[3];
    float m_gyrData[] = new float[3];
    float m_magData[] = new float[3];
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, m_accData, 0, 3);
                m_accReady = true;
                break;
            case Sensor.TYPE_GYROSCOPE:
                System.arraycopy(event.values, 0, m_gyrData, 0, 3);
                m_gyrReady = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, m_magData, 0, 3);
                m_magReady = true;
                break;
        }


        if (m_magnetometer != null) {
            if (m_accReady && m_gyrReady && m_magReady) {
                m_accReady = m_gyrReady = m_magReady = false;
                m_ahrs.MadgwickAHRSupdate(
                        m_gyrData[0], m_gyrData[1], m_gyrData[2],
                        m_accData[0], m_accData[1], m_accData[2],
                        m_magData[0], m_magData[1], m_magData[2]);

                float q[] = m_ahrs.getQuaternion();
                m_currentOrientationQ = new Quaternion(q[0], q[1], q[2], q[3]);
            }
        } else {
            if (m_accReady && m_gyrReady) {
                m_accReady = m_gyrReady = false;
                m_ahrs.MadgwickAHRSupdateIMU(
                        m_gyrData[0], m_gyrData[1], m_gyrData[2],
                        m_accData[0], m_accData[1], m_accData[2]
                );
                float q[] = m_ahrs.getQuaternion();
                m_currentOrientationQ = new Quaternion(q[0], q[1], q[2], q[3]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }
}
