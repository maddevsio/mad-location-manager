package com.example.lezh1k.sensordatacollector.Eldar;

import android.hardware.SensorEvent;

public interface ISensorDataProvider  {
    void AccelerometerData(SensorEvent event);
    void GyroscopeData(SensorEvent event);
    void MagnetometerData(SensorEvent event);

    interface View {
        void onStop();
        void onStart();
    }
}
