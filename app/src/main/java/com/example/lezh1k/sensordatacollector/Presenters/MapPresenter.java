package com.example.lezh1k.sensordatacollector.Presenters;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.Loggers.GeohashRTFilter;

import com.example.lezh1k.sensordatacollector.Interfaces.MapInterface;
import com.example.lezh1k.sensordatacollector.MainActivity;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by lezh1k on 1/30/18.
 */

public class MapPresenter implements LocationListener {
    private MapInterface mapInterface;
    private Context context;

    private GeohashRTFilter m_geoHashRTFilter;
    private List<Location> m_lstGpsCoordinates = new ArrayList<>();
    private List<Location> m_lstKalmanFilteredCoordinates = new ArrayList<>();

    public MapPresenter(Context context, MapInterface mapInterface, GeohashRTFilter geoHashRTFilter) {
        this.mapInterface = mapInterface;
        this.context = context;
        m_geoHashRTFilter = geoHashRTFilter;
    }

    public void locationChanged(Location loc, CameraPosition currentCameraPosition) {
        CameraPosition.Builder position =
                new CameraPosition.Builder(currentCameraPosition).target(new LatLng(loc));
        mapInterface.moveCamera(position.build());
        getRoute();
        m_lstKalmanFilteredCoordinates.add(loc);
        m_geoHashRTFilter.filter(loc);
    }

    public void getRoute() {
        List<LatLng> routGpsAsIs = new ArrayList<>(m_lstGpsCoordinates.size());
        List<LatLng> routeFilteredKalman = new ArrayList<>(m_lstKalmanFilteredCoordinates.size());
        List<LatLng> routeFilteredWithGeoHash =
                new ArrayList<>(m_geoHashRTFilter.getGeoFilteredTrack().size());

        for (Location loc : new ArrayList<>(m_lstKalmanFilteredCoordinates)) {
            routeFilteredKalman.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }

        for (Location loc : new ArrayList<>(m_geoHashRTFilter.getGeoFilteredTrack())) {
            routeFilteredWithGeoHash.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }

        for (Location loc : new ArrayList<>(m_lstGpsCoordinates)) {
            routGpsAsIs.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }

        mapInterface.showRoute(routeFilteredKalman, MainActivity.FILTER_KALMAN_ONLY);
        mapInterface.showRoute(routeFilteredWithGeoHash, MainActivity.FILTER_KALMAN_WITH_GEO);
        mapInterface.showRoute(routGpsAsIs, MainActivity.GPS_ONLY);
    }
    //////////////////////////////////////////////////////////

    public void start() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //todo something
        } else {
            LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            lm.removeUpdates(this);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Utils.GPS_MIN_TIME, Utils.GPS_MIN_DISTANCE, this );
        }
    }

    public void stop() {
        LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        lm.removeUpdates(this);
        m_lstGpsCoordinates.clear();
        m_lstKalmanFilteredCoordinates.clear();
    }

    @Override
    public void onLocationChanged(Location loc) {
        if (loc == null) return;
//        if (loc.isFromMockProvider()) return;
        m_lstGpsCoordinates.add(loc);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        /*do nothing*/
    }

    @Override
    public void onProviderEnabled(String provider) {
        /*do nothing*/
    }

    @Override
    public void onProviderDisabled(String provider) {
        /*do nothing*/
    }
}
