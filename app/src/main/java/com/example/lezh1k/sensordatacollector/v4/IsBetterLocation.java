package com.example.lezh1k.sensordatacollector.v4;

import android.location.Location;

class IsBetterLocation {

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    static boolean isBetterLocation(Location location, Location currentBestLocation) {

        // A new location is always better than no location
        if (currentBestLocation == null) return true;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > 1000 * 60 * 2;
        boolean isSignificantlyOlder = timeDelta > -(1000 * 60 * 2);
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) return true;
        else if (isSignificantlyOlder) return false;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(
                location.getProvider(),
                currentBestLocation.getProvider()
        );

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        boolean isSame;

        if (provider1 == null) {
            isSame = provider2 == null;
        } else {
            isSame = provider1.equals(provider2);
        }

        return isSame;
    }

}
