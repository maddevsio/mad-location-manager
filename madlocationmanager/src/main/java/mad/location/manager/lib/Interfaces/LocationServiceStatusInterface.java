package mad.location.manager.lib.Interfaces;

/**
 * Created by lezh1k on 2/13/18.
 */

public interface LocationServiceStatusInterface {
    void serviceStatusChanged(int status);
    void GPSStatusChanged(int activeSatellites);
    void GPSEnabledChanged(boolean enabled);
    void lastLocationAccuracyChanged(float accuracy);
}
