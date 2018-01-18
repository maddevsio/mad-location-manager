package com.example.lezh1k.sensordatacollector.Loggers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.example.lezh1k.sensordatacollector.CommonClasses.Commons;
import com.example.lezh1k.sensordatacollector.CommonClasses.Coordinates;
import com.example.lezh1k.sensordatacollector.CommonClasses.GeoPoint;
import com.example.lezh1k.sensordatacollector.CommonClasses.SensorGpsDataItem;
import com.example.lezh1k.sensordatacollector.Filters.GPSAccKalmanFilter;
import com.example.lezh1k.sensordatacollector.Filters.GeoHash;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceInterface;
import com.example.lezh1k.sensordatacollector.Services.ServicesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by lezh1k on 1/8/18.
 */

public class KalmanDistanceLogger implements LocationServiceInterface {

    private boolean firstCoordinateReceived = false;
    private double llat, llon;
    private ArrayList<GeoPoint> track = new ArrayList<>();
    private ArrayList<GeoPoint> tmp = new ArrayList<>();
    private double m_distance;

    public KalmanDistanceLogger() {
        ServicesHelper.addLocationServiceInterface(this);
    }

    public double getDistance() {
        return m_distance;
    }

    public void reset() {
        track.clear();
        tmp.clear();
        m_distance = 0.0;
        firstCoordinateReceived = false;
    }

    @Override
    public void locationChanged(Location loc) {
        XLog.i("%d%d FKS : lat=%f, lon=%f, alt=%f",
                Commons.LogMessageType.FILTERED_GPS_DATA.ordinal(),
                loc.getTime(), loc.getLatitude(),
                loc.getLongitude(), loc.getAltitude());

        GeoPoint pp = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        if (!firstCoordinateReceived) {
            llat = loc.getLatitude();
            llon = loc.getLongitude();
            firstCoordinateReceived = true;
            track.add(pp);
            return;
        }

        String geo0, geo1;
        final int precision = 8;
        final int minPoints = 2;

        geo0 = GeoHash.encode(llat, llon, precision);
        geo1 = GeoHash.encode(pp.Latitude, pp.Longitude, precision);

        track.add(pp);

        if (geo0.equals(geo1))
            return;

        tmp = Coordinates.filterByGeohash(track, precision, minPoints);
        double dd = Coordinates.calculateDistance(tmp);
        m_distance = dd;
        llat = pp.Latitude;
        llon = pp.Longitude;
    }
}
