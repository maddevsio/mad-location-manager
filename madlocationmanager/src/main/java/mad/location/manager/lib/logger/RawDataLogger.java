package mad.location.manager.lib.logger;

import android.location.Location;

import mad.location.manager.lib.Commons.SensorGpsDataItem;

public interface RawDataLogger {
    void reset();

    String start();

    void stop();

    void logGpsData(Location loc);

    void logKalmanPredict(SensorGpsDataItem sdi);

    void logLinearAcceleration(long nowMs, float[] absAcceleration);

    void logKalmanUpdate(SensorGpsDataItem sdi, double xVel, double yVel);
}
