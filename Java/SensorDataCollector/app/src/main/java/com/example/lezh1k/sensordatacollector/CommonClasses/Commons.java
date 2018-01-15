package com.example.lezh1k.sensordatacollector.CommonClasses;

/**
 * Created by lezh1k on 12/12/17.
 */

public class Commons {
    public static void lowPassFilterArr(float alpha, float[] prev, float[] measured) {
        for (int i = 0; i < measured.length; ++i) {
            prev[i] = lowPassFilter(alpha, prev[i], measured[i]);
        }
    }

    public static float lowPassFilter(float alpha, float prev, float measured) {
        return prev + alpha * (measured - prev);
    }

    public static float highPassFilter(float alpha, float prev, float prevMeasured, float measured) {
        return alpha*prev + alpha*(measured - prevMeasured);
    }


    public static double milesPerHour2MeterPerSecond(double mph) {
        return 2.23694 * mph;
    }
    public static double knotsPerHour2MeterPerSecond(double kph) {
        return 0.5144444 * kph;
    }

    public static int hertz2periodUs(double hz) { return (int) (1000000 / (1.0 / hz));}
    public static int hertz2periodMs(double hz) { return hertz2periodUs(hz) / 1000;}

    public static final String AppName = "SensorDataCollector";

}

