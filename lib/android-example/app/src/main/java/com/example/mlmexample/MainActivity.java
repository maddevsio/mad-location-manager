package com.example.mlmexample;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy2;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.example.mlmexample.calibration.ENUAccelerometerCalibrator;
import com.example.mlmexample.loggers.ENUAccelerometerLogger;
import com.example.mlmexample.loggers.GPSLogger;
import com.example.mlmexample.loggers.RawENULogger;
import com.example.mlmexample.sensors.ENUAccelerometerSensor;
import com.example.mlmexample.sensors.GPSSensor;
import com.example.mlmexample.sensors.ISensor;
import com.example.mlmexample.databinding.ActivityMainBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.naming.FileNameGenerator;
import com.example.mlmexample.sensors.RawENUSensor;


public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("mlm_filter");
    }

    private ENUAccelerometerSensor m_accLogger;
    private GPSSensor m_GPSLogger;
    private RawENUSensor m_rawENULogger;
    private final List<ISensor> m_sensors = new ArrayList<>();
    private ActivityMainBinding m_binding;
    private boolean m_isLogging = false;

    // LOGGER
    private String xLogFolderPath;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    class ChangableFileNameGenerator implements FileNameGenerator {
        private String fileName;

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public ChangableFileNameGenerator() {
        }

        @Override
        public boolean isFileNameChangeable() {
            return true;
        }

        @Override
        public String generateFileName(int logLevel, long timestamp) {
            return fileName;
        }
    }
    // /LOGGER

    @Override
    protected void onStart() {
        super.onStart();
        initActivity();
    }

    private void initActivity() {
        String[] interestedPermissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> lstPermissions = new ArrayList<>(interestedPermissions.length);
        for (String perm : interestedPermissions) {
            if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                lstPermissions.add(perm);
            }
        }

        if (!lstPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, lstPermissions.toArray(new String[0]),
                    100);
        }
    }

    private ChangableFileNameGenerator xLogFileNameGenerator = new ChangableFileNameGenerator();

    public void initXlogPrintersFileName() {
        sdf.setTimeZone(TimeZone.getDefault());
        String dateStr = sdf.format(System.currentTimeMillis());
        String fileName = dateStr;
        final int secondsIn24Hour = 86400; //I don't think that it's possible to press button more frequently
        for (int i = 0; i < secondsIn24Hour; ++i) {
            fileName = String.format(Locale.US, "%s_%d", dateStr, i);
            File f = new File(xLogFolderPath, fileName);
            if (!f.exists())
                break;
        }
        xLogFileNameGenerator.setFileName(fileName);
    }

    private boolean initXLog() {
        String storageState = Environment.getExternalStorageState();
        if (storageState == null || !storageState.equals(Environment.MEDIA_MOUNTED))
            return false;

        File esd = getExternalFilesDir(null);
        if (esd == null)
            return false;

        xLogFolderPath = String.format(Locale.US, "%s/%s/", esd.getAbsolutePath(), "SensorDataCollector");
        Printer androidPrinter = new AndroidPrinter();
        initXlogPrintersFileName();
        Printer xLogFilePrinter = new FilePrinter
                .Builder(xLogFolderPath)
                .fileNameGenerator(xLogFileNameGenerator)
                .backupStrategy(new FileSizeBackupStrategy2(300 * 1024 * 1024, 200))
                .build();

        XLog.init(LogLevel.ALL, androidPrinter, xLogFilePrinter);
        System.out.println(xLogFolderPath);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        m_binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(m_binding.getRoot());
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean useOnlySensors = false; // to use loggers set to false
        if (useOnlySensors) {
            m_accLogger = new ENUAccelerometerSensor(sensorManager);
            m_GPSLogger = new GPSSensor(locationManager, this.getApplicationContext());
            m_rawENULogger = new RawENUSensor(sensorManager);
        } else {
            m_accLogger = new ENUAccelerometerLogger(sensorManager);
            m_GPSLogger = new GPSLogger(locationManager, this.getApplicationContext());
            m_rawENULogger = new RawENULogger(sensorManager);
        }
//        m_sensors.add(m_accLogger);
        m_sensors.add(m_GPSLogger);
        m_sensors.add(m_rawENULogger);
    }

    public void btnStartStop_click(View v) {
        m_isLogging = !m_isLogging;
        if (!m_isLogging) {
            m_binding.btnStartStop.setText("Start");
            for (ISensor mDataLogger : m_sensors) {
                mDataLogger.stop();
            }
            return;
        }

        initXLog();
        m_binding.btnStartStop.setText("Stop");
        for (ISensor mDataLogger : m_sensors) {
            mDataLogger.start();
        }

        new Thread(() -> {
            while (m_accLogger.is_active()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float[] enu = m_accLogger.ENU();
                        float east = enu[0];
                        float north = enu[1];
                        float up = enu[2];
                        String txt = String.format(Locale.US, "east: %f\nnorth: %f\nup: %f\n", east, north, up);
                        m_binding.lblSampleText.setText(txt);
                    }
                });
            }
        }).start();
    }

    public void btnCalibrate_click(View v) {
        m_binding.btnCalibrate.setText("Calibrating");
        m_binding.btnStartStop.setEnabled(false);
        m_binding.btnCalibrate.setEnabled(false);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ENUAccelerometerCalibrator c = new ENUAccelerometerCalibrator(sensorManager);
        c.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000 && c.is_active(); ++i) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Log.d("CalibratingThread", "Calibrating is finished");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String txt = String.format(Locale.US, "%f\n%f\n%f\n", c.EastOffset(), c.NorthOffset(), c.UpOffset());
                        m_binding.lblSampleText.setText(txt);
                        m_binding.btnCalibrate.setText("Calibrate");
                        m_binding.btnStartStop.setEnabled(true);
                        m_binding.btnCalibrate.setEnabled(true);
                    }
                });
            }
        }).start();

    }

    /**
     * A native method that is implemented by the 'mlmexample' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native double processAcc(double x, double y, double z, double ts);

    public native double processGPS(double latitude, double longitude);
}