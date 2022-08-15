package mad.location.manager.lib.logger.Impl;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.location.Location;
import android.os.Environment;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.types.KalmanPredict;
import com.maddevs.logtransferobject.types.LocationLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import mad.location.manager.lib.Commons.SensorGpsDataItem;
import mad.location.manager.lib.Services.Settings;
import mad.location.manager.lib.enums.Direction;
import mad.location.manager.lib.logger.RawDataLogger;
import mad.location.manager.lib.logger.RawLogFileNameGenerator;
import mad.location.manager.lib.logger.RawLogSenderTask;

public class RawDataLoggerService implements RawDataLogger {
    private final String LOG_TAG = "dataLogs";
    private Logger logger;

    private List<Log> logs;
    private static RawDataLoggerService instance = null;
    private Boolean started = FALSE;
    private Integer debugCount = 0;
    private Settings settings;

    public static RawDataLoggerService getInstance(){
        if (instance==null){
            instance = new RawDataLoggerService();
        }

        return instance;
    }

    private RawDataLoggerService() {
        settings = Settings.getInstance();
        Printer filePrinter = new FilePrinter
                .Builder(String.format("%s/%s/", Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "SensorDataCollector")
                )
                .fileNameGenerator(new RawLogFileNameGenerator())
                .backupStrategy(new NeverBackupStrategy())
//                .cleanStrategy(new FileLastModifiedCleanStrategy(MAX_TIME))
//                .flattener(new MyFlattener())
//                .writer(new MyWriter())
                .build();

         logger = XLog.tag(LOG_TAG)
                 //.addObjectFormatter(SensorGpsDataItem.class)
                 .printers(filePrinter)
                 .build();

        logs = new ArrayList<>();
    }

    @Override
    public void reset(){
        logs.clear();
        started = FALSE;
    }

    @Override
    public void start(){
        logs.clear();
        started = TRUE;
    }

    @Override
    public void stop(){
        sendDataToServer(logs);
        started = FALSE;
        XLog.i("=== FULL COUNT LOGS %s ===", debugCount);
    }

    @Override
    public void addObjectToLog(Object obj) {
        logger.v(obj);
    }

    @Override
    public void log2file(String format, Object... args) {
        logger.v(String.format(format, args));
    }

    @Override
    public void logGpsData(Location loc) {
        LocationLog record = LocationLog.builder().build();
        writeRecord(record);
    }

    @Override
    public void logKalmanPredict(SensorGpsDataItem sdi) {
//        Record record = new Record();
//        record.setType(RecordType.KALMAN_FILTER);
//        String payload = String.format("%f,%f", sdi.getAbsEastAcc(), sdi.getAbsNorthAcc());
//        record.setValue(payload);
//        writeRecord(record);
//        logger.v(payload);
    }

    @Override
    public void logLinearAcceleration(float[] absAcceleration) {
//                String logStr = String.format(Locale.ENGLISH, "%d%d abs acc: %f %f %f",
//                        Utils.LogMessageType.ABS_ACC_DATA.ordinal(),
//                        nowMs, absAcceleration[east], absAcceleration[north], absAcceleration[up]);

//        String payload = String.format(Locale.ENGLISH, "%f,%f,%f",
//                absAcceleration[Direction.EAST.getCode()],
//                absAcceleration[Direction.NORTH.getCode()],
//                absAcceleration[Direction.UP.getCode()]);
        KalmanPredict record = KalmanPredict.builder()
                .absEastAcceleration(BigDecimal.valueOf(absAcceleration[Direction.EAST.getCode()]))
                .absNorthAcceleration(BigDecimal.valueOf(absAcceleration[Direction.NORTH.getCode()]))
                .absUpAcceleration(BigDecimal.valueOf(absAcceleration[Direction.UP.getCode()]))
                .build();

        writeRecord(record);
    }

    private void writeRecord(Log log) {
        logs.add(log);
        debugCount = debugCount+1;
        if (logs.size() < settings.chankSize) {
            return;
        }
        sendDataToServer(logs);
        logs.clear();
    }

    private void sendDataToServer(List<Log> logsToWrite) {
        new RawLogSenderTask().execute(logsToWrite);
    }
}
