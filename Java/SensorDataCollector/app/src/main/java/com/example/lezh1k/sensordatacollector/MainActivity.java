package com.example.lezh1k.sensordatacollector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.example.lezh1k.sensordatacollector.Loggers.AccelerationLogger;
import com.example.lezh1k.sensordatacollector.Loggers.GPSDataLogger;
import com.example.lezh1k.sensordatacollector.SensorDataProvider.DeviationCalculator;
import com.example.lezh1k.sensordatacollector.Loggers.SensorRawDataLogger;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.GSASentence;
import net.sf.marineapi.nmea.sentence.GSVSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.VTGSentence;
import net.sf.marineapi.nmea.util.Position;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LocationManager m_locationManager = null;
    private SensorManager m_sensorManager = null;
    private SensorRawDataLogger m_sensorRawDataLogger = null;
    private GPSDataLogger m_gpsDataLogger = null;
    private AccelerationLogger m_accDataLogger = null;
    private boolean m_isLogging = false;

    protected void onPause() {
        super.onPause();
        if (m_sensorRawDataLogger != null)
            m_sensorRawDataLogger.stop();
        if (m_gpsDataLogger != null)
            m_gpsDataLogger.stop();
        if (m_accDataLogger != null)
            m_accDataLogger.stop();
        m_isLogging = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void set_isLogging(boolean isLogging) {
        Button btnStartStop = (Button) findViewById(R.id.btnStartStop);
        if (btnStartStop != null) {
            btnStartStop.setText(isLogging ? "Stop" : "Start");
        }

        TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
        if (tvStatus != null) {
            tvStatus.setText(isLogging ? "In progress" : "Paused");
        }
        this.m_isLogging = isLogging;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!m_isLogging)
            return;

        if (m_gpsDataLogger != null && m_accDataLogger != null) {
            m_gpsDataLogger.start();
            m_accDataLogger.start();
        }
    }

    //used as handler!!! don't remove.
    public void btnStartStop_click(View v) {
        if (m_isLogging) {
            m_gpsDataLogger.stop();
            m_accDataLogger.stop();
            set_isLogging(false);
        } else {
            m_gpsDataLogger.start();
            m_accDataLogger.start();
            set_isLogging(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] interestedPermissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            interestedPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        } else {
            interestedPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
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

        m_sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (m_sensorManager == null) {
            System.exit(1);
        }

        m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (m_locationManager == null) {
            System.exit(2);
        }

        m_sensorRawDataLogger = new SensorRawDataLogger(m_sensorManager);
        m_gpsDataLogger = new GPSDataLogger(m_locationManager, this);
        m_accDataLogger = new AccelerationLogger(m_sensorManager);

        File esd = Environment.getExternalStorageDirectory();
        String storageState = Environment.getExternalStorageState();
        if (storageState != null && storageState.equals(Environment.MEDIA_MOUNTED)) {
            String logFolderPath = String.format("%s/%s/", esd.getAbsolutePath(), Commons.AppName);
            Printer androidPrinter = new AndroidPrinter();             // Printer that print the log using android.util.Log
            Printer filePrinter = new FilePrinter                      // Printer that print the log to the file system
                    .Builder(logFolderPath)                            // Specify the path to save log file
                    .fileNameGenerator(new DateFileNameGenerator())    // Date as file name
                    .backupStrategy(new FileSizeBackupStrategy(1024*1024*300)) //300MB for backup files
                    .build();
            XLog.init(LogLevel.ALL, androidPrinter, filePrinter);
            XLog.i("Application started!!!");
        } else {
            System.exit(3); //MUOHOHO
        }
    }
}
