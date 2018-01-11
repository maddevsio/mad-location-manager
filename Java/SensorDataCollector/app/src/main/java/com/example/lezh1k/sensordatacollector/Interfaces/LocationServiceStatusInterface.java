package com.example.lezh1k.sensordatacollector.Interfaces;

/**
 * Created by lezh1k on 1/11/18.
 */

public interface LocationServiceStatusInterface {
    void serviceStatusChanged(int status);
    void GPSStatusChanged(int activeSatellites);
    void GPSEnabledChanged(boolean enabled);
    void lastLocationAccuracyChanged(float accuracy);
}
