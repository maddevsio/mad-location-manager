package com.example.lezh1k.sensordatacollector.database.repository;

import android.content.Context;

import com.example.lezh1k.sensordatacollector.database.AppDatabase;
import com.example.lezh1k.sensordatacollector.database.model.Gyroscope;

public class GyroscopeRepository {
    private Context context;

    public GyroscopeRepository(Context context) {
        this.context = context;
    }

    private AppDatabase database() {
        return AppDatabase.getAppDatabase(context);
    }

    public boolean saveGyroscope(Gyroscope... gyroscopes) {

        database().gyroscopeDao().insertAll(gyroscopes);

        return true;

    }
}
