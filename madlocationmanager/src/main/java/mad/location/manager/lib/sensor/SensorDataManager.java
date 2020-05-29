package mad.location.manager.lib.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.utils.Settings;

import static android.content.Context.SENSOR_SERVICE;

public class SensorDataManager implements SensorEventListener {
    private static final String TAG = "SensorDataManager";
    private static final int[] SENSOR_TYPES = { Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_ROTATION_VECTOR };

    private SensorManager sensorManager;
    private List<Sensor> sensors;
    private SensorCallback callback;

    private double sensorFrequencyHz;

    private float[] rotationMatrix;
    private float[] rotationMatrixInv;
    private float[] absAcceleration;
    private float[] linearAcceleration;

    public SensorDataManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        sensors = new ArrayList<>();

        sensorFrequencyHz = Settings.DEFAULT_SENSOR_FREQ_HZ;

        rotationMatrix = new float[16];
        rotationMatrixInv = new float[16];
        absAcceleration = new float[4];
        linearAcceleration = new float[4];
    }

    public SensorDataManager setCallback(SensorCallback callback) {
        this.callback = callback;
        return this;
    }

    public SensorDataManager setSensorFrequencyHz(double sensorFrequencyHz) {
        this.sensorFrequencyHz = sensorFrequencyHz;
        return this;
    }

    public boolean start() {
        if (sensorManager == null) {
            Log.e(TAG, "Sensor manager is null");
            return false;
        }

        for (Integer sensorType : SENSOR_TYPES) {
            Sensor sensor = sensorManager.getDefaultSensor(sensorType);
            if (sensor == null) {
                Log.e(TAG, String.format("Couldn't get sensor %d", sensorType));
                continue;
            }
            sensors.add(sensor);
        }

        for (Sensor sensor : sensors) {
            sensorManager.unregisterListener(this, sensor);
            sensorManager.registerListener(this, sensor, Utils.hertz2periodUs(sensorFrequencyHz));
        }

        return !sensors.isEmpty();
    }

    public void stop() {
        for (Sensor sensor : sensors) {
            sensorManager.unregisterListener(this, sensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(sensorEvent.values, 0, linearAcceleration, 0, sensorEvent.values.length);
                android.opengl.Matrix.multiplyMV(absAcceleration, 0, rotationMatrixInv, 0, linearAcceleration, 0);

                if (callback != null) {
                    callback.onABSAccelerationChanged(absAcceleration);
                }

                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
                android.opengl.Matrix.invertM(rotationMatrixInv, 0, rotationMatrix, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }
}
