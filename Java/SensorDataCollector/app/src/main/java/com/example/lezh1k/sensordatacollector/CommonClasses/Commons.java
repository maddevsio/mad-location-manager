package com.example.lezh1k.sensordatacollector.CommonClasses;

/**
 * Created by lezh1k on 12/12/17.
 */

public class Commons {
    public static void LowPassFilterArr(float alpha, float[] prev, float[] measured) {
        for (int i = 0; i < measured.length; ++i) {
            prev[i] = LowPassFilter(alpha, prev[i], measured[i]);
        }
    }

    public static float LowPassFilter(float alpha, float prev, float measured) {
        return prev + alpha * (measured - prev);
    }

    public static float HighPassFilter(float alpha, float prev, float prevMeasured, float measured) {
        return alpha*prev + alpha*(measured - prevMeasured);
    }


    public static double MilesPerHour2MeterPerSecond(double mph) {
        return 2.23694 * mph;
    }

    public static double KnotsPerHour2MeterPerSecond(double kph) {
        return 0.5144444 * kph;
    }

    public static final String AppName = "SensorDataCollector";

}

