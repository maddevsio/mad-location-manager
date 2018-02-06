package com.example.lezh1k.sensordatacollector.Filters;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by lezh1k on 2/6/18.
 */

public class MeanFilter {
    public static final float DEFAULT_TIME_CONSTANT = 0.2f;
    private long startTime;
    private long timestamp;
    private int count;
    private int dataItemsCount;

    private float timeConstant;
    private ArrayDeque<float[]> values;

    public MeanFilter() {
        this(DEFAULT_TIME_CONSTANT, 3);
    }
    public MeanFilter(float timeConstant, int dataItemsCount) {
        this.dataItemsCount = dataItemsCount;
        this.timeConstant = timeConstant;
        this.values = new ArrayDeque();
    }

    public void filter(float[] data, long timestamp, float[] mean) {
        if(this.startTime == 0L) {
            this.startTime = timestamp;
            System.arraycopy(data, 0, mean, 0, dataItemsCount);
        }

        this.timestamp = timestamp;
        float hz = (float)(++this.count) / ((float)(this.timestamp - this.startTime) / 1.0E9F);
        int filterWindow = (int)Math.ceil((double)(hz * this.timeConstant));
        this.values.addLast(Arrays.copyOf(data, data.length));
        while(this.values.size() > filterWindow) {
            this.values.removeFirst();
        }

        this.getMean(this.values, mean);
    }

    private float[] getMean(ArrayDeque<float[]> data, float[] mean) {
//        float[] mean = new float[dataItemsCount];
        Iterator var3 = data.iterator();

        while(var3.hasNext()) {
            float[] axis = (float[])var3.next();

            for(int i = 0; i < axis.length; ++i) {
                mean[i] += axis[i];
            }
        }

        for(int i = 0; i < mean.length; ++i) {
            mean[i] /= (float)data.size();
        }

        return mean;
    }

    public void setTimeConstant(float timeConstant) {
        this.timeConstant = timeConstant;
    }

    public void reset() {
        startTime = 0L;
        if(this.values != null) {
            this.values.clear();
        }
    }
}
