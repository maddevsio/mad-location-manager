package mad.location.manager.lib.Services.edlar;

import mad.location.manager.lib.Interfaces.ILogger;

public class Settings {
    public double accelerationDeviation;
    public int gpsMinDistance;
    public int gpsMinTime;
    public int positionMinTime;
    public int geoHashPrecision;
    public int geoHashMinPointCount;
    public double sensorFrequencyHz;
    public ILogger logger;
    public boolean filterMockGpsCoordinates;

    public double mVelFactor;
    public double mPosFactor;


    public Settings(double accelerationDeviation,
                    int gpsMinDistance,
                    int gpsMinTime,
                    int positionMinTime,
                    int geoHashPrecision,
                    int geoHashMinPointCount,
                    double sensorFrequencyHz,
                    ILogger logger,
                    boolean filterMockGpsCoordinates,
                    double velFactor,
                    double posFactor) {
        this.accelerationDeviation = accelerationDeviation;
        this.gpsMinDistance = gpsMinDistance;
        this.gpsMinTime = gpsMinTime;
        this.positionMinTime = positionMinTime;
        this.geoHashPrecision = geoHashPrecision;
        this.geoHashMinPointCount = geoHashMinPointCount;
        this.sensorFrequencyHz = sensorFrequencyHz;
        this.logger = logger;
        this.filterMockGpsCoordinates = filterMockGpsCoordinates;
        this.mVelFactor = velFactor;
        this.mPosFactor = posFactor;
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

    public int getPositionMinTime() {
        return positionMinTime;
    }

    public int getGeoHashPrecision() {
        return geoHashPrecision;
    }

    public int getGeoHashMinPointCount() {
        return geoHashMinPointCount;
    }

    public double getSensorFrequencyHz() {
        return sensorFrequencyHz;
    }

    public ILogger getLogger() {
        return logger;
    }

    public boolean isFilterMockGpsCoordinates() {
        return filterMockGpsCoordinates;
    }

    public double getmVelFactor() {
        return mVelFactor;
    }

    public double getmPosFactor() {
        return mPosFactor;
    }
}
