package com.example.lezh1k.sensordatacollector.Loggers;

import android.location.Location;

import com.elvishew.xlog.XLog;
import com.example.lezh1k.sensordatacollector.CommonClasses.Commons;
import com.example.lezh1k.sensordatacollector.CommonClasses.Coordinates;
import com.example.lezh1k.sensordatacollector.CommonClasses.GeoPoint;
import com.example.lezh1k.sensordatacollector.Filters.GeoHash;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceInterface;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceStatusInterface;
import com.example.lezh1k.sensordatacollector.Services.ServicesHelper;

import java.util.Arrays;

/**
 * Created by lezh1k on 1/8/18.
 */

public class KalmanDistanceLogger implements LocationServiceInterface, LocationServiceStatusInterface {

    private double m_distanceGeoFiltered = 0.0;
    private double m_distanceGeoFilteredHP = 0.0;
    private double m_distanceAsIs = 0.0;
    private double m_distanceAsIsHP = 0.0;

    private static final double COORD_NOT_INITIALIZED = 361.0;
    private int ppCompGeoHash = 0;
    private int ppReadGeoHash = 1;

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
        ServicesHelper.addLocationServiceStatusInterface(this);
    }

    public double getDistanceGeoFiltered() {
        return m_distanceGeoFiltered;
    }

    public double getDistanceGeoFilteredHP() {
        return m_distanceGeoFilteredHP;
    }

    public double getDistanceAsIs() {
        return m_distanceAsIs;
    }

    public double getDistanceAsIsHP() {
        return m_distanceAsIsHP;
    }

    public void reset() {
        char[] buff1 = new char[GeoHash.GEOHASH_MAX_PRECISION];
        char[] buff2 = new char[GeoHash.GEOHASH_MAX_PRECISION];
        geoHashBuffers = new char[][]{buff1, buff2};
        pointsInCurrentGeohashCount = 0;
        lastApprovedGeoPoint = new GeoPoint(COORD_NOT_INITIALIZED, COORD_NOT_INITIALIZED);
        currentGeoPoint = new GeoPoint(COORD_NOT_INITIALIZED, COORD_NOT_INITIALIZED);

        lastGeoPointAsIs = new GeoPoint(COORD_NOT_INITIALIZED, COORD_NOT_INITIALIZED);
        m_distanceGeoFilteredHP = m_distanceGeoFiltered = 0.0;
        m_distanceAsIsHP = m_distanceAsIs = 0.0;
        isFirstCoordinate = true;
    }

    //todo move to parameters
    private final int precision = 8;
    private final int minPointCount = 2;
    private float hpResBuffAsIs[] = new float[3];
    private float hpResBuffGeo[] = new float[3];

    @Override
    public void locationChanged(Location loc) {
        XLog.i("%d%d FKS : lat=%f, lon=%f, alt=%f",
                Commons.LogMessageType.FILTERED_GPS_DATA.ordinal(),
                loc.getTime(),
                loc.getLatitude(), loc.getLongitude(), loc.getAltitude());

        GeoPoint pi = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        lastFilteredLocationString = String.format("Lat: %f, Lon : %f, speed : %f",
                loc.getLatitude(),
                loc.getLongitude(),
                loc.getSpeed());

        if (isFirstCoordinate) {
            GeoHash.encode(pi.Latitude, pi.Longitude, geoHashBuffers[ppCompGeoHash], precision);
            currentGeoPoint.Latitude = pi.Latitude;
            currentGeoPoint.Longitude = pi.Longitude;
            pointsInCurrentGeohashCount = 1;

            isFirstCoordinate = false;
            lastGeoPointAsIs.Latitude = pi.Latitude;
            lastGeoPointAsIs.Longitude = pi.Longitude;
            return;
        }

        m_distanceAsIs += Coordinates.geoDistanceMeters(
                lastGeoPointAsIs.Longitude,
                lastGeoPointAsIs.Latitude,
                pi.Longitude,
                pi.Latitude);

        Location.distanceBetween(
                lastGeoPointAsIs.Latitude,
                lastGeoPointAsIs.Longitude,
                pi.Latitude,
                pi.Longitude,
                hpResBuffAsIs);


        m_distanceAsIsHP += hpResBuffAsIs[0];
        lastGeoPointAsIs.Longitude = loc.getLongitude();
        lastGeoPointAsIs.Latitude = loc.getLatitude();

        GeoHash.encode(pi.Latitude, pi.Longitude, geoHashBuffers[ppReadGeoHash], precision);
        if (!Arrays.equals(geoHashBuffers[ppCompGeoHash], geoHashBuffers[ppReadGeoHash])) {
            if (pointsInCurrentGeohashCount >= minPointCount) {
                currentGeoPoint.Latitude /= pointsInCurrentGeohashCount;
                currentGeoPoint.Longitude /= pointsInCurrentGeohashCount;

                if (lastApprovedGeoPoint.Latitude != COORD_NOT_INITIALIZED) {
                    double dd1 = Coordinates.geoDistanceMeters(
                            lastApprovedGeoPoint.Longitude,
                            lastApprovedGeoPoint.Latitude,
                            currentGeoPoint.Longitude,
                            currentGeoPoint.Latitude);
                    m_distanceGeoFiltered += dd1;
                    Location.distanceBetween(
                            lastApprovedGeoPoint.Latitude,
                            lastApprovedGeoPoint.Longitude,
                            currentGeoPoint.Latitude,
                            currentGeoPoint.Longitude,
                            hpResBuffGeo);
                    double dd2 = hpResBuffGeo[0];
                    m_distanceGeoFilteredHP += dd2;
                }
                lastApprovedGeoPoint.Longitude = currentGeoPoint.Longitude;
                lastApprovedGeoPoint.Latitude = currentGeoPoint.Latitude;
                currentGeoPoint.Latitude = currentGeoPoint.Longitude = 0.0;
            }

            pointsInCurrentGeohashCount = 1;
            currentGeoPoint.Latitude = pi.Latitude;
            currentGeoPoint.Longitude = pi.Longitude;
            //swap buffers
            int swp = ppCompGeoHash;
            ppCompGeoHash = ppReadGeoHash;
            ppReadGeoHash = swp;
            return;
        }

        currentGeoPoint.Latitude += pi.Latitude;
        currentGeoPoint.Longitude += pi.Longitude;
        ++pointsInCurrentGeohashCount;
    }

    @Override
    public void serviceStatusChanged(int status) {

    }

    @Override
    public void GPSStatusChanged(int activeSatellites) {

    }

    @Override
    public void GPSEnabledChanged(boolean enabled) {

    }

    @Override
    public void lastLocationAccuracyChanged(float accuracy) {

    }
}
