package mad.location.manager.lib.Services;

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
    /**
     * If value of onlyGpsSensor is true, the location is determined using GNSS satellites.
     * if the value is false, LocationManager intrinsically uses fused location provider.
     * Fused location provider combines may combine inputs from several location sources to provide the
     * best possible location fix.
     */
    public boolean onlyGpsSensor;
    public boolean useGpsSpeed;

    public double mVelFactor;
    public double mPosFactor;
    LocationProvider provider;
    public enum LocationProvider { GPS, FUSED}


    public Settings(double accelerationDeviation,
                    int gpsMinDistance,
                    int gpsMinTime,
                    int positionMinTime,
                    int geoHashPrecision,
                    int geoHashMinPointCount,
                    double sensorFrequencyHz,
                    ILogger logger,
                    boolean filterMockGpsCoordinates,
                    boolean onlyGpsSensor,
                    boolean useGpsSpeed,
                    double velFactor,
                    double posFactor,
                    LocationProvider provider) {
        this.accelerationDeviation = accelerationDeviation;
        this.gpsMinDistance = gpsMinDistance;
        this.gpsMinTime = gpsMinTime;
        this.positionMinTime = positionMinTime;
        this.geoHashPrecision = geoHashPrecision;
        this.geoHashMinPointCount = geoHashMinPointCount;
        this.sensorFrequencyHz = sensorFrequencyHz;
        this.logger = logger;
        this.filterMockGpsCoordinates = filterMockGpsCoordinates;
        this.onlyGpsSensor = onlyGpsSensor;
        this.useGpsSpeed = useGpsSpeed;
        this.mVelFactor = velFactor;
        this.mPosFactor = posFactor;
        this.provider = provider;
    }
}