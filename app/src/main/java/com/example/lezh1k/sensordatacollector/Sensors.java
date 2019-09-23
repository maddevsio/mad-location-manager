package com.example.lezh1k.sensordatacollector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.lezh1k.sensordatacollector.database.AsyncRequest;
import com.example.lezh1k.sensordatacollector.database.model.Accelerometer;
import com.example.lezh1k.sensordatacollector.database.model.Gyroscope;
import com.example.lezh1k.sensordatacollector.database.model.Magnetometer;

// MAGNETOMETRO

public abstract class Sensors {

    private SensorManager sensorManager;

    public static class GyroscopeSensor implements SensorEventListener {

        private final Context context;

        public GyroscopeSensor(Context context) {
            this.context = context;
        }
//        private static final float NS2S = 1.0f / 1000000000.0f;
//        private final float[] deltaRotationVector = new float[4];
//        private float timestamp;

        @Override
        public void onSensorChanged(SensorEvent event) {
            // This timestep's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            /*if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                // Calculate the angular speed of the sample
                float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                // (that is, EPSILON should represent your maximum allowable margin of error)
                if (omegaMagnitude > Half.EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;
            }
            timestamp = event.timestamp;
            float[] deltaRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
            // User code should concatenate the delta rotation we computed with the current rotation
            // in order to get the updated rotation.
            // rotationCurrent = rotationCurrent * deltaRotationMatrix;*/

            float[] values = event.values;
            Gyroscope gyroscope = new Gyroscope(values[0], values[1], values[2], event.timestamp);

            new AsyncRequest.SaveGyroscope(context).execute(gyroscope);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    public static class AccelerometerSensor implements SensorEventListener {

        private final Context context;

        public AccelerometerSensor(Context context) {
            this.context = context;
        }

//        private final float[] gravity = {10, 10, 10};
//        private float[] linear_acceleration = new float[3];

        @Override
        public void onSensorChanged(SensorEvent event) {
            /*// In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.
            final float alpha = 0.8F;

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            Log.i("SENSOR:ACCEL", Arrays.toString(linear_acceleration));*/

            float[] values = event.values;
            Accelerometer accelerometer = new Accelerometer(values[0], values[1], values[2], event.timestamp);

            new AsyncRequest.SaveAccelerometer(context).execute(accelerometer);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    public static class MagnetometerSensor implements SensorEventListener {
        private final Context context;

        public MagnetometerSensor(Context context) {
            this.context = context;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            Magnetometer magnetometer = new Magnetometer(values[0], values[1], values[2], event.timestamp);

            new AsyncRequest.SaveMagnetometer(context).execute(magnetometer);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

}
