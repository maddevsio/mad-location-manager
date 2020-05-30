package com.example.lezh1k.sensordatacollector;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lezh1k.sensordatacollector.Interfaces.TestInterface;
import com.example.lezh1k.sensordatacollector.Presenters.TestPresenter;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

public class TestActivity extends AppCompatActivity implements TestInterface {
    private static final int REQUEST_CODE = 330;

    private MapView map_view;
    private CheckBox show_gps;
    private CheckBox show_kalman;
    private CheckBox show_geohash;

    private MapboxMap map;
    private TestPresenter presenter;

    private Polyline rawLocationsPolyline;
    private Polyline kalmanFilteredLocationsPolyline;
    private Polyline geoHashFilteredLocationsPolyline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, BuildConfig.access_token);

        setContentView(R.layout.activity_test);

        map_view = findViewById(R.id.map_view);
        show_gps = findViewById(R.id.show_gps);
        show_kalman = findViewById(R.id.show_kalman);
        show_geohash = findViewById(R.id.show_geohash);
        View start = findViewById(R.id.start);
        View stop = findViewById(R.id.stop);
        View enable_log = findViewById(R.id.enable_log);
        View disable_log = findViewById(R.id.disable_log);
        View share = findViewById(R.id.share);
        View delete_log = findViewById(R.id.delete_log);

        start.setOnClickListener(view -> presenter.startService());
        stop.setOnClickListener(view -> presenter.stopService());
        enable_log.setOnClickListener(view -> presenter.enableLog());
        disable_log.setOnClickListener(view -> presenter.disableLog());
        share.setOnClickListener(view -> presenter.share());
        delete_log.setOnClickListener(view -> presenter.deleteLog());


        map_view.onCreate(savedInstanceState);
        map_view.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            map.setStyle(Style.LIGHT);
            map.getUiSettings().setTiltGesturesEnabled(false);
        });

        show_gps.setOnCheckedChangeListener((compoundButton, b) -> updateRoute());
        show_kalman.setOnCheckedChangeListener((compoundButton, b) -> updateRoute());
        show_geohash.setOnCheckedChangeListener((compoundButton, b) -> updateRoute());

        presenter = new TestPresenter(this, this);
    }

    @Override
    public void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_CODE);
    }

    @Override
    public void showCurrentPosition(LatLng position) {
        if (map != null) {
            runOnUiThread(() -> map_view.post(() -> {
                map.animateCamera(CameraUpdateFactory.newLatLng(position));
            }));
        }
    }

    @Override
    public void updateRoute() {//todo fix map update
        if (map != null) {
            runOnUiThread(() -> map_view.post(() -> {
                if (rawLocationsPolyline != null) {
                    rawLocationsPolyline.remove();
                }
                if (kalmanFilteredLocationsPolyline != null) {
                    kalmanFilteredLocationsPolyline.remove();
                }
                if (geoHashFilteredLocationsPolyline != null) {
                    geoHashFilteredLocationsPolyline.remove();
                }

                if (show_gps.isChecked()) {
                    rawLocationsPolyline = map.addPolyline(
                            new PolylineOptions()
                                    .addAll(presenter.getRawLocations())
                                    .alpha(0.3f)
                                    .color(ContextCompat.getColor(this, R.color.green))
                    );
                }
                if (show_kalman.isChecked()) {
                    kalmanFilteredLocationsPolyline = map.addPolyline(
                            new PolylineOptions()
                                    .addAll(presenter.getKalmanFilteredLocations())
                                    .alpha(0.3f)
                                    .color(ContextCompat.getColor(this, R.color.orange))
                    );
                }
                if (show_geohash.isChecked()) {
                    geoHashFilteredLocationsPolyline = map.addPolyline(
                            new PolylineOptions()
                                    .addAll(presenter.getGeoHashFilteredLocations())
                                    .alpha(0.3f)
                                    .color(ContextCompat.getColor(this, R.color.red))
                    );
                }
            }));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            presenter.startService();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        map_view.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map_view.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map_view.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        map_view.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map_view.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map_view.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map_view.onDestroy();
        presenter.onDestroy();
    }
}
