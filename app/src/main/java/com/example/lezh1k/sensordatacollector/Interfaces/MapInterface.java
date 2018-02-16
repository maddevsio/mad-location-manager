package com.example.lezh1k.sensordatacollector.Interfaces;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.List;

/**
 * Created by lezh1k on 1/30/18.
 */

public interface MapInterface {
    void showRoute(List<LatLng> route, int interestedRoute); //second arg is HACK! :)
    void moveCamera(CameraPosition position);
    void setAllGesturesEnabled(boolean enabled);
}
