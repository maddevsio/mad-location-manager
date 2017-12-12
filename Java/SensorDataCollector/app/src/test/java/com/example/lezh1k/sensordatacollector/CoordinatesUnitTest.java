package com.example.lezh1k.sensordatacollector;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Created by lezh1k on 12/12/17.
 */

public class CoordinatesUnitTest {

    //http://www.onlineconversion.com/map_greatcircle_distance.htm
    @Test
    public void LongitudeToMetersTest() throws Exception {
        final double eps = 1e-08;
        assertTrue(Math.abs(1373039.2908091505 - Math.abs(Coordinates.LongitudeToMeters(12.348039))) < eps);
        assertTrue(Math.abs(13703046.250524132 - Math.abs(Coordinates.LongitudeToMeters(123.2344556))) < eps);
        assertTrue(Math.abs(13602958.57731845 - Math.abs(Coordinates.LongitudeToMeters(-122.33434553))) < eps);
    }

    @Test
    public void LatitudeToMetersTest() throws Exception {
        final double eps = 1e-08;
        assertTrue(Math.abs(4038993.6993554747 - Math.abs(Coordinates.LatitudeToMeters(36.323543))) < eps);
        assertTrue(Math.abs(13974509.760789291 - Math.abs(Coordinates.LatitudeToMeters(234.3242144))) < eps);
        assertTrue(Math.abs(14159832.607369563 - Math.abs(Coordinates.LatitudeToMeters(-127.342434))) < eps);
    }

    @Test
    public void MetersToGeoPointTest() throws Exception {
        final double eps = 1e-06;
        GeoPoint t = Coordinates.MetersToGeoPoint(1373039.2908091505, 4038993.6993554747);
        assertTrue(Math.abs(t.Latitude - 36.323543) < eps);
        assertTrue(Math.abs(t.Longitude - 12.348039) < eps);
    }
}
