package com.example.lezh1k.sensordatacollector.Services;

import android.app.Service;
import android.location.Location;

import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceInterface;
import com.example.lezh1k.sensordatacollector.Interfaces.LocationServiceStatusInterface;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lezh1k on 1/15/18.
 */

public abstract class LocationService extends Service {
    protected String TAG = "LocationService";
    protected List<LocationServiceInterface> m_locationServiceInterfaces;
    protected List<LocationServiceStatusInterface> m_locationServiceStatusInterfaces;

    protected Location m_lastLocation;
    protected List<Location> m_track;

    public List<Location> getTrack() {
        return m_track;
    }
    public void clearTrack() {m_track.clear();}

    public LocationService() {
        m_locationServiceInterfaces = new ArrayList<>();
        m_locationServiceStatusInterfaces = new ArrayList<>();
    }

    public void addInterface(LocationServiceInterface locationServiceInterface) {
        if (m_locationServiceInterfaces.add(locationServiceInterface) && m_lastLocation != null) {
            locationServiceInterface.locationChanged(m_lastLocation);
        }
    }

    public void addInterfaces(List<LocationServiceInterface> locationServiceInterfaces) {
        if (m_locationServiceInterfaces.addAll(locationServiceInterfaces) && m_lastLocation != null) {
            for (LocationServiceInterface locationServiceInterface : locationServiceInterfaces) {
                locationServiceInterface.locationChanged(m_lastLocation);
            }
        }
    }

    public void removeInterface(LocationServiceInterface locationServiceInterface) {
        m_locationServiceInterfaces.remove(locationServiceInterface);
    }

    public void removeStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        m_locationServiceStatusInterfaces.remove(locationServiceStatusInterface);
    }

    public abstract void start();
    public abstract void stop();
    public abstract void reset();

    public void addStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        if (m_locationServiceStatusInterfaces.add(locationServiceStatusInterface)) {
//            locationServiceStatusInterface.serviceStatusChanged(m_serviceStatus);
//            locationServiceStatusInterface.GPSStatusChanged(m_activeSatellites);
//            locationServiceStatusInterface.GPSEnabledChanged(m_gpsEnabled);
//            locationServiceStatusInterface.lastLocationAccuracyChanged(m_lastLocationAccuracy);
        }
    }

    public void addStatusInterfaces(List<LocationServiceStatusInterface> locationServiceStatusInterfaces) {
        if (m_locationServiceStatusInterfaces.addAll(locationServiceStatusInterfaces)) {
            for (LocationServiceStatusInterface locationServiceStatusInterface : locationServiceStatusInterfaces) {
//                locationServiceStatusInterface.serviceStatusChanged(m_serviceStatus);
//                locationServiceStatusInterface.GPSStatusChanged(m_activeSatellites);
//                locationServiceStatusInterface.GPSEnabledChanged(m_gpsEnabled);
//                locationServiceStatusInterface.lastLocationAccuracyChanged(m_lastLocationAccuracy);
            }
        }
    }
}
