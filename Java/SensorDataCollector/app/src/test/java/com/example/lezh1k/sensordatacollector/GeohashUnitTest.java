package com.example.lezh1k.sensordatacollector;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

/**
 * Created by lezh1k on 12/6/17.
 */

public class GeohashUnitTest {

    class EncodeTestItem {
        double lat;
        double lon;
        int precision;
        String expected;

        EncodeTestItem(double lat, double lon, int precision, String expected) {
            this.lat = lat;
            this.lon = lon;
            this.precision = precision;
            this.expected = expected;
        }
    }

    @Test
    public void encodeTest() throws Exception {
        EncodeTestItem posTests[] = {
                new EncodeTestItem(44.87533558, -64.3057251, 8, "dxfr29mc"),
                new EncodeTestItem(46.76244305, -60.6362915, 8, "f8kfh0y4"),
                new EncodeTestItem(50.79204706, 61.47949219, 8, "v358zn2j"),
                new EncodeTestItem(-82.214234, 114.257834, 9, "n93k21252"),
                new EncodeTestItem(-21.45306863, 137.02148438, 9, "rh1myn84b"),
                new EncodeTestItem(44.87533558, -64.3057251, 7, "dxfr29m"),
                new EncodeTestItem(46.76244305, -60.6362915, 7, "f8kfh0y"),
                new EncodeTestItem(50.79204706, 61.47949219, 7, "v358zn2"),
                new EncodeTestItem(-82.214234, 114.257834, 8, "n93k2125"),
                new EncodeTestItem(-21.45306863, 137.02148438, 8, "rh1myn84"),
        };

        //random changes in right strings
        EncodeTestItem negTests[] = {
                new EncodeTestItem(44.87533558, -64.3057251, 8, "dxer29mc"),
                new EncodeTestItem(46.76244305, -60.6362915, 8, "f8kgh0y4"),
                new EncodeTestItem(50.79204706, 61.47949219, 8, "v338zn2j"),
                new EncodeTestItem(-82.214234, 114.257834, 9, "n93kgg1252"),
                new EncodeTestItem(-21.45306863, 137.02148438, 9, "rh12myn84b"),
                new EncodeTestItem(44.87533558, -64.3057251, 7, "dxfr2gm"),
                new EncodeTestItem(46.76244305, -60.6362915, 7, "f84fh0y"),
                new EncodeTestItem(50.79204706, 61.47949219, 7, "v318zn2"),
                new EncodeTestItem(-82.214234, 114.257834, 8, "143k2125"),
                new EncodeTestItem(-21.45306863, 137.02148438, 8, "43fmyn84"),
        };

        for (EncodeTestItem ti : posTests) {
            assertEquals(ti.expected, Geohash.Encode(ti.lat, ti.lon, ti.precision));
        }

        for (EncodeTestItem ti : negTests) {
            assertNotSame(ti.expected, Geohash.Encode(ti.lat, ti.lon, ti.precision));
        }
    }
}
