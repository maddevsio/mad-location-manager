package com.example.lezh1k.sensordatacollector.database;

import android.location.Location;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Tracking",
        indices = {@Index(value = {"timestamp"}, unique = true)})
public class Tracking {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "latitude")
    private double latitude;
    @ColumnInfo(name = "longitude")
    private double longitude;
    @ColumnInfo(name = "accuracy")
    private float accuracy;
    @ColumnInfo(name = "speed")
    private float speed;
    @ColumnInfo(name = "filter")
    private String filter;
    @ColumnInfo(name = "timestamp")
    private long timestamp;

    public Tracking() {
        this(0, 0, 0, 0, 0, Filter.NONE);
    }

    public Tracking(Location loc, Filter filter) {
        this(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(), loc.getTime(), loc.getSpeed(), filter);
    }

    public Tracking(double latitude, double longitude, float accuracy, long timestamp, float speed, Filter filter) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.speed = speed;
        this.filter = filter.getCode();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "\n[id=" + id + ", lat=" + latitude + ", lng=" + longitude + ", acc=" + accuracy + ", timestamp=" + timestamp + ", speed=" + speed + ", filter=" + filter + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tracking)) return false;

        Tracking t = (Tracking) o;

        return (t.latitude == this.latitude &&
                t.longitude == this.longitude &&
                t.accuracy == this.accuracy &&
                t.speed == this.speed &&
                t.timestamp == this.timestamp &&
                t.filter == this.filter);
    }

    public enum Filter {

        GPS("GPS"),
        KALMAN("KALMAN"),
        GEOHASH("GEOHASH"),
        V4("V4"),
        NONE("NONE");

        private String code;

        Filter(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}

//       todo: save file for all filters > lat, lng, accur, bearing, timestamp, speed, filter
