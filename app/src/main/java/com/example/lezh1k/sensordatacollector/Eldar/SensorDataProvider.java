package com.example.lezh1k.sensordatacollector.Eldar;

import android.content.Context;
import android.hardware.SensorManager;

public abstract class SensorDataProvider implements ISensorDataProvider.View {
    ISensorDataProvider sensorDataProvider;
    Context context;

    public SensorDataProvider(ISensorDataProvider sensorDataProvider, Context context) {
        this.sensorDataProvider = sensorDataProvider;
        this.context = context;
    }

}
