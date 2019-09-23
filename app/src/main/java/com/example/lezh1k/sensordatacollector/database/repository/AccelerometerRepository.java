package com.example.lezh1k.sensordatacollector.database.repository;

import android.content.Context;

import com.example.lezh1k.sensordatacollector.database.AppDatabase;
import com.example.lezh1k.sensordatacollector.database.model.Accelerometer;

public class AccelerometerRepository {

    private Context context;

    public AccelerometerRepository(Context context) {
        this.context = context;
    }

    private AppDatabase database() {
        return AppDatabase.getAppDatabase(context);
    }

    public boolean saveAccelerometer(Accelerometer... accelerometers) {

        database().accelerometerDao().insertAll(accelerometers);

        return true;

    }
}
