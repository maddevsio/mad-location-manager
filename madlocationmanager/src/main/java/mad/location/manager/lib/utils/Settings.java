package mad.location.manager.lib.utils;

public class Settings {

    public static final boolean DEFAULT_LOGGER_ENABLED = false;
    public static final double DEFAULT_ACCELEROMETER_DEVIATION = 0.1;
    public static final int DEFAULT_GPS_MIN_TIME = 2000;
    public static final int DEFAULT_GPS_MIN_DISTANCE = 0;
    public static final int DEFAULT_SENSOR_POSITION_MIN_TIME = 500;
    public static final int DEFAULT_GEOHASH_PREC = 6;
    public static final int DEFAULT_GEOHASH_MIN_POINT_COUNT = 2;
    public static final double DEFAULT_SENSOR_FREQ_HZ = 10.0;
    public static final boolean DEFAULT_FILTER_MOCK_COORDINATES = true;
    public static final double DEFAULT_VEL_FACTOR = 1.0;
    public static final double DEFAULT_POS_FACTOR = 1.0;

    private boolean loggerEnabled;
    private double accelerationDeviation;
    private int gpsMinDistance;
    private int gpsMinTime;
    private int positionMinTime;
    private int geoHashPrecision;
    private int geoHashMinPointCount;
    private double sensorFrequencyHz;
    private boolean filterMockGpsCoordinates;
    private double velFactor;
    private double posFactor;

    public Settings() {
        this(
                DEFAULT_ACCELEROMETER_DEVIATION,
                DEFAULT_GPS_MIN_DISTANCE,
                DEFAULT_GPS_MIN_TIME,
                DEFAULT_SENSOR_POSITION_MIN_TIME,
                DEFAULT_GEOHASH_PREC,
                DEFAULT_GEOHASH_MIN_POINT_COUNT,
                DEFAULT_SENSOR_FREQ_HZ,
                DEFAULT_FILTER_MOCK_COORDINATES,
                DEFAULT_VEL_FACTOR,
                DEFAULT_POS_FACTOR
        );
    }

    public Settings(double accelerationDeviation,
                    int gpsMinDistance,
                    int gpsMinTime,
                    int positionMinTime,
                    int geoHashPrecision,
                    int geoHashMinPointCount,
                    double sensorFrequencyHz,
                    boolean filterMockGpsCoordinates,
                    double velFactor,
                    double posFactor) {
        loggerEnabled = DEFAULT_LOGGER_ENABLED;
        this.accelerationDeviation = accelerationDeviation;
        this.gpsMinDistance = gpsMinDistance;
        this.gpsMinTime = gpsMinTime;
        this.positionMinTime = positionMinTime;
        this.geoHashPrecision = geoHashPrecision;
        this.geoHashMinPointCount = geoHashMinPointCount;
        this.sensorFrequencyHz = sensorFrequencyHz;
        this.filterMockGpsCoordinates = filterMockGpsCoordinates;
        this.velFactor = velFactor;
        this.posFactor = posFactor;
    }

    public boolean isLoggerEnabled() {
        return loggerEnabled;
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

    public boolean isFilterMockGpsCoordinates() {
        return filterMockGpsCoordinates;
    }

    public double getVelFactor() {
        return velFactor;
    }

    public double getPosFactor() {
        return posFactor;
    }

    public Settings setLoggerEnabled(boolean loggerEnabled) {
        this.loggerEnabled = loggerEnabled;
        return this;
    }

    public Settings setAccelerationDeviation(double accelerationDeviation) {
        this.accelerationDeviation = accelerationDeviation;
        return this;
    }

    public Settings setGpsMinDistance(int gpsMinDistance) {
        this.gpsMinDistance = gpsMinDistance;
        return this;
    }

    public Settings setGpsMinTime(int gpsMinTime) {
        this.gpsMinTime = gpsMinTime;
        return this;
    }

    public Settings setPositionMinTime(int positionMinTime) {
        this.positionMinTime = positionMinTime;
        return this;
    }

    public Settings setGeoHashPrecision(int geoHashPrecision) {
        this.geoHashPrecision = geoHashPrecision;
        return this;
    }

    public Settings setGeoHashMinPointCount(int geoHashMinPointCount) {
        this.geoHashMinPointCount = geoHashMinPointCount;
        return this;
    }

    public Settings setSensorFrequencyHz(double sensorFrequencyHz) {
        this.sensorFrequencyHz = sensorFrequencyHz;
        return this;
    }

    public Settings setFilterMockGpsCoordinates(boolean filterMockGpsCoordinates) {
        this.filterMockGpsCoordinates = filterMockGpsCoordinates;
        return this;
    }

    public Settings setVelFactor(double velFactor) {
        this.velFactor = velFactor;
        return this;
    }

    public Settings setPosFactor(double posFactor) {
        this.posFactor = posFactor;
        return this;
    }
}
