package com.example.lezh1k.sensordatacollector.Interfaces;

import android.content.Intent;

import com.mapbox.mapboxsdk.geometry.LatLng;

public interface TestInterface {
    void requestPermissions();
    void showCurrentPosition(LatLng position);
    void updateRoute();
    void startActivity(Intent intent);
}
