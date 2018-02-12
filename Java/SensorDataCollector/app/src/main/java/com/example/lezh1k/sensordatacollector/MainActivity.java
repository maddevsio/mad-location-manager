package com.example.lezh1k.sensordatacollector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.example.lezh1k.sensordatacollector.CommonClasses.Commons;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceInterface;
import com.example.lezh1k.sensordatacollector.Interfaces.MapInterface;
import com.example.lezh1k.sensordatacollector.Loggers.KalmanDistanceLogger;
import com.example.lezh1k.sensordatacollector.Presenters.MapPresenter;
import com.example.lezh1k.sensordatacollector.SensorsAux.SensorCalibrator;
import com.example.lezh1k.sensordatacollector.Services.KalmanLocationService;
import com.example.lezh1k.sensordatacollector.Services.ServicesHelper;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationServiceInterface, MapInterface {

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

                });
            } else {
                if (m_sensorCalibrator.isInProgress()) {
                    tvStatus.setText(m_sensorCalibrator.getCalibrationStatus());
                    if (m_sensorCalibrator.getDcAbsLinearAcceleration().isCalculated() &&
                            m_sensorCalibrator.getDcLinearAcceleration().isCalculated() &&
                            m_sensorCalibrator.getDcMeanLinearAcceleration().isCalculated()) {
                        set_isCalibrating(false, false);
                        tvDistance.setText(/*m_sensorCalibrator.getDcLinearAcceleration().deviationInfoString() +*/
                            m_sensorCalibrator.getDcMeanLinearAcceleration().deviationInfoString());
                    }


                }
            }
        }
    }
    /*********************************************************/

    MapPresenter m_presenter;
    Polyline m_routeKalmanOnly;
    Polyline m_routeKalmanWithGeo;
    Polyline m_routeGps;

    MapboxMap m_map;
    MapView m_mapView;

    private SensorCalibrator m_sensorCalibrator = null;
    private boolean m_isLogging = false;
    private boolean m_isCalibrating = false;
    RefreshTask m_refreshTask = new RefreshTask(1000l, this);

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
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        } else {
            btnStartStopText = "Start tracking";
            btnTvStatusText = "Paused";
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
                Log.i(Commons.AppName, String.format("Megaunhandled exception : %s, %s, %s",
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

            m_presenter.onLocationChanged(location, m_map.getCameraPosition());
        }
    }

    @Override
    public void showRoute(List<LatLng> route, int hack) {
        if (m_map != null) {
            runOnUiThread(() ->
                    m_mapView.post(() -> {
                        if (hack == 0) {
                            if (this.m_routeKalmanOnly != null) {
                                m_map.removeAnnotation(this.m_routeKalmanOnly);
                            }

                            this.m_routeKalmanOnly = m_map.addPolyline(new PolylineOptions()
                                    .addAll(route)
                                    .color(ContextCompat.getColor(this, R.color.colorAccent))
                                    .width(2));
                        }
                        if (hack == 1) {
                            if (this.m_routeKalmanWithGeo != null) {
                                m_map.removeAnnotation(this.m_routeKalmanWithGeo);
                            }

                            this.m_routeKalmanWithGeo = m_map.addPolyline(new PolylineOptions()
                                    .addAll(route)
                                    .color(ContextCompat.getColor(this, R.color.mapbox_blue))
                                    .width(2));

                        }
                        if (hack == 2) {
                            if (this.m_routeGps != null) {
                                m_map.removeAnnotation(this.m_routeGps);
                            }

                            this.m_routeGps = m_map.addPolyline(new PolylineOptions()
                                    .addAll(route)
                                    .color(ContextCompat.getColor(this, R.color.green))
                                    .width(2));

                        }
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

        m_presenter = new MapPresenter(this, this);
        m_mapView.getMapAsync(mapboxMap -> {
            m_map = mapboxMap;
            m_map.setStyleUrl(BuildConfig.lightMapStyle);

            m_map.getUiSettings().setLogoEnabled(false);
            m_map.getUiSettings().setAttributionEnabled(false);
            m_map.getUiSettings().setTiltGesturesEnabled(false);

            int leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
            int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            int bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            m_map.getUiSettings().setCompassMargins(leftMargin, topMargin, rightMargin, bottomMargin);

            if (m_routeKalmanOnly != null) {
                m_routeKalmanOnly.setMapboxMap(m_map);
            }

            ServicesHelper.addLocationServiceInterface(this);
            m_presenter.getRoute();
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
        Mapbox.getInstance(this, BuildConfig.access_token);
        setContentView(R.layout.activity_main);
        setupMap(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
}
