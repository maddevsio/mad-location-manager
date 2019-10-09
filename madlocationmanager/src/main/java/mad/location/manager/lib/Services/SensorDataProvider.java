package mad.location.manager.lib.Services;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.Interfaces.ISensorDataProvider;

import static android.content.Context.SENSOR_SERVICE;
import static mad.location.manager.lib.Services.KalmanService.m_settings;

public class SensorDataProvider implements SensorEventListener, ISensorDataProvider.Provider {

    private float[] rotationMatrix = new float[16];
    private float[] rotationMatrixInv = new float[16];
    private float[] absAcceleration = new float[4];
    private float[] linearAcceleration = new float[4];

    private static final String TAG = SensorDataProvider.class.getName();

    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };

    private ISensorDataProvider.Client client;
    private List<Sensor> m_lstSensors;
    private SensorManager m_sensorManager;

    private boolean m_sensorsEnabled = false;

    public SensorDataProvider (ISensorDataProvider.Client client, Context context) {
        m_sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        m_lstSensors = new ArrayList<Sensor>();
        initSensor();
        this.client = client;
    }

    private void initSensor() {
        if (m_sensorManager == null) {
            m_sensorsEnabled = false;
            return; //todo handle somehow
        }

        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.d(TAG, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }
    }

    /*
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:

                break;
            case Sensor.TYPE_GYROSCOPE:

                break;
            case Sensor.TYPE_MAGNETIC_FIELD:

                break;

        }
    }
     */

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linearAcceleration, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(absAcceleration, 0, rotationMatrixInv, 0, linearAcceleration, 0);
                client.absAccelerationDate(absAcceleration);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                android.opengl.Matrix.invertM(rotationMatrixInv, 0, rotationMatrix, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*  */
    }

    @Override
    public void start() {
        m_sensorsEnabled = true;
        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.unregisterListener(this, sensor);
            m_sensorsEnabled &= !m_sensorManager.registerListener(this, sensor,
                    Utils.hertz2periodUs(m_settings.getSensorFrequencyHz()));
        }
    }

    @Override
    public void stop() {
        m_sensorsEnabled = false;
        for (Sensor sensor : m_lstSensors)
            m_sensorManager.unregisterListener(this, sensor);
    }
}
