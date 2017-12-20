package com.example.lezh1k.sensordatacollector;

/**
 * Created by lezh1k on 12/6/17.
 */

public class GeoHash {

    static final char base32Table[] = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public static String Encode(double srcLat, double srcLon, int precision) {
        class Interval {
            private double min, max;

            private Interval(double min, double max) {
                this.min = min;
                this.max = max;
            }
        }

        String geohash = "";
        Interval lat = new Interval(-90.0, 90.0);
        Interval lon = new Interval(-180.0, 180.0);
        Interval ci;
        boolean isEven = true;
        double mid, cd;
        int idx = 0; // index into base32 map
        int bit = 0; // each char holds 5 bits

        while (precision > 0) {
            if (isEven) {
                ci = lon;
                cd = srcLon;
            } else {
                ci = lat;
                cd = srcLat;
            }

            mid = (ci.min + ci.max) / 2.0;
            idx <<= 1; //idx *= 2
            if (cd >= mid) {
                ci.min = mid;
                idx |= 1; //idx += 1
            } else {
                ci.max = mid;
            }

            isEven = !isEven;

            if (++bit == 5) {
                geohash += base32Table[idx];
                idx = bit = 0;
                --precision;
            }
        }
        return geohash;
    }
}