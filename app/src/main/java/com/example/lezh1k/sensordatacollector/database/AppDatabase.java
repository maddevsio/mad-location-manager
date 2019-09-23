package com.example.lezh1k.sensordatacollector.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Tracking.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase APP_DATABASE;

    public static AppDatabase getAppDatabase(Context context) {
        if (APP_DATABASE == null) {
            APP_DATABASE = Room.databaseBuilder(context, AppDatabase.class, "tracking_db").build();
        }

        return APP_DATABASE;
    }

    abstract TrackingDao trackingDao();
}
