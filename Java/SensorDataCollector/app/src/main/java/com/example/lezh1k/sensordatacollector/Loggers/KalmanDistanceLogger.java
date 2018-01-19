package com.example.lezh1k.sensordatacollector.Loggers;

import android.location.Location;

import com.elvishew.xlog.XLog;
import com.example.lezh1k.sensordatacollector.CommonClasses.Commons;
import com.example.lezh1k.sensordatacollector.CommonClasses.Coordinates;
import com.example.lezh1k.sensordatacollector.CommonClasses.GeoPoint;
import com.example.lezh1k.sensordatacollector.Filters.GeoHash;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceInterface;
import com.example.lezh1k.sensordatacollector.Services.ServicesHelper;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by lezh1k on 1/8/18.
 */

public class KalmanDistanceLogger implements LocationServiceInterface {

    private ArrayList<GeoPoint> track = new ArrayList<>();
    private double m_distance = 0.0;

    private char geoBuffers[][];
    private int count;
    private GeoPoint tmpGeo;
    private GeoPoint laGeo;
    private static final double COORD_NOT_INITIALIZED = 361.0;
    private static final int ppCompGeoHash = 0;
    private static final int ppReadGeoHash = 1;
    private boolean isFirstCoordinate = true;

    public KalmanDistanceLogger() {
        reset();
        ServicesHelper.addLocationServiceInterface(this);
    }

    public double getDistance() {
        return m_distance;
    }

    public void reset() {
        char[] buff1 = new char[GeoHash.GEOHASH_MAX_PRECISION];
        char[] buff2 = new char[GeoHash.GEOHASH_MAX_PRECISION];
        geoBuffers = new char[][]{buff1, buff2};
        count = 0;
        laGeo = new GeoPoint(COORD_NOT_INITIALIZED, COORD_NOT_INITIALIZED);
        tmpGeo = new GeoPoint(COORD_NOT_INITIALIZED, COORD_NOT_INITIALIZED);
        track.clear();
        m_distance = 0.0;
    }

    //todo move to parameters
    private final int prec = 8;
    private final int minPointCount = 2;

    @Override
    public void locationChanged(Location loc) {
        XLog.i("%d%d FKS : lat=%f, lon=%f, alt=%f",
                Commons.LogMessageType.FILTERED_GPS_DATA.ordinal(),
                loc.getTime(), loc.getLatitude(),
                loc.getLongitude(), loc.getAltitude());

        GeoPoint pi = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        track.add(pi);

        if (isFirstCoordinate) {
            GeoHash.encode(pi.Latitude, pi.Longitude, geoBuffers[ppCompGeoHash], prec);
            tmpGeo.Latitude = pi.Latitude;
            tmpGeo.Longitude = pi.Longitude;
            count = 1;
            isFirstCoordinate = false;
            return;
        }

        GeoHash.encode(pi.Latitude, pi.Longitude, geoBuffers[ppReadGeoHash], prec);
        if (Arrays.equals(geoBuffers[ppCompGeoHash], geoBuffers[ppReadGeoHash])) {
            if (count >= minPointCount) {
                tmpGeo.Latitude /= count;
                tmpGeo.Longitude /= count;

                if (laGeo.Latitude != COORD_NOT_INITIALIZED) {
                    m_distance += Coordinates.geoDistanceMeters(laGeo.Longitude, laGeo.Latitude,
                            tmpGeo.Longitude, tmpGeo.Latitude);
                }
                laGeo = tmpGeo;
                tmpGeo.Latitude = tmpGeo.Longitude = 0.0;
            }
            count = 1;
            tmpGeo.Latitude = pi.Latitude;
            tmpGeo.Longitude = pi.Longitude;
            //swap buffers
            char[] swp = geoBuffers[ppCompGeoHash];
            geoBuffers[ppCompGeoHash] = geoBuffers[ppReadGeoHash];
            geoBuffers[ppReadGeoHash] = swp;
            return;
        }

        tmpGeo.Latitude += pi.Latitude;
        tmpGeo.Longitude += pi.Longitude;
        ++count;
    }
}
