package mad.location.manager.lib.locationProviders;

import android.location.Location;

public interface LocationProviderCallback {
    void locationAvailabilityChanged(boolean isLocationAvailable);

    void onLocationAvailable(Location location);
}

