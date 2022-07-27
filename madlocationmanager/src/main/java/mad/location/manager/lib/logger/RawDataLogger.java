package mad.location.manager.lib.logger;

import android.location.Location;

import mad.location.manager.lib.Commons.SensorGpsDataItem;

public interface RawDataLogger {
    void log2file(String format, Object... args);

    void logGpsData(Location loc);

    void logKalmanPredict(SensorGpsDataItem sdi);
}
