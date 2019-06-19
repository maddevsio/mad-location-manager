package com.example.lezh1k.sensordatacollector;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy;
import com.elvishew.xlog.printer.file.naming.FileNameGenerator;
import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.Interfaces.ILogger;
import mad.location.manager.lib.Interfaces.LocationServiceInterface;
import mad.location.manager.lib.Loggers.GeohashRTFilter;
import mad.location.manager.lib.SensorAux.SensorCalibrator;
import mad.location.manager.lib.Services.KalmanLocationService;
import mad.location.manager.lib.Services.ServicesHelper;
import com.example.lezh1k.sensordatacollector.Interfaces.MapInterface;
import com.example.lezh1k.sensordatacollector.Presenters.MapPresenter;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LocationServiceInterface, MapInterface, ILogger {

    private SharedPreferences mSharedPref;

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

    ChangableFileNameGenerator xLogFileNameGenerator = new ChangableFileNameGenerator();
    public void initXlogPrintersFileName() {
        sdf.setTimeZone(TimeZone.getDefault());
        String dateStr = sdf.format(System.currentTimeMillis());
        String fileName = dateStr;
        final int secondsIn24Hour = 86400; //I don't think that it's possible to press button more frequently
        for (int i = 0; i < secondsIn24Hour; ++i) {
            fileName = String.format("%s_%d", dateStr, i);
            File f = new File(xLogFolderPath, fileName);
            if (!f.exists())
                break;
        }
        xLogFileNameGenerator.setFileName(fileName);
    }

    @Override
    public void log2file(String format, Object... args) {
        XLog.i(format, args);
    }


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
            TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
            TextView tvDistance = (TextView) findViewById(R.id.tvDistance);
            if (m_isLogging) {
                if (m_geoHashRTFilter == null)
                    return;

                tvDistance.setText(String.format(
                        "Distance (geo): %fm\n" +
                                "Distance (geo) HP: %fm\n" +
                                "Distance as is : %fm\n" +
                                "Distance as is HP: %fm",
                        m_geoHashRTFilter.getDistanceGeoFiltered(),
                        m_geoHashRTFilter.getDistanceGeoFilteredHP(),
                        m_geoHashRTFilter.getDistanceAsIs(),
                        m_geoHashRTFilter.getDistanceAsIsHP()));
            } else {
                if (!m_sensorCalibrator.isInProgress())
                    return;

                tvStatus.setText(m_sensorCalibrator.getCalibrationStatus());
                if (m_sensorCalibrator.getDcAbsLinearAcceleration().isCalculated() &&
                        m_sensorCalibrator.getDcLinearAcceleration().isCalculated()) {
                    set_isCalibrating(false, false);
                    tvDistance.setText(m_sensorCalibrator.getDcLinearAcceleration().deviationInfoString());
                }
            }
        }
    }
    /*********************************************************/

    private MapPresenter m_presenter;
    private MapboxMap m_map;
    private MapView m_mapView;

    private GeohashRTFilter m_geoHashRTFilter;
    private SensorCalibrator m_sensorCalibrator = null;
    private boolean m_isLogging = false;
    private boolean m_isCalibrating = false;
    private RefreshTask m_refreshTask = new RefreshTask(1000l, this);

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void set_isLogging(boolean isLogging) {
        Button btnStartStop = (Button) findViewById(R.id.btnStartStop);
        TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
        Button btnCalibrate = (Button) findViewById(R.id.btnCalibrate);
        String btnStartStopText;
        String btnTvStatusText;

        if (isLogging) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            m_presenter.stop();
            m_presenter.start();
            m_geoHashRTFilter.stop();
            m_geoHashRTFilter.reset(this);
            ServicesHelper.getLocationService(this, value -> {
                if (value.IsRunning()) {
                    return;
                }
                value.stop();
                initXlogPrintersFileName();
                KalmanLocationService.Settings settings =
                        new KalmanLocationService.Settings(
                                Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                                Integer.parseInt(mSharedPref.getString("pref_gps_min_distance", "")),
                                Integer.parseInt(mSharedPref.getString("pref_gps_min_time", "")),
                                Integer.parseInt(mSharedPref.getString("pref_position_min_time", "")),
                                Integer.parseInt(mSharedPref.getString("pref_geohash_precision", "")),
                                Integer.parseInt(mSharedPref.getString("pref_geohash_min_point", "")),
                                Double.parseDouble(mSharedPref.getString("pref_sensor_frequency", "")),
                                this,
                                false,
                                Utils.DEFAULT_VEL_FACTOR,
                                Utils.DEFAULT_POS_FACTOR
                        );
                value.reset(settings); //warning!! here you can adjust your filter behavior
                value.start();
            });

            btnStartStopText = "Stop tracking";
            btnTvStatusText = "Tracking is in progress";

        } else {
            btnStartStopText = "Start tracking";
            btnTvStatusText = "Paused";
            m_presenter.stop();
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

    public void btnStartStop_click(View v) {
        set_isLogging(!m_isLogging);
    }
    public void btnCalibrate_click(View v) {
        set_isCalibrating(!m_isCalibrating, true);
    }

    private void initActivity() {

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

        m_sensorCalibrator = new SensorCalibrator(sensorManager);
        ServicesHelper.getLocationService(this, value -> {
            set_isLogging(value.IsRunning());
        });
        set_isCalibrating(false, true);
    }

    //uncaught exceptions
    private Thread.UncaughtExceptionHandler defaultUEH;
    // handler listener
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            try {
                XLog.i("UNHANDLED EXCEPTION: %s, stack : %s", ex.toString(), ex.getStackTrace());
            } catch (Exception e) {
                Log.i("SensorDataCollector", String.format("Megaunhandled exception : %s, %s, %s",
                        e.toString(), ex.toString(), ex.getStackTrace()));
            }
            defaultUEH.uncaughtException(thread, ex);
        }
    };

    @Override
    public void locationChanged(Location location) {
        if (m_map != null && m_presenter != null) {
            if (!m_map.isMyLocationEnabled()) {
                m_map.setMyLocationEnabled(true);
                m_map.getMyLocationViewSettings().setForegroundTintColor(ContextCompat.getColor(this, R.color.red));
            }

            m_presenter.locationChanged(location, m_map.getCameraPosition());
        }
    }

    public static final int FILTER_KALMAN_ONLY = 0;
    public static final int FILTER_KALMAN_WITH_GEO = 1;
    public static final int GPS_ONLY = 2;
    private int routeColors[] = {R.color.mapbox_blue, R.color.colorAccent, R.color.green};
    private int routeWidths[] = {1, 3, 1};
    private Polyline lines[] = new Polyline[3];

    @Override
    public void showRoute(List<LatLng> route, int interestedRoute) {

        CheckBox cbGps, cbFilteredKalman, cbFilteredKalmanGeo;
        cbGps = (CheckBox) findViewById(R.id.cbGPS);
        cbFilteredKalman = (CheckBox) findViewById(R.id.cbFilteredKalman);
        cbFilteredKalmanGeo = (CheckBox) findViewById(R.id.cbFilteredKalmanGeo);
        boolean enabled[] = {cbFilteredKalman.isChecked(), cbFilteredKalmanGeo.isChecked(), cbGps.isChecked()};
        if (m_map != null) {
            runOnUiThread(() ->
                    m_mapView.post(() -> {
                        if (lines[interestedRoute] != null)
                            m_map.removeAnnotation(lines[interestedRoute]);

                        if (!enabled[interestedRoute])
                            route.clear(); //too many hacks here

                        lines[interestedRoute] = m_map.addPolyline(new PolylineOptions()
                                .addAll(route)
                                .color(ContextCompat.getColor(this, routeColors[interestedRoute]))
                                .width(routeWidths[interestedRoute]));
                    }));
        }
    }

    @Override
    public void moveCamera(CameraPosition position) {
        runOnUiThread(() ->
                m_mapView.postDelayed(() -> {
                    if (m_map != null) {
                        m_map.animateCamera(CameraUpdateFactory.newCameraPosition(position));
                    }
                }, 100));
    }

    @Override
    public void setAllGesturesEnabled(boolean enabled) {
        if (enabled) {
            m_mapView.postDelayed(() -> {
                m_map.getUiSettings().setScrollGesturesEnabled(true);
                m_map.getUiSettings().setZoomGesturesEnabled(true);
                m_map.getUiSettings().setDoubleTapGesturesEnabled(true);
            }, 500);
        } else {
            m_map.getUiSettings().setScrollGesturesEnabled(false);
            m_map.getUiSettings().setZoomGesturesEnabled(false);
            m_map.getUiSettings().setDoubleTapGesturesEnabled(false);
        }
    }

    public void setupMap(@Nullable Bundle savedInstanceState) {
        m_mapView = (MapView) findViewById(R.id.mapView);
        m_mapView.onCreate(savedInstanceState);

        m_presenter = new MapPresenter(this, this, m_geoHashRTFilter);
        m_mapView.getMapAsync(mapboxMap -> {
            m_map = mapboxMap;
            MainActivity this_ = this;
            ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage("Wait while map loading...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();

            m_map.setStyleUrl(BuildConfig.lightMapStyle);
            m_map.setStyleUrl(Style.SATELLITE_STREETS, new MapboxMap.OnStyleLoadedListener() {
                @Override
                public void onStyleLoaded(String style) {
                    m_map.getUiSettings().setLogoEnabled(false);
                    m_map.getUiSettings().setAttributionEnabled(false);
                    m_map.getUiSettings().setTiltGesturesEnabled(false);

                    int leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
                    int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
                    int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
                    int bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
                    m_map.getUiSettings().setCompassMargins(leftMargin, topMargin, rightMargin, bottomMargin);
                    ServicesHelper.addLocationServiceInterface(this_);
                    m_presenter.getRoute();
                    progress.dismiss();
                }
            });
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
        Mapbox.getInstance(this, BuildConfig.access_token);
        setContentView(R.layout.activity_main);
        m_geoHashRTFilter = new GeohashRTFilter(Utils.GEOHASH_DEFAULT_PREC, Utils.GEOHASH_DEFAULT_MIN_POINT_COUNT);
        setupMap(savedInstanceState);

        CheckBox cbGps, cbFilteredKalman, cbFilteredKalmanGeo;
        cbGps = (CheckBox) findViewById(R.id.cbGPS);
        cbFilteredKalman = (CheckBox) findViewById(R.id.cbFilteredKalman);
        cbFilteredKalmanGeo = (CheckBox) findViewById(R.id.cbFilteredKalmanGeo);
        CheckBox cb[] = {cbFilteredKalman, cbFilteredKalmanGeo, cbGps};
        for (int i = 0; i < 3; ++i) {
            if (cb[i] == null)
                continue;
            cb[i].setBackgroundColor(ContextCompat.getColor(this, routeColors[i]));
        }

        File esd = Environment.getExternalStorageDirectory();
        String storageState = Environment.getExternalStorageState();
        if (storageState != null && storageState.equals(Environment.MEDIA_MOUNTED)) {
            xLogFolderPath = String.format("%s/%s/", esd.getAbsolutePath(), "SensorDataCollector");
            Printer androidPrinter = new AndroidPrinter();             // Printer that print the log using android.util.Log
            initXlogPrintersFileName();
            Printer xLogFilePrinter = new FilePrinter
                    .Builder(xLogFolderPath)
                    .fileNameGenerator(xLogFileNameGenerator)
                    .backupStrategy(new FileSizeBackupStrategy(1024 * 1024 * 100)) //100MB for backup files
                    .build();
            XLog.init(LogLevel.ALL, androidPrinter, xLogFilePrinter);
        } else {
            //todo set some status
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set preferences data
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        initActivity();
        if (m_mapView != null) {
            m_mapView.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (m_mapView != null) {
            m_mapView.onStop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (m_mapView != null) {
            m_mapView.onResume();
        }
        m_refreshTask = new RefreshTask(1000, this);
        m_refreshTask.needTerminate = false;
        m_refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (m_mapView != null) {
            m_mapView.onPause();
        }

        m_refreshTask.needTerminate = true;
        m_refreshTask.cancel(true);
        if (m_sensorCalibrator != null) {
            m_sensorCalibrator.stop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (m_mapView != null) {
            m_mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (m_mapView != null) {
            m_mapView.onLowMemory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_mapView != null) {
            m_mapView.onDestroy();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
