package mad.location.manager.lib.Interfaces;

import static mad.location.manager.lib.Services.KalmanLocationService.ServiceStatus;

/**
 * Created by lezh1k on 2/13/18.
 */

public interface LocationServiceStatusInterface {
    void serviceStatusChanged(ServiceStatus status);
    void GPSStatusChanged(int activeSatellites);
    void GPSEnabledChanged(boolean enabled);
    void lastLocationAccuracyChanged(float accuracy);
}
