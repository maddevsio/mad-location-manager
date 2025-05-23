package com.example.mlmexample;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy2;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.example.mlmexample.calibration.AbsAccelerometerCalibrator;
import com.example.mlmexample.loggers.AbsAccelerometerLogger;
import com.example.mlmexample.loggers.GPSLogger;
import com.example.mlmexample.sensors.AbsAccelerometerSensor;
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


public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("mlm_filter");
    }

    private AbsAccelerometerSensor m_accLogger;
    private GPSSensor m_GPSLogger;
    private final List<ISensor> m_sensors = new ArrayList<>();
    private ActivityMainBinding m_binding;
    private boolean m_isLogging = false;


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

    private boolean initXLog() {
        String storageState = Environment.getExternalStorageState();
        if (storageState == null || !storageState.equals(Environment.MEDIA_MOUNTED))
            return false;

        File esd = getExternalFilesDir(null);
        FileNameGenerator xLogFileNameGenerator = new DateFileNameGenerator();
        String xLogFolderPath = String.format(Locale.US, "%s/%s/", esd.getAbsolutePath(), "SensorDataCollector");
        Printer androidPrinter = new AndroidPrinter(); // Printer that print the log using android.util.Log
        Printer xLogFilePrinter = new FilePrinter
                .Builder(xLogFolderPath)
                .backupStrategy(new FileSizeBackupStrategy2(300 * 1024 * 1024, 200))
                .fileNameGenerator(xLogFileNameGenerator)
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

        TextView tv = m_binding.lblSampleText;
        if (!initXLog()) {
            tv.setText("Failed to init XLog");
            return;
        }

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        m_accLogger = new AbsAccelerometerSensor(sensorManager, windowManager);
//        m_GPSLogger = new GPSSensor(locationManager, this.getApplicationContext());
        m_accLogger = new AbsAccelerometerLogger(sensorManager, windowManager);
        m_GPSLogger = new GPSLogger(locationManager, this.getApplicationContext());
        m_sensors.add(m_accLogger);
        m_sensors.add(m_GPSLogger);
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

        // todo move to onCreate
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        AbsAccelerometerCalibrator c = new AbsAccelerometerCalibrator(sensorManager, windowManager);
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
                        String txt = String.format(Locale.US, "%f\n%f\n%f\n", c.Offset_X(), c.Offset_Y(), c.Offset_Z());
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