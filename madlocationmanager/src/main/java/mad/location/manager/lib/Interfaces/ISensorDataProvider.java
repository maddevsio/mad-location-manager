package mad.location.manager.lib.Interfaces;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public interface ISensorDataProvider {
    interface Provider {
        void start();
        void stop();
    }

    interface Client {
        void absAccelerationDate(float[] absAcceleration);
        void rotationMatrixInv(float[] rotationMatrixInv, float[] rotationMatrix);
    }

    /*

    interface Provider {
        interface AccelerometerDataProvider {

        }

        interface GyroscopeDataProvider {

        }

        interface MagnetometerDataProvider {

        }
    }

    interface Client {
        \\ void SHOWDATE();
    }

     */
}
