package com.example.lezh1k.sensordatacollector.database.repository;

import android.content.Context;

import com.example.lezh1k.sensordatacollector.database.AppDatabase;
import com.example.lezh1k.sensordatacollector.database.model.Tracking;

public class TrackingRepository {

    private Context context;

    public TrackingRepository(Context context) {
        this.context = context;
    }

    private AppDatabase database() {
        return AppDatabase.getAppDatabase(context);
    }

    public boolean saveTracking(Tracking... trackings) {

        database().trackingDao().insertAll(trackings);

        return true;
    }
}
