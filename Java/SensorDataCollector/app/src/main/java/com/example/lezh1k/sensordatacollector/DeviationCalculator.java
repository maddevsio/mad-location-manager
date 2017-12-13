package com.example.lezh1k.sensordatacollector;

import android.util.Log;

/**
 * Created by lezh1k on 12/12/17.
 */

public class DeviationCalculator {
    private static final double SigmaNotInitialized = -1.0;
    private final int measurementCalibrationCount;
    private int m_count = 0;
    private int m_valuesCount;
    private double m_sigmas[];
    private double m_measurements[][];
    private boolean m_calculated = false;
    private String id;

    public DeviationCalculator(int measurementCalibrationCount, int valuesCount, String id) {
        this.id  = id;
        this.measurementCalibrationCount = measurementCalibrationCount;
        m_valuesCount = valuesCount;
        m_measurements = new double[valuesCount][measurementCalibrationCount];
        m_sigmas = new double[valuesCount];
        for (int i = 0; i < valuesCount; ++i)
            m_sigmas[i] = SigmaNotInitialized;
    }

    private double calculateSigma(double sigma, double[] calibrations) {
        if (sigma != SigmaNotInitialized) return sigma;
        double sum = sigma = 0.0;
        for (int i = 0; i < measurementCalibrationCount; ++i) {
            sum += calibrations[i];
        }
        sum /= measurementCalibrationCount;

        for (int i = 0; i < measurementCalibrationCount; ++i) {
            sigma += Math.pow(calibrations[i] - sum, 2.0);
        }

        sigma /= measurementCalibrationCount;
        return sigma;
    }

    public void Measure(double... args) {
        if (args.length < m_valuesCount) return;
        if (m_count < measurementCalibrationCount) {
            for (int i = 0; i < m_valuesCount; ++i) {
                m_measurements[i][m_count] = args[i];
            }
            ++m_count;
        } else {
            for (int i = 0; i < m_valuesCount; ++i) {
                m_sigmas[i] = calculateSigma(m_sigmas[i], m_measurements[i]);
            }
            m_calculated = true;
        }
    }

    public double[] getSigmas() {
        return m_sigmas;
    }

    public boolean isM_calculated() {
        return m_calculated;
    }

    public String sigmasToStr() {
        String res = "";
        for (int i = 0; i < m_sigmas.length-1; ++i) {
            res += String.format("S%d=%f, ", i, m_sigmas[i]);
        }
        res += String.format("S%d=%f", m_sigmas.length-1, m_sigmas[m_sigmas.length-1]);
        return res;
    }
}
