package com.example.gpsacckalmanfusion.Lib.Services;

/**
 * Created by lezh1k on 1/11/18.
 */

public class LocationServiceSettings {
    private double accelerationDeviation;
    private int gpsMinDistance;
    private int gpsMinTime;
    private int sensorFfequencyHz;
    private int geoHashPrecision;
    private int geoHashPointMinCount;
    private boolean logToFile;

    public LocationServiceSettings(double accelerationDeviation,
                                   int gpsMinDistance,
                                   int gpsMinTime,
                                   int sensorFfequencyHz,
                                   int geoHashPrecision,
                                   int geoHashPointMinCount,
                                   boolean logToFile) {
        this.accelerationDeviation = accelerationDeviation;
        this.gpsMinDistance = gpsMinDistance;
        this.gpsMinTime = gpsMinTime;
        this.sensorFfequencyHz = sensorFfequencyHz;
        this.geoHashPrecision = geoHashPrecision;
        this.geoHashPointMinCount = geoHashPointMinCount;
        this.logToFile = logToFile;
    }

    public double getAccelerationDeviation() {
        return accelerationDeviation;
    }

    public int getGpsMinDistance() {
        return gpsMinDistance;
    }

    public int getGpsMinTime() {
        return gpsMinTime;
    }

    public int getSensorFfequencyHz() {
        return sensorFfequencyHz;
    }

    public int getGeoHashPrecision() {
        return geoHashPrecision;
    }

    public int getGeoHashPointMinCount() {
        return geoHashPointMinCount;
    }

    public boolean isLogToFile() { return logToFile; }
}
