package com.example.gpsacckalmanfusion.Lib.Loggers;

import android.location.Location;

import com.example.gpsacckalmanfusion.Lib.Commons.Coordinates;
import com.example.gpsacckalmanfusion.Lib.Commons.GeoPoint;
import com.example.gpsacckalmanfusion.Lib.Commons.Utils;
import com.example.gpsacckalmanfusion.Lib.Filters.GeoHash;
import com.example.gpsacckalmanfusion.Lib.Interfaces.ILogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lezh1k on 2/13/18.
 */

public class GeohashRTFilter {

    public static String PROVIDER_NAME = "GeoHashFiltered";

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
    private List<Location> m_geoFilteredTrack;

    public List<Location> getGeoFilteredTrack() {
        return m_geoFilteredTrack;
    }

    private int m_geohashPrecision;
    private int m_geohashMinPointCount;

    public GeohashRTFilter(int geohashPrecision,
                           int geohashMinPointCount) {
        m_geohashPrecision = geohashPrecision;
        m_geohashMinPointCount = geohashMinPointCount;
        m_geoFilteredTrack = new ArrayList<>();
        reset(null);
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

    private ILogger m_logger;

    public void reset(ILogger logger) {
        m_logger = logger;
        m_geoFilteredTrack.clear();
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

    private float hpResBuffAsIs[] = new float[3];
    private float hpResBuffGeo[] = new float[3];

    public void filter(Location loc) {
        if (m_logger != null) {
            String toLog = String.format("%d%d FKS : lat=%f, lon=%f, alt=%f",
                    Utils.LogMessageType.FILTERED_GPS_DATA.ordinal(),
                    loc.getTime(),
                    loc.getLatitude(), loc.getLongitude(), loc.getAltitude());
            m_logger.log2file(toLog);
        }

        GeoPoint pi = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        if (isFirstCoordinate) {
            GeoHash.encode(pi.Latitude, pi.Longitude, geoHashBuffers[ppCompGeoHash], m_geohashPrecision);
            currentGeoPoint.Latitude = pi.Latitude;
            currentGeoPoint.Longitude = pi.Longitude;
            pointsInCurrentGeohashCount = 1;

            isFirstCoordinate = false;
            lastGeoPointAsIs.Latitude = pi.Latitude;
            lastGeoPointAsIs.Longitude = pi.Longitude;
            return;
        }

        m_distanceAsIs += Coordinates.distanceBetween(
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

        GeoHash.encode(pi.Latitude, pi.Longitude, geoHashBuffers[ppReadGeoHash], m_geohashPrecision);
        if (!Arrays.equals(geoHashBuffers[ppCompGeoHash], geoHashBuffers[ppReadGeoHash])) {
            if (pointsInCurrentGeohashCount >= m_geohashMinPointCount) {
                currentGeoPoint.Latitude /= pointsInCurrentGeohashCount;
                currentGeoPoint.Longitude /= pointsInCurrentGeohashCount;

                if (lastApprovedGeoPoint.Latitude != COORD_NOT_INITIALIZED) {
                    double dd1 = Coordinates.distanceBetween(
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
                Location laLoc = new Location(PROVIDER_NAME);
                laLoc.setLatitude(lastApprovedGeoPoint.Latitude);
                laLoc.setLongitude(lastApprovedGeoPoint.Longitude);
                laLoc.setAltitude(loc.getAltitude()); //hack.
                m_geoFilteredTrack.add(laLoc);
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

    public void stop() {
        if (pointsInCurrentGeohashCount >= m_geohashMinPointCount) {
            currentGeoPoint.Latitude /= pointsInCurrentGeohashCount;
            currentGeoPoint.Longitude /= pointsInCurrentGeohashCount;

            if (lastApprovedGeoPoint.Latitude != COORD_NOT_INITIALIZED) {
                double dd1 = Coordinates.distanceBetween(
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
            Location laLoc = new Location(PROVIDER_NAME);
            laLoc.setLatitude(lastApprovedGeoPoint.Latitude);
            laLoc.setLongitude(lastApprovedGeoPoint.Longitude);
            m_geoFilteredTrack.add(laLoc);
            currentGeoPoint.Latitude = currentGeoPoint.Longitude = 0.0;
        }
    }
}
