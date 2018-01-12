package com.example.lezh1k.sensordatacollector.CommonClasses;

import com.example.lezh1k.sensordatacollector.CommonClasses.GeoPoint;
import com.example.lezh1k.sensordatacollector.Filters.GeoHash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lezh1k on 12/12/17.
 */

public class Coordinates {

    private static final double EARTH_RADIUS = 6371.0 * 1000.0; // meters

    public static double geoDistanceMeters(double lon1, double lat1, double lon2, double lat2) {
        double deltaLon = Math.toRadians(lon2 - lon1);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double a =
                Math.pow(Math.sin(deltaLat / 2.0), 2.0) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.pow(Math.sin(deltaLon/2.0), 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0-a));
        return EARTH_RADIUS * c;
    }

    public static double longitudeToMeters(double lon) {
        double distance = geoDistanceMeters(lon, 0.0, 0.0, 0.0);
        return distance * (lon < 0.0 ? -1.0 : 1.0);
    }

    public static GeoPoint metersToGeoPoint(double lonMeters,
                                            double latMeters) {
        GeoPoint point = new GeoPoint(0.0, 0.0);
        GeoPoint pointEast = pointPlusDistanceEast(point, lonMeters);
        GeoPoint pointNorthEast = pointPlusDistanceNorth(pointEast, latMeters);
        return pointNorthEast;
    }

    public static double latitudeToMeters(double lat) {
        double distance = geoDistanceMeters(0.0, lat, 0.0, 0.0);
        return distance * (lat < 0.0 ? -1.0 : 1.0);
    }

    private static GeoPoint getPointAhead(GeoPoint point,
                                          double distance,
                                          double azimuthDegrees) {

        double radiusFraction = distance / EARTH_RADIUS;
        double bearing = Math.toRadians(azimuthDegrees);
        double lat1 = Math.toRadians(point.Latitude);
        double lng1 = Math.toRadians(point.Longitude);

        double lat2_part1 = Math.sin(lat1) * Math.cos(radiusFraction);
        double lat2_part2 = Math.cos(lat1) * Math.sin(radiusFraction) * Math.cos(bearing);
        double lat2 = Math.asin(lat2_part1 + lat2_part2);

        double lng2_part1 = Math.sin(bearing) * Math.sin(radiusFraction) * Math.cos(lat1);
        double lng2_part2 = Math.cos(radiusFraction) - Math.sin(lat1) * Math.sin(lat2);
        double lng2 = lng1 + Math.atan2(lng2_part1, lng2_part2);

        lng2 = (lng2 + 3.0*Math.PI) % (2.0*Math.PI) - Math.PI;

        GeoPoint res = new GeoPoint(Math.toDegrees(lat2), Math.toDegrees(lng2));
        return res;
    }

    private static GeoPoint pointPlusDistanceEast(GeoPoint point, double distance) {
        return getPointAhead(point, distance, 90.0);
    }

    private static GeoPoint pointPlusDistanceNorth(GeoPoint point, double distance) {
        return getPointAhead(point, distance, 0.0);
    }

    public static ArrayList<GeoPoint> filterByGeohash(List<GeoPoint> lstSrc,
                                                      int precision,
                                                      int minPointCount) {
        class cindex {
            int index;
            int count;
            double lon, lat;
        }

        HashMap<String, cindex> dctHashCount = new HashMap<>();

        int idx = 0;
        for (GeoPoint ci : lstSrc) {
            String geohash = GeoHash.encode(ci.Latitude, ci.Longitude, precision);
            if (!dctHashCount.containsKey(geohash)) {
                cindex ni = new cindex();
                ni.count = 0;
                ni.lat = 0.0;
                ni.lon = 0.0;
                ni.index = -1;
                dctHashCount.put(geohash, ni);
            }
            cindex it = dctHashCount.get(geohash);
            if (++it.count == minPointCount)
                it.index = idx++;
            it.lat += ci.Latitude;
            it.lon += ci.Longitude;
        }

        GeoPoint resArr[] = new GeoPoint[idx];
        for (Map.Entry<String, cindex> it : dctHashCount.entrySet()) {
            cindex val = it.getValue();
            if (val.index == -1)
                continue;

            GeoPoint np = new GeoPoint(val.lat / val.count,
                    val.lon / val.count);
            resArr[val.index] = np;
        }
        ArrayList<GeoPoint> lstRes = new ArrayList<>(Arrays.asList(resArr));
        return lstRes;
    }

    public static double calculateDistance(ArrayList<GeoPoint> lstTrack) {
        double distance = 0.0;
        double llon, llat;

        if (lstTrack.isEmpty() || lstTrack.size() == 1)
            return 0.0;

        llon = lstTrack.get(0).Longitude;
        llat = lstTrack.get(0).Latitude;

        for (int i = 1; i < lstTrack.size(); ++i) {
            GeoPoint pp = lstTrack.get(i); //todo use iterator;
            distance += Coordinates.geoDistanceMeters(llat, llon, pp.Latitude, pp.Longitude);
            llat = pp.Latitude;
            llon = pp.Longitude;
        }
        return distance;
    }
}
