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

/*class SensorDataEventLoopTask extends AsyncTask {

        boolean needTerminate = false;
        long deltaTMs;
        double llat, llon;
        ArrayList<GeoPoint> track = new ArrayList<>();
        ArrayList<GeoPoint> tmp = new ArrayList<>();

        SensorDataEventLoopTask(long deltaTMs) {
            this.deltaTMs = deltaTMs;
        }

        private void calculateDistanceStep() {
            GeoPoint pp = Coordinates.metersToGeoPoint(
                    m_kalmanFilter.getCurrentX(),
                    m_kalmanFilter.getCurrentY());
            String geo0, geo1;
            final int precision = 7;
            final int minPoints = 3;

            geo0 = GeoHash.encode(llat, llon, precision);
            geo1 = GeoHash.encode(pp.Latitude, pp.Longitude, precision);

            track.add(pp);

            if (geo0.equals(geo1))
                return;

            tmp = Coordinates.filterByGeohash(track, precision, minPoints);
            double dd = Coordinates.calculateDistance(tmp);
            m_distance += dd;
            llat = pp.Latitude;
            llon = pp.Longitude;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            while (!needTerminate) {
                try {
                    Thread.sleep(deltaTMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue; //bad
                }

                SensorGpsDataItem sdi;
                while ((sdi = m_sensorDataQueue.poll()) != null) {
                    //warning!!!
                    if (sdi.getGpsLat() == SensorGpsDataItem.NOT_INITIALIZED) {
                        m_kalmanFilter.predict(sdi.getTimestamp(), sdi.getAbsEastAcc(), sdi.getAbsNorthAcc());
                    } else {
                        double xVel = sdi.getSpeed() * Math.cos(sdi.getCourse());
                        double yVel = sdi.getSpeed() * Math.sin(sdi.getCourse());
                        m_kalmanFilter.update(
                                sdi.getTimestamp(),
                                Coordinates.longitudeToMeters(sdi.getGpsLon()),
                                Coordinates.latitudeToMeters(sdi.getGpsLat()),
                                xVel,
                                yVel,
                                sdi.getPosErr(),
                                sdi.getVelErr());
                        calculateDistanceStep();
                    }
                }
            }
            return null;
        }
    }*/

public class KalmanDistanceLogger implements LocationServiceInterface {

    private boolean firstCoordinateReceived = true;
    private double llat, llon;

    public KalmanDistanceLogger() {
        ServicesHelper.addLocationServiceInterface(this);
    }

    @Override
    public void locationChanged(Location location) {
        firstCoordinateReceived = false;
    }
}
