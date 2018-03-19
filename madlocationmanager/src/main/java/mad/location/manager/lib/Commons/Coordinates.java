package mad.location.manager.lib.Commons;

import mad.location.manager.lib.Filters.GeoHash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lezh1k on 2/13/18.
 */

public class Coordinates {
    private static final double EARTH_RADIUS = 6371.0 * 1000.0; // meters

    public static double distanceBetween(double lon1, double lat1, double lon2, double lat2) {
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
        double distance = distanceBetween(lon, 0.0, 0.0, 0.0);
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
        double distance = distanceBetween(0.0, lat, 0.0, 0.0);
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

    public static GeoPoint[] filterByGeoHash(List<GeoPoint> lstSrc,
                                             int precision,
                                             int minPointCount) {
        final int NOT_VALID_INDEX  = -1;
        class AuxItem {
            int index, count;
            double lon, lat;

            AuxItem() {
                index = NOT_VALID_INDEX;
                count = 0;
                lon = lat = 0.0;
            }
        }

        char buff[] = new char[precision];
        HashMap<String, AuxItem> dctHashCount = new HashMap<>();

        int idx = 0;
        for (GeoPoint ci : lstSrc) {
            GeoHash.encode(ci.Latitude, ci.Longitude, buff, precision);
            String geoHash = new String(buff);
            AuxItem it;
            if (!dctHashCount.containsKey(geoHash)) {
                it = new AuxItem();
                dctHashCount.put(geoHash, it);
            }
            it = dctHashCount.get(geoHash);
            if (++it.count == minPointCount)
                it.index = idx++;
            it.lat += ci.Latitude;
            it.lon += ci.Longitude;
        }

        GeoPoint resArr[] = new GeoPoint[idx];
        for (Map.Entry<String, AuxItem> it : dctHashCount.entrySet()) {
            AuxItem val = it.getValue();
            if (val.index == NOT_VALID_INDEX)
                continue;
            double meanLatitude = val.lat / val.count;
            double meanLongitude = val.lon / val.count;
            GeoPoint np = new GeoPoint(meanLatitude, meanLongitude);
            resArr[val.index] = np;
        }
        return resArr;
    }

    public static double calculateDistance(GeoPoint track[]) {
        double distance = 0.0;
        double lastLon, lastLat;
        //WARNING! I didn't find array.length type. Seems it's int, so we can use next comparison:
        if (track == null || track.length - 1 <= 0) //track.length == 0 || track.length == 1
            return 0.0;

        lastLon = track[0].Longitude;
        lastLat = track[0].Latitude;

        for (int i = 1; i < track.length; ++i) {
            distance += Coordinates.distanceBetween(
                    lastLat, lastLon,
                    track[i].Latitude, track[i].Longitude);
            lastLat = track[i].Latitude;
            lastLon = track[i].Longitude;
        }
        return distance;
    }
}
