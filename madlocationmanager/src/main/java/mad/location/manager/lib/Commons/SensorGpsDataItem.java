package mad.location.manager.lib.Commons;

import androidx.annotation.NonNull;

/**
 * Created by lezh1k on 2/13/18.
 */

public class SensorGpsDataItem implements Comparable<SensorGpsDataItem> {
    public static final int GPS_DATA = 0;
    public static final int ACCELERATION_DATA = 1;
    public static final double NOT_INITIALIZED = Double.MIN_VALUE;

    private int dataType;
    private double timestamp;
    private double gpsLat;
    private double gpsLon;
    private double gpsAlt;
    private double absNorthAcc;
    private double absEastAcc;
    private double absUpAcc;
    private double speed;
    private double course;
    private double posErr;
    private double velErr;

    public SensorGpsDataItem(double timestamp,
                             double gpsLat,
                             double gpsLon,
                             double gpsAlt,
                             double speed,
                             double course,
                             double posErr,
                             double velErr,
                             double declination) {
        this(
                GPS_DATA,
                timestamp,
                gpsLat,
                gpsLon,
                gpsAlt,
                NOT_INITIALIZED,
                NOT_INITIALIZED,
                NOT_INITIALIZED,
                speed,
                course,
                posErr,
                velErr,
                declination
        );
    }

    public SensorGpsDataItem(double timestamp,
                             double absNorthAcc,
                             double absEastAcc,
                             double absUpAcc,
                             double declination) {
        this(
                ACCELERATION_DATA,
                timestamp,
                NOT_INITIALIZED,
                NOT_INITIALIZED,
                NOT_INITIALIZED,
                absNorthAcc,
                absEastAcc,
                absUpAcc,
                NOT_INITIALIZED,
                NOT_INITIALIZED,
                NOT_INITIALIZED,
                NOT_INITIALIZED,
                declination
        );
    }

    public SensorGpsDataItem(double timestamp,
                              double gpsLat,
                              double gpsLon,
                              double gpsAlt,
                              double absNorthAcc,
                              double absEastAcc,
                              double absUpAcc,
                              double speed,
                              double course,
                              double posErr,
                              double velErr,
                              double declination) {
        this(
                gpsLat != NOT_INITIALIZED ? GPS_DATA : ACCELERATION_DATA,
                timestamp,
                gpsLat,
                gpsLon,
                gpsAlt,
                absNorthAcc,
                absEastAcc,
                absUpAcc,
                speed,
                course,
                posErr,
                velErr,
                declination
        );
    }

    private SensorGpsDataItem(int dataType,
                              double timestamp,
                              double gpsLat,
                              double gpsLon,
                              double gpsAlt,
                              double absNorthAcc,
                              double absEastAcc,
                              double absUpAcc,
                              double speed,
                              double course,
                              double posErr,
                              double velErr,
                              double declination) {
        this.dataType = dataType;

        this.timestamp = timestamp;
        this.gpsLat = gpsLat;
        this.gpsLon = gpsLon;
        this.gpsAlt = gpsAlt;
        this.absNorthAcc = absNorthAcc;
        this.absEastAcc = absEastAcc;
        this.absUpAcc = absUpAcc;
        this.speed = speed;
        this.course = course;
        this.posErr = posErr;
        this.velErr = velErr;

        this.absNorthAcc = absNorthAcc * Math.cos(declination) + absEastAcc * Math.sin(declination);
        this.absEastAcc = absEastAcc * Math.cos(declination) - absNorthAcc * Math.sin(declination);
    }

    public int getDataType() {
        return dataType;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public double getGpsLat() {
        return gpsLat;
    }

    public double getGpsLon() {
        return gpsLon;
    }

    public double getGpsAlt() {
        return gpsAlt;
    }

    public double getAbsNorthAcc() {
        return absNorthAcc;
    }

    public double getAbsEastAcc() {
        return absEastAcc;
    }

    public double getAbsUpAcc() {
        return absUpAcc;
    }

    public double getSpeed() {
        return speed;
    }

    public double getCourse() {
        return course;
    }

    public double getPosErr() {
        return posErr;
    }

    public double getVelErr() {
        return velErr;
    }

    @Override
    public int compareTo(@NonNull SensorGpsDataItem o) {
        return (int) (this.timestamp - o.timestamp);
    }

}
