package com.example.lezh1k.sensordatacollector.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Gyroscope",
        indices = {@Index(value = {"timestamp"}, unique = true)})
public class Gyroscope {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo private float x;
    @ColumnInfo private float y;
    @ColumnInfo private float z;
    @ColumnInfo private long timestamp;

    public Gyroscope(){

    }

    public Gyroscope(float x, float y, float z, long timestamp){
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getX() {
        return x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
