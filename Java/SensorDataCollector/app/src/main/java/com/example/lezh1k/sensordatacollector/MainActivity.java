package com.example.lezh1k.sensordatacollector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.elvishew.xlog.printer.file.naming.FileNameGenerator;
import com.example.lezh1k.sensordatacollector.CommonClasses.Commons;
import com.example.lezh1k.sensordatacollector.Loggers.AccelerationLogger;
import com.example.lezh1k.sensordatacollector.Loggers.GPSDataLogger;
import com.example.lezh1k.sensordatacollector.Loggers.KalmanDistanceLogger;
import com.example.lezh1k.sensordatacollector.SensorsAux.SensorCalibrator;
import com.example.lezh1k.sensordatacollector.Services.KalmanLocationService;
import com.example.lezh1k.sensordatacollector.Services.ServicesHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    class RefreshTask extends AsyncTask {
        boolean needTerminate = false;
        long deltaT;
        Context owner;
        RefreshTask(long deltaTMs, Context owner) {
            this.owner = owner;
            this.deltaT = deltaTMs;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            while (!needTerminate) {
                try {
                    Thread.sleep(deltaT);
                    publishProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            TextView tvLinAcc = (TextView) findViewById(R.id.tvLinAccelerometer);
            TextView tvLinAccData = (TextView) findViewById(R.id.tvLinAccelerometerData);
            TextView tvLocationData = (TextView) findViewById(R.id.tvLocationData);
            TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
            TextView tvDistance = (TextView) findViewById(R.id.tvDistance);

            tvLinAccData.setText(String.format("Acceleration:\n" +
                            "Lin:%s\n" +
                            "Abs:%s",
                    m_accDataLogger.getLastLinAccelerationString(),
                    m_accDataLogger.getLastAbsAccelerationString()));

            tvLinAcc.setText(String.format("Lin acc: %s\n%s",
                    m_sensorCalibrator.getDcAbsLinearAcceleration().deviationInfoString(),
                    m_sensorCalibrator.getDcLinearAcceleration().deviationInfoString()));

            ServicesHelper.getLocationService(owner, value -> {
                KalmanLocationService kls = (KalmanLocationService) value;
                KalmanDistanceLogger kdl = kls.getDistanceLogger();
                tvDistance.setText(String.format("Distance (geo): %fm\n" +
                                "Distance (geo) HP: %fm\n" +
                                "Distance as is : %fm\n" +
                                "Distance as is HP: %fm",
                        kdl.getDistanceGeoFiltered(),
                        kdl.getDistanceGeoFilteredHP(),
                        kdl.getDistanceAsIs(),
                        kdl.getDistanceAsIsHP()));
                tvLocationData.setText(String.format("Location:\n%s\n%s",
                        m_gpsDataLogger.getLastLoggedGPSMessage(),
                        kdl.getLastFilteredLocationString()));
            });

            if (m_sensorCalibrator.isInProgress()) {
                tvStatus.setText(m_sensorCalibrator.getCalibrationStatus());
                if( m_sensorCalibrator.getDcAbsLinearAcceleration().isCalculated() &&
                        m_sensorCalibrator.getDcLinearAcceleration().isCalculated()) {
                    set_isCalibrating(false, false);
                }
            }
        }
    }
    /*********************************************************/

    private GPSDataLogger m_gpsDataLogger = null;
    private AccelerationLogger m_accDataLogger = null;
    private SensorCalibrator m_sensorCalibrator = null;

    private boolean m_isLogging = false;
    private boolean m_isCalibrating = false;
    RefreshTask m_refreshTask = new RefreshTask(1000l, this);

    protected void onPause() {
        super.onPause();
        m_refreshTask.needTerminate = true;
        m_refreshTask.cancel(true);
        if (m_gpsDataLogger != null) {
            m_gpsDataLogger.stop();
        }
        if (m_accDataLogger != null) {
            m_accDataLogger.stop();
        }
        if (m_sensorCalibrator != null) {
            m_sensorCalibrator.stop();
        }
        m_isLogging = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //todo change to state machine
    private void set_isLogging(boolean isLogging) {
        Button btnStartStop = (Button) findViewById(R.id.btnStartStop);
        TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
        Button btnCalibrate = (Button) findViewById(R.id.btnCalibrate);
        String btnStartStopText;
        String btnTvStatusText;

        if (isLogging) {
            ServicesHelper.getLocationService(this, value -> {
                if (value.IsRunning())
                    return;
                KalmanLocationService kls = (KalmanLocationService) value;
                kls.initXlogPrintersFileName();
                value.stop();
                value.reset();
                value.start();
            });
            btnStartStopText = "Stop tracking";
            btnTvStatusText = "Tracking is in progress";
            m_gpsDataLogger.start();
            m_accDataLogger.start();

        } else {
            btnStartStopText = "Start tracking";
            btnTvStatusText = "Paused";
            m_gpsDataLogger.stop();
            m_accDataLogger.stop();
            ServicesHelper.getLocationService(this, value -> {
                value.stop();
            });
        }

        if (btnStartStop != null)
            btnStartStop.setText(btnStartStopText);
        if (tvStatus != null)
            tvStatus.setText(btnTvStatusText);

        btnCalibrate.setEnabled(!isLogging);
        m_isLogging = isLogging;
    }

    //todo change to state machine
    private void set_isCalibrating(boolean isCalibrating, boolean byUser) {
        Button btnStartStop = (Button) findViewById(R.id.btnStartStop);
        TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
        Button btnCalibrate = (Button) findViewById(R.id.btnCalibrate);
        String btnCalibrateText;
        String tvStatusText;

        if (isCalibrating) {
            btnCalibrateText = "Stop calibration";
            tvStatusText = "Calibrating";
            m_sensorCalibrator.reset();
            m_sensorCalibrator.start();
        } else {
            btnCalibrateText = "Start calibration";
            tvStatusText = byUser ? "Calibration finished by user" : "Calibration finished";
            m_sensorCalibrator.stop();
        }

        btnCalibrate.setText(btnCalibrateText);
        tvStatus.setText(tvStatusText);
        btnStartStop.setEnabled(!isCalibrating);
        m_isCalibrating = isCalibrating;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        m_refreshTask = new RefreshTask(1000, this);
        m_refreshTask.needTerminate = false;
        m_refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void btnStartStop_click(View v) {
        set_isLogging(!m_isLogging);
    }

    public void btnCalibrate_click(View v) {
        set_isCalibrating(!m_isCalibrating, true);
    }

    private void initActivity() {
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

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (sensorManager == null || locationManager == null) {
            System.exit(1);
        }

        m_gpsDataLogger = new GPSDataLogger(locationManager, this);
        m_accDataLogger = new AccelerationLogger(sensorManager);
        m_sensorCalibrator = new SensorCalibrator(sensorManager);

        ServicesHelper.getLocationService(this, value -> {
            set_isLogging(value.IsRunning());
        });
        set_isCalibrating(false, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
