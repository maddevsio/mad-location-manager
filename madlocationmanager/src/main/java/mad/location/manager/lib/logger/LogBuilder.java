package mad.location.manager.lib.logger;

import android.location.Location;

import java.util.Locale;

import mad.location.manager.lib.Commons.SensorGpsDataItem;

public class LogBuilder {

    public enum MessageType {
        KALMAN_ALLOC,
        KALMAN_PREDICT,
        KALMAN_UPDATE,
        GPS_DATA,
        ABS_ACC_DATA,
        FILTERED_GPS_DATA
    }

    public static String buildKalmanAlloc(long timeStamp, Location location, double accelerationDeviation) {
        return String.format(Locale.getDefault(),
                "%d%d KalmanAlloc : lon=%f, lat=%f, speed=%f, course=%f, m_accDev=%f, posDev=%f",
                MessageType.KALMAN_ALLOC.ordinal(),
                timeStamp,
                location.getLongitude(),
                location.getLatitude(),
                location.getSpeed(),
                location.getBearing(),
                accelerationDeviation,
                location.getAccuracy());
    }

    public static String buildKalmanPredict(SensorGpsDataItem item) {
        return String.format(Locale.getDefault(),
                "%d%d KalmanPredict : accX=%f, accY=%f",
                MessageType.KALMAN_PREDICT.ordinal(),
                (long) item.getTimestamp(),
                item.getAbsEastAcc(),
                item.getAbsNorthAcc());
    }

    public static String buildKalmanUpdate(SensorGpsDataItem item, double xVelocity, double yVelocity) {
        return String.format(Locale.getDefault(),
                "%d%d KalmanUpdate : pos lon=%f, lat=%f, xVel=%f, yVel=%f, posErr=%f, velErr=%f",
                MessageType.KALMAN_UPDATE.ordinal(),
                (long) item.getTimestamp(),
                item.getGpsLon(),
                item.getGpsLat(),
                xVelocity,
                yVelocity,
                item.getPosErr(),
                item.getVelErr());
    }

    public static String buildGpsData(long timeStamp, Location location, double velErr) {
        return String.format(Locale.getDefault(),
                "%d%d GPS : pos lat=%f, lon=%f, alt=%f, hdop=%f, speed=%f, bearing=%f, sa=%f",
                MessageType.GPS_DATA.ordinal(),
                timeStamp,
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                location.getAccuracy(),
                location.getSpeed(),
                location.getBearing(),
                velErr);
    }

    public static String buildAbsAccData(long timeStamp, float[] absAcceleration) {
        return String.format(Locale.getDefault(),
                "%d%d absAcc : %f %f %f",
                MessageType.ABS_ACC_DATA.ordinal(),
                timeStamp,
                absAcceleration[0]/* east */,
                absAcceleration[1]/* north */,
                absAcceleration[2]/* up */);
    }

    public static String buildFilteredGpsData(Location location) {
        return String.format(Locale.getDefault(),
                "%d%d FKS : lat=%f, lon=%f, alt=%f",
                MessageType.FILTERED_GPS_DATA.ordinal(),
                location.getTime(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());
    }
}
