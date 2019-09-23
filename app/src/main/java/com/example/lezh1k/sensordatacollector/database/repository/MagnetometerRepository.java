package com.example.lezh1k.sensordatacollector.database.repository;

import android.content.Context;

import com.example.lezh1k.sensordatacollector.database.AppDatabase;
import com.example.lezh1k.sensordatacollector.database.model.Magnetometer;

public class MagnetometerRepository {
    private Context context;

    public MagnetometerRepository(Context context) {
        this.context = context;
    }

    private AppDatabase database() {
        return AppDatabase.getAppDatabase(context);
    }

    public boolean saveMagnetometer(Magnetometer... magnetometers) {

        database().magnetometerDao().insertAll(magnetometers);

        return true;

    }
}
