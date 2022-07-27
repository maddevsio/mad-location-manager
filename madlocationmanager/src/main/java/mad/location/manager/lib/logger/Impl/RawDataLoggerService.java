package mad.location.manager.lib.logger.Impl;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.elvishew.xlog.XLog;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import mad.location.manager.lib.Commons.SensorGpsDataItem;
import mad.location.manager.lib.logger.RawDataLogger;

public class RawDataLoggerService implements RawDataLogger {
    private final String LOG_TAG = "dataLogs";

    private static RawDataLoggerService instance = null;

    public static RawDataLoggerService getInstance(){
        if (instance==null){
            instance = new RawDataLoggerService();
        }

        return instance;
    }

    private RawDataLoggerService() {
    }

    @Override
    public void addObjectToLog(Object obj) {
        XLog.v(obj);
    }

    @Override
    public void log2file(String format, Object... args) {
        XLog.v(String.format(format, args));
    }

    @Override
    public void logGpsData(Location loc) {
        XLog.v(loc);
    }

    @Override
    public void logKalmanPredict(SensorGpsDataItem sdi) {
        XLog.v(sdi);
    }
}
