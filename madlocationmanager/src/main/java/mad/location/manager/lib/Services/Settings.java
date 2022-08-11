package mad.location.manager.lib.Services;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.logger.Impl.RawDataLoggerService;

public class Settings {
    public double accelerationDeviation;
    public int gpsMinDistance;
    public int gpsMinTime;
    public int positionMinTime;
    public int geoHashPrecision;
    public int geoHashMinPointCount;
    public double sensorFrequencyHz;
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

    public static Settings getDefaultSettings() {
        Settings defaultSettings = new Settings();
        defaultSettings.load(
                Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                Utils.GPS_MIN_DISTANCE,
                Utils.GPS_MIN_TIME,
                Utils.SENSOR_POSITION_MIN_TIME,
                Utils.GEOHASH_DEFAULT_PREC,
                Utils.GEOHASH_DEFAULT_MIN_POINT_COUNT,
                Utils.SENSOR_DEFAULT_FREQ_HZ,
                true,
                true,
                false,
                Utils.DEFAULT_VEL_FACTOR,
                Utils.DEFAULT_POS_FACTOR,
                Settings.LocationProvider.GPS,
                "localhost:8080",
                1000
        );
        return defaultSettings;
    }

    public enum LocationProvider { GPS, FUSED}
    public String server;
    public Integer chankSize;

    private static Settings instance = null;

    public static Settings getInstance(){
        if (instance==null){
            instance = new Settings();
        }

        return instance;
    }

    public void load(double accelerationDeviation, int gpsMinDistance, int gpsMinTime,
                     int positionMinTime, int geoHashPrecision, int geoHashMinPointCount,
                     double sensorFrequencyHz, boolean filterMockGpsCoordinates,
                     boolean onlyGpsSensor, boolean useGpsSpeed, double velFactor, double posFactor,
                     LocationProvider provider, String server, Integer chankSize) {
        this.accelerationDeviation = accelerationDeviation;
        this.gpsMinDistance = gpsMinDistance;
        this.gpsMinTime = gpsMinTime;
        this.positionMinTime = positionMinTime;
        this.geoHashPrecision = geoHashPrecision;
        this.geoHashMinPointCount = geoHashMinPointCount;
        this.sensorFrequencyHz = sensorFrequencyHz;
        this.filterMockGpsCoordinates = filterMockGpsCoordinates;
        this.onlyGpsSensor = onlyGpsSensor;
        this.useGpsSpeed = useGpsSpeed;
        this.mVelFactor = velFactor;
        this.mPosFactor = posFactor;
        this.provider = provider;
        this.server = server;
        this.chankSize = chankSize;
    }
}