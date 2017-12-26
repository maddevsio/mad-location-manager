package com.example.lezh1k.sensordatacollector.Loggers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.example.lezh1k.sensordatacollector.Commons;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by lezh1k on 12/26/17.
 */

public class AccelerationLogger implements SensorEventListener {
    private List<Sensor> m_lstSensors = new ArrayList<Sensor>();
    private SensorManager m_sensorManager;

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

    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };
    private static String[] sensorDataPrefix = {
            "Linear abs acc : ",
            "Rotation vector : ",
    };


    public AccelerationLogger(SensorManager sensorManager) {
        m_sensorManager = sensorManager;
        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.d(Commons.AppName, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }
    }

    LoggerTask loggerTask = new LoggerTask(10);
    public void start() {
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

    float[] R = new float[16];
    float[] RI = new float[16];
    float[] accAxis = new float[4];
    float[] linAcc = new float[4];

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linAcc, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(accAxis, 0, RI,
                        0, linAcc, 0);
                DataItem di = new DataItem(event.sensor.getType(), accAxis, System.currentTimeMillis());
                m_sensorDataQueue.add(di);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(R, event.values);
                android.opengl.Matrix.invertM(RI, 0, R, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
