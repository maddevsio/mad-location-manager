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

/**
 * Created by lezh1k on 1/8/18.
 */

public class KalmanDistanceLogger implements LocationServiceInterface {

    private boolean firstCoordinateReceived = false;
    private double lastLat, lastLon;
    private ArrayList<GeoPoint> track = new ArrayList<>();
    private double m_distance;

    public KalmanDistanceLogger() {
        ServicesHelper.addLocationServiceInterface(this);
    }

    public double getDistance() {
        return m_distance;
    }

    public void reset() {
        track.clear();
        m_distance = 0.0;
        firstCoordinateReceived = false;
    }

    @Override
    public void locationChanged(Location loc) {
        XLog.i("%d%d FKS : lat=%f, lon=%f, alt=%f",
                Commons.LogMessageType.FILTERED_GPS_DATA.ordinal(),
                loc.getTime(), loc.getLatitude(),
                loc.getLongitude(), loc.getAltitude());

        GeoPoint pp = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        if (!firstCoordinateReceived) {
            lastLat = loc.getLatitude();
            lastLon = loc.getLongitude();
            firstCoordinateReceived = true;
            track.add(pp);
            return;
        }

        String geo0, geo1;
        final int precision = 8;
        final int minPoints = 2;

        geo0 = GeoHash.encode(lastLat, lastLon, precision);
        geo1 = GeoHash.encode(pp.Latitude, pp.Longitude, precision);
        track.add(pp);

        if (geo0.equals(geo1))
            return;

        GeoPoint[] tmp = Coordinates.filterByGeohash(track, precision, minPoints);
        m_distance = Coordinates.calculateDistance(tmp);
        lastLat = pp.Latitude;
        lastLon = pp.Longitude;
    }
}
