package mad.location.manager.lib.location;

import android.location.Location;

public interface LocationCallback {
    void locationChanged(Location location);
    void GPSStatusChanged(int activeSatellites);
    void GPSEnabledChanged(boolean enabled);
}
