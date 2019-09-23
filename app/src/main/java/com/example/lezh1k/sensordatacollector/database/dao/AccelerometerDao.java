package com.example.lezh1k.sensordatacollector.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.example.lezh1k.sensordatacollector.database.model.Accelerometer;

@Dao
public interface AccelerometerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Accelerometer... accelerometers);

}
