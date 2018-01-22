package com.example.lezh1k.sensordatacollector.Loggers;

import android.location.Location;

import com.elvishew.xlog.XLog;
import com.example.lezh1k.sensordatacollector.CommonClasses.Commons;
import com.example.lezh1k.sensordatacollector.CommonClasses.Coordinates;
import com.example.lezh1k.sensordatacollector.CommonClasses.GeoPoint;
import com.example.lezh1k.sensordatacollector.Filters.GeoHash;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceInterface;
import com.example.lezh1k.sensordatacollector.Services.ServicesHelper;

import java.util.Arrays;

/**
 * Created by lezh1k on 1/8/18.
 */

public class KalmanDistanceLogger implements LocationServiceInterface {

    private double m_distanceGeoFiltered = 0.0;
    private double m_distanceAsIs = 0.0;

    private static final double COORD_NOT_INITIALIZED = 361.0;
    private static final int ppCompGeoHash = 0;
    private static final int ppReadGeoHash = 1;

    private char geoHashBuffers[][];
    private int pointsInCurrentGeohashCount;

    private GeoPoint currentGeoPoint;
    private GeoPoint lastApprovedGeoPoint;
    private GeoPoint lastGeoPointAsIs;

    private boolean isFirstCoordinate = true;

    private String lastFilteredLocationString;

    public String getLastFilteredLocationString() {
        return lastFilteredLocationString;
    }

    public KalmanDistanceLogger() {
        reset();
        ServicesHelper.addLocationServiceInterface(this);
    }

    public double getDistanceGeoFiltered() {
        return m_distanceGeoFiltered;
    }
    public double getDistanceAsIs() { return m_distanceAsIs; }

    public void reset() {
        char[] buff1 = new char[GeoHash.GEOHASH_MAX_PRECISION];
        char[] buff2 = new char[GeoHash.GEOHASH_MAX_PRECISION];
        geoHashBuffers = new char[][]{buff1, buff2};
        pointsInCurrentGeohashCount = 0;
        lastApprovedGeoPoint = new GeoPoint(COORD_NOT_INITIALIZED, COORD_NOT_INITIALIZED);
        currentGeoPoint = new GeoPoint(COORD_NOT_INITIALIZED, COORD_NOT_INITIALIZED);

        lastGeoPointAsIs = new GeoPoint(COORD_NOT_INITIALIZED, COORD_NOT_INITIALIZED);
        m_distanceGeoFiltered = 0.0;
        m_distanceAsIs = 0.0;
        isFirstCoordinate = true;
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

        lastFilteredLocationString = String.format("Lat: %f, Lon : %f, speed : %f",
                loc.getLatitude(),
                loc.getLongitude(),
                loc.getSpeed());
        if (isFirstCoordinate) {
            GeoHash.encode(pi.Latitude, pi.Longitude, geoHashBuffers[ppCompGeoHash], prec);
            currentGeoPoint.Latitude = pi.Latitude;
            currentGeoPoint.Longitude = pi.Longitude;
            lastGeoPointAsIs.Latitude = pi.Latitude;
            lastGeoPointAsIs.Longitude = pi.Longitude;
            pointsInCurrentGeohashCount = 1;
            isFirstCoordinate = false;
            return;
        }

        m_distanceAsIs += Coordinates.geoDistanceMeters(lastGeoPointAsIs.Longitude,
                lastGeoPointAsIs.Latitude,
                pi.Longitude,
                pi.Latitude);

        lastGeoPointAsIs.Longitude = loc.getLongitude();
        lastGeoPointAsIs.Latitude = loc.getLatitude();

        GeoHash.encode(pi.Latitude, pi.Longitude, geoHashBuffers[ppReadGeoHash], prec);
        if (!Arrays.equals(geoHashBuffers[ppCompGeoHash], geoHashBuffers[ppReadGeoHash])) {
            if (pointsInCurrentGeohashCount >= minPointCount) {
                currentGeoPoint.Latitude /= pointsInCurrentGeohashCount;
                currentGeoPoint.Longitude /= pointsInCurrentGeohashCount;

                if (lastApprovedGeoPoint.Latitude != COORD_NOT_INITIALIZED) {
                    m_distanceGeoFiltered += Coordinates.geoDistanceMeters(lastApprovedGeoPoint.Longitude, lastApprovedGeoPoint.Latitude,
                            currentGeoPoint.Longitude, currentGeoPoint.Latitude);
                }
                lastApprovedGeoPoint = currentGeoPoint;
                currentGeoPoint.Latitude = currentGeoPoint.Longitude = 0.0;
            }
            pointsInCurrentGeohashCount = 1;
            currentGeoPoint.Latitude = pi.Latitude;
            currentGeoPoint.Longitude = pi.Longitude;
            //swap buffers
            char[] swp = geoHashBuffers[ppCompGeoHash];
            geoHashBuffers[ppCompGeoHash] = geoHashBuffers[ppReadGeoHash];
            geoHashBuffers[ppReadGeoHash] = swp;
            return;
        }

        currentGeoPoint.Latitude += pi.Latitude;
        currentGeoPoint.Longitude += pi.Longitude;
        ++pointsInCurrentGeohashCount;
    }
}
