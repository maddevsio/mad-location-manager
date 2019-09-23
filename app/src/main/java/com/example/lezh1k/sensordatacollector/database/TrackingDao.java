package com.example.lezh1k.sensordatacollector.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TrackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Tracking... trackings);

    @Query("SELECT * FROM tracking ORDER BY timestamp ASC")
    List<Tracking> getAll();

    @Query("SELECT * FROM tracking WHERE filter = :filter")
    List<Tracking> getAll(String filter);
}
