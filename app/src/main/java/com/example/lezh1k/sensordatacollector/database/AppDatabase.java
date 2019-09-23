package com.example.lezh1k.sensordatacollector.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.lezh1k.sensordatacollector.database.dao.AccelerometerDao;
import com.example.lezh1k.sensordatacollector.database.dao.GyroscopeDao;
import com.example.lezh1k.sensordatacollector.database.dao.MagnetometerDao;
import com.example.lezh1k.sensordatacollector.database.dao.TrackingDao;
import com.example.lezh1k.sensordatacollector.database.model.Accelerometer;
import com.example.lezh1k.sensordatacollector.database.model.Gyroscope;
import com.example.lezh1k.sensordatacollector.database.model.Magnetometer;
import com.example.lezh1k.sensordatacollector.database.model.Tracking;

@Database(entities = {Tracking.class, Accelerometer.class, Gyroscope.class, Magnetometer.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase APP_DATABASE;

    public static AppDatabase getAppDatabase(Context context) {
        if (APP_DATABASE == null) {
            APP_DATABASE = Room.databaseBuilder(context, AppDatabase.class, "tracking_db").build();
        }

        return APP_DATABASE;
    }

    public abstract TrackingDao trackingDao();

    public abstract GyroscopeDao gyroscopeDao();

    public abstract AccelerometerDao accelerometerDao();

    public abstract MagnetometerDao magnetometerDao();
}
