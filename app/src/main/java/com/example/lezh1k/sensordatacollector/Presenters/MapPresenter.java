package com.example.lezh1k.sensordatacollector.Presenters;

import android.content.Context;
import android.location.Location;

import com.example.gpsacckalmanfusion.Lib.Commons.Utils;
import com.example.gpsacckalmanfusion.Lib.Loggers.GeohashRTFilter;
import com.example.gpsacckalmanfusion.Lib.Services.KalmanLocationService;
import com.example.gpsacckalmanfusion.Lib.Services.ServicesHelper;
import com.example.lezh1k.sensordatacollector.Interfaces.MapInterface;
import com.example.lezh1k.sensordatacollector.MainActivity;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lezh1k on 1/30/18.
 */

public class MapPresenter {
    private MapInterface mapInterface;
    private Context context;
    private GeohashRTFilter m_geoHashRTFilter;

    public MapPresenter(Context context, MapInterface mapInterface, GeohashRTFilter geoHashRTFilter) {
        this.mapInterface = mapInterface;
        this.context = context;
        m_geoHashRTFilter = geoHashRTFilter;
    }

    public void onLocationChanged(Location location, CameraPosition currentCameraPosition) {
        CameraPosition.Builder position =
                new CameraPosition.Builder(currentCameraPosition).target(new LatLng(location));
        mapInterface.moveCamera(position.build());
        getRoute();
        m_geoHashRTFilter.filter(location);
    }

    public void getRoute() {
        ServicesHelper.getLocationService(context, value -> {
            KalmanLocationService kls = (KalmanLocationService) value;

            List<LatLng> routGpsAsIs = new ArrayList<>(kls.getGpsTrack().size());
            List<LatLng> routeFilteredKalman = new ArrayList<>(value.getKalmanOnlyFilteredTrack().size());
            List<LatLng> routeFilteredWithGeoHash =
                    new ArrayList<>(m_geoHashRTFilter.getGeoFilteredTrack().size());

            for (Location location : new ArrayList<>(value.getKalmanOnlyFilteredTrack())) {
                routeFilteredKalman.add(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            for (Location location : new ArrayList<>(m_geoHashRTFilter.getGeoFilteredTrack())) {
                routeFilteredWithGeoHash.add(new LatLng(location.getLatitude(),
                        location.getLongitude()));
            }

            for (Location location : new ArrayList<>(kls.getGpsTrack())) {
                routGpsAsIs.add(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            mapInterface.showRoute(routeFilteredKalman, MainActivity.FILTER_KALMAN_ONLY);
            mapInterface.showRoute(routeFilteredWithGeoHash, MainActivity.FILTER_KALMAN_WITH_GEO);
            mapInterface.showRoute(routGpsAsIs, MainActivity.GPS_ONLY);
        });
    }
}
