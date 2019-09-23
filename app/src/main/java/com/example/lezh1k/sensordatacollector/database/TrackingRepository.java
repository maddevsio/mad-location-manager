package com.example.lezh1k.sensordatacollector.database;

import android.content.Context;

import java.util.List;

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

    public List<Tracking> getAll() {
        return database().trackingDao().getAll();
    }

    public List<Tracking> getAll(Tracking.Filter filter) {
        return database().trackingDao().getAll(filter.toString());
    }

    public void clearAllTables() {
        database().clearAllTables();
    }
}
