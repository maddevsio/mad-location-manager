package mad.location.manager.lib.logger.Impl;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import androidx.annotation.Nullable;

import mad.location.manager.lib.Commons.SensorGpsDataItem;
import mad.location.manager.lib.logger.RawDataLogger;

public class RawDataLoggerDefault extends Service {

//    @Override
//    public void log2file(String format, Object... args) {
//        System.out.println("RawDataLoger => log2file");
//    }
//
//    @Override
//    public void logGpsData(Location loc) {
//        System.out.println("RawDataLoger => logGpsData");
//    }
//
//    @Override
//    public void logKalmanPredict(SensorGpsDataItem sdi) {
//        System.out.println("RawDataLoger => logKalmanPredict");
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
