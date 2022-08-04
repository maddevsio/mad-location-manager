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
import com.maddevs.logtransferobject.types.KalmanPredict;
import com.maddevs.logtransferobject.types.LocationRecord;
import com.maddevs.logtransferobject.types.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mad.location.manager.lib.Commons.SensorGpsDataItem;
import mad.location.manager.lib.enums.Direction;
import mad.location.manager.lib.logger.RawDataLogger;
import mad.location.manager.lib.logger.RawLogFileNameGenerator;
import mad.location.manager.lib.logger.RawLogSenderTask;

public class RawDataLoggerService implements RawDataLogger {
    private final Integer CHANK_SIZE = 10; ///TODO move to settings
    private final String LOG_TAG = "dataLogs";
    private Logger logger;

    private List<Record> datas;
    private final RawLogSenderTask rawLogSenderTask = new RawLogSenderTask();
    private static RawDataLoggerService instance = null;
    private Boolean started = FALSE;

    public static RawDataLoggerService getInstance(){
        if (instance==null){
            instance = new RawDataLoggerService();
        }

        return instance;
    }

    private RawDataLoggerService() {
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

        datas = new ArrayList<>();
    }

    @Override
    public void reset(){
        datas.clear();
        started = FALSE;
    }

    @Override
    public void start(){
        datas.clear();
        started = TRUE;
    }

    @Override
    public void stop(){
        sendDataToServer(datas);
        started = FALSE;
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
        String payload = String.format("%f,%f,%f",loc.getLatitude(), loc.getLongitude(), loc.getAltitude());
        LocationRecord record = new LocationRecord(payload);
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

        String payload = String.format(Locale.ENGLISH, "%f,%f,%f",
                absAcceleration[Direction.EAST.getCode()],
                absAcceleration[Direction.NORTH.getCode()],
                absAcceleration[Direction.UP.getCode()]);
        KalmanPredict record = new KalmanPredict(payload);
        writeRecord(record);
    }

    private void writeRecord(Record record) {
        datas.add(record);
        if (datas.size() == CHANK_SIZE) {
            sendDataToServer(datas);
            datas.clear();
        }
    }

    private void sendDataToServer(List<Record> records) {
        new RawLogSenderTask().execute(records);
    }
}
