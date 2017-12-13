package com.example.lezh1k.sensordatacollector;

/**
 * Created by lezh1k on 12/12/17.
 */

public class Commons {
    public static void LowPassFilterArr(float alpha, float[] prev, float[] measured) {
        for (int i = 0; i < prev.length; ++i) {
            prev[i] = LowPassFilter(alpha, prev[i], measured[i]);
        }
    }

    public static float LowPassFilter(float alpha, float prev, float measured) {
        return prev + alpha * (measured - prev);
    }

    public static double MilesPerHour2MeterPerSecond(double mph) {
        return 2.23694 * mph;
    }

    public static double KnotsPerHour2MeterPerSecond(double kph) {
        return 0.5144444 * kph;
    }

    public static final String AppName = "SensorDataCollector";
}
