package com.example.lezh1k.sensordatacollector.CommonClasses;

/**
 * Created by lezh1k on 12/12/17.
 */

public class Commons {
    public static final String AppName = "SensorDataCollector";

    public static int hertz2periodUs(double hz) { return (int) (1000000 / (1.0 / hz));}
    public static int hertz2periodMs(double hz) { return hertz2periodUs(hz) / 1000;}

    public static long nano2micro(long nano) {return (long) (nano / 1e3);}
    public static long nano2milli(long nano) {return (long) (nano / 1e6);}

    public static final int GPS_MIN_TIME = 1000;
    public static final int GPS_MIN_DISTANCE = 0;



    public enum LogMessageType {
        KALMAN_ALLOC,
        KALMAN_PREDICT,
        KALMAN_UPDATE,
        GPS_DATA,
        ABS_ACC_DATA,
        FILTERED_GPS_DATA,
        FINAL_DISTANCE,
        ABS_ACC_MEAN_DATA
    }
}

