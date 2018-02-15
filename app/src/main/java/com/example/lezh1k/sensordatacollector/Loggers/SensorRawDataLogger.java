package com.example.lezh1k.sensordatacollector.Loggers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by lezh1k on 12/20/17.
 */

//collect all possible data. uses only in logger
public class SensorRawDataLogger implements SensorEventListener {

    private static final String TAG = "SensorRawDataLogger";
    private List<Sensor> m_lstSensors = new ArrayList<Sensor>();
    private SensorManager m_sensorManager;

    private static int[] sensorTypes = {
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
            Sensor.TYPE_MAGNETIC_FIELD
    };
    private static String[] sensorDataPrefix = {
            "Accelerometer uncalibrated : ",
            "Accelerometer : ",
            "Linear accelerometer : ",
            "Gravity : ",
            "Gyroscope uncalibrated : ",
            "Gyroscope : ",
            "Rotation vector : ",
            "Magnetic uncalibrated : ",
            "Magnetic : "
    };
    
    class DataItem {
        int sensorType;
        float[] data;
        long timestamp;
        public DataItem(int sensorType, float[] data, long timestamp) {
            this.sensorType = sensorType;
            this.data = new float[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);
            this.timestamp = timestamp;
        }

        public void log() {
            int i = 0;
            for (i = 0; i < sensorTypes.length; ++i) {
                if (this.sensorType == sensorTypes[i]) break;
            }
            if (i == sensorTypes.length) return;
            String logStr = String.format(" %d ", timestamp);
            logStr += sensorDataPrefix[i];
            for (float fv : data)
                logStr += String.format("%f ", fv); //%)
            XLog.i(logStr);
        }
    }

    ConcurrentLinkedQueue<DataItem> m_sensorDataQueue = new ConcurrentLinkedQueue<>();
    class LoggerTask extends AsyncTask {
        boolean needTerminate = false;
        long deltaT;
        LoggerTask(long deltaTMs) {
            this.deltaT = deltaTMs;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            while (!needTerminate) {
                try {
                    Thread.sleep(deltaT);
                    DataItem di = null;
                    di = m_sensorDataQueue.poll();
                    if (di != null) {
                        di.log();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
    /*********************************************************/

    LoggerTask loggerTask = new LoggerTask(10);
    public SensorRawDataLogger(SensorManager sensorManager) {
        m_sensorManager = sensorManager;
        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.d(TAG, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }
    }

    public void start() {
        loggerTask.needTerminate = false;
        loggerTask = new LoggerTask(10);
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)
            loggerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            loggerTask.execute();
        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        loggerTask.needTerminate = true;
        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.unregisterListener(this, sensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        DataItem di = new DataItem(event.sensor.getType(), event.values, System.currentTimeMillis());
        m_sensorDataQueue.add(di);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*do nothing*/
    }
}
