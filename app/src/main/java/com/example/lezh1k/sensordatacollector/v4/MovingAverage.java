package com.example.lezh1k.sensordatacollector.v4;

import android.location.Location;
import android.support.annotation.NonNull;

class MovingAverage {

    private static double mOldAverageLatitude = 1.0;
    private static double mOldAverageLongitude = 1.0;
    static int currentNumOfSamples = 0;

    static Location calcMovingAverage(@NonNull Location location) {
        int windowSize = 3;

        if (currentNumOfSamples < windowSize) {
            windowSize = ++currentNumOfSamples;
        }

        double averageLatitude = (mOldAverageLatitude * (windowSize - 1) + location.getLatitude()) / windowSize;
        double averageLongitude = (mOldAverageLongitude * (windowSize - 1) + location.getLongitude()) / windowSize;

        mOldAverageLatitude = averageLatitude;
        mOldAverageLongitude = averageLongitude;

        location.setLatitude(averageLatitude);
        location.setLongitude(averageLongitude);

        return location;
    }

    void clearAverageDate() {
        mOldAverageLatitude = 1.0;
        mOldAverageLongitude = 1.0;
        currentNumOfSamples = 0;
    }
}
