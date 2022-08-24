package mad.location.manager.lib.logger.Impl;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.location.Location;
import android.os.Build;
import android.text.format.Time;

import androidx.annotation.RequiresApi;

import com.elvishew.xlog.XLog;
import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.types.ABSAcceleration;
import com.maddevs.logtransferobject.types.GpsData;
import com.maddevs.logtransferobject.types.KalmanPredict;
import com.maddevs.logtransferobject.types.KalmanUpdatePredict;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import mad.location.manager.lib.Commons.SensorGpsDataItem;
import mad.location.manager.lib.Services.Settings;
import mad.location.manager.lib.Utils.UniquePsuedoID;
import mad.location.manager.lib.enums.Direction;
import mad.location.manager.lib.logger.RawDataLogger;
import mad.location.manager.lib.logger.RawLogSenderTask;

@RequiresApi(api = Build.VERSION_CODES.O)
public class RawDataLoggerService implements RawDataLogger {
    private final String LOG_TAG = "dataLogs";
    //private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy(HH::mm:ss)");

    private List<Log> logs;
    private static RawDataLoggerService instance = null;
    private Boolean started = FALSE;
    private Integer debugCount = 0;
    private Settings settings;
    private final String uniquePhoneID = UniquePsuedoID.getUniquePsuedoID();
    private String uniqueTripID;

    public static RawDataLoggerService getInstance(){
        if (instance==null){
            instance = new RawDataLoggerService();

        }

        return instance;
    }

    private RawDataLoggerService() {
        settings = Settings.getInstance();
        logs = new ArrayList<>();
    }

    @Override
    public void reset(){
        logs.clear();
        uniqueTripID = null;
        started = FALSE;
    }

    @Override
    public String start(){
        logs.clear();
        Calendar now = Calendar.getInstance();
        uniqueTripID = String.format("%02d-%02d-%d(%02d:%02d:%02d)",
                now.get(Calendar.DATE), now.get(Calendar.MONTH), now.get(Calendar.YEAR),
                now.get(Calendar.HOUR), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
        started = TRUE;
        return uniqueTripID;
    }

    @Override
    public void stop(){
        sendDataToServer(logs);
        started = FALSE;
        XLog.i("=== FULL COUNT LOGS %s ===", debugCount);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void logGpsData(Location loc) {
        GpsData record = GpsData.builder()
                .timestamp(BigDecimal.valueOf(loc.getTime()))
                .lat(BigDecimal.valueOf(loc.getLatitude()))
                .lon(BigDecimal.valueOf(loc.getLongitude()))
                .alt(BigDecimal.valueOf(loc.getAltitude()))
                .build();
        writeRecord(record);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void logKalmanPredict(SensorGpsDataItem sdi) {
//        log2File("%d%d KalmanPredict : accX=%f, accY=%f",
//                    Utils.LogMessageType.KALMAN_PREDICT.ordinal(),
//                    (long) sdi.getTimestamp(),
//                    sdi.getAbsEastAcc(),
//                    sdi.getAbsNorthAcc());
        KalmanPredict kalmanPredict = KalmanPredict.builder()
                .timestamp(BigDecimal.valueOf(sdi.getTimestamp()))
                .absEastAcceleration(BigDecimal.valueOf(sdi.getAbsEastAcc()))
                .absNorthAcceleration(BigDecimal.valueOf(sdi.getAbsNorthAcc()))
                .absUpAcceleration(BigDecimal.valueOf(sdi.getAbsUpAcc()))
                .build();
        writeRecord(kalmanPredict);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void logLinearAcceleration(long nowMs, float[] absAcceleration) {
        ABSAcceleration record = ABSAcceleration.builder()
                .timestamp(BigDecimal.valueOf(nowMs))
                .eastAcceleration(BigDecimal.valueOf(absAcceleration[Direction.EAST.getCode()]))
                .northAcceleration(BigDecimal.valueOf(absAcceleration[Direction.NORTH.getCode()]))
                .upAcceleration(BigDecimal.valueOf(absAcceleration[Direction.UP.getCode()]))
                .build();

        writeRecord(record);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void logKalmanUpdate(SensorGpsDataItem sdi, double xVel, double yVel) {
        //            log2File("%d%d KalmanUpdate : pos lon=%f, lat=%f, xVel=%f, yVel=%f, posErr=%f, velErr=%f",
//                    Utils.LogMessageType.KALMAN_UPDATE.ordinal(),
//                    (long) sdi.getTimestamp(),
//                    sdi.getGpsLon(),
//                    sdi.getGpsLat(),
//                    xVel,
//                    yVel,
//                    sdi.getPosErr(),
//                    sdi.getVelErr()
//            );
       KalmanUpdatePredict kalmanUpdatePredict = KalmanUpdatePredict.builder()
               .timestamp(BigDecimal.valueOf(sdi.getTimestamp()))
               .gpsLon(BigDecimal.valueOf(sdi.getGpsLon()))
               .gpsLat(BigDecimal.valueOf(sdi.getGpsLat()))
               .xVel(BigDecimal.valueOf(xVel))
               .yVel(BigDecimal.valueOf(yVel))
               .posError(BigDecimal.valueOf(sdi.getPosErr()))
               .velError(BigDecimal.valueOf(sdi.getVelErr()))
       .build();

        writeRecord(kalmanUpdatePredict);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeRecord(Log log) {
        log.setTripUuid(uniqueTripID);
        logs.add(log);
        if (logs.size() < settings.chankSize) {
            return;
        }
        List<Log> records = new ArrayList<>();
        synchronized (this) {
            records.addAll(logs);
            logs.clear();
        }
        sendDataToServer(records);
    }

    private void sendDataToServer(List<Log> logsToWrite) {
        new RawLogSenderTask(uniquePhoneID).execute(logsToWrite);
    }
}
