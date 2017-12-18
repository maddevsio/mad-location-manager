package com.example.lezh1k.sensordatacollector;

/**
 * Created by lezh1k on 12/12/17.
 */

public class DeviationCalculator {
    private static final double SigmaNotInitialized = -1.0;
    private final int m_measurementCalibrationCount;
    private int m_count = 0;
    private int m_valuesCount;
    private double m_sigmas[];
    private double m_measurements[][];
    private boolean m_calculated = false;
    private String id;
    private double m_means[];

    private long m_lastTimeStamp;
    private long m_freqMeanAux;
    private double m_freqMean;

    public DeviationCalculator(int measurementCalibrationCount, int valuesCount, String id) {
        this.id  = id;
        this.m_measurementCalibrationCount = measurementCalibrationCount;
        m_valuesCount = valuesCount;
        m_measurements = new double[valuesCount][measurementCalibrationCount];
        m_sigmas = new double[valuesCount];
        m_means = new double[valuesCount];
        for (int i = 0; i < valuesCount; ++i)
            m_sigmas[i] = SigmaNotInitialized;
        m_lastTimeStamp = System.nanoTime();
    }

    private double calculateSigma(double sigma, double mean, double[] calibrations) {
        if (sigma != SigmaNotInitialized) return sigma;
        for (int i = 0; i < m_measurementCalibrationCount; ++i) {
            sigma += Math.pow(calibrations[i] - mean, 2.0);
        }

        sigma /= m_measurementCalibrationCount;
        return sigma;
    }

    public void Measure(double... args) {
        if (args.length < m_valuesCount) return;
        if (m_count < m_measurementCalibrationCount) {
            for (int i = 0; i < m_valuesCount; ++i) {
                m_measurements[i][m_count] = args[i];
                m_means[i] += args[i] /*/ m_measurementCalibrationCount*/;
            }
            long now = System.nanoTime();
            m_freqMeanAux += (now - m_lastTimeStamp) / m_measurementCalibrationCount;
            m_lastTimeStamp = now;
            ++m_count;
        } else {
            for (int i = 0; i < m_valuesCount; ++i) {
                m_means[i] /= m_measurementCalibrationCount;
                m_sigmas[i] = calculateSigma(m_sigmas[i], m_means[i], m_measurements[i]);
            }
            m_calculated = true;
            m_freqMean = 1.0 / (m_freqMeanAux * 1e-9);
        }
    }

    public void Measure(float[] args) {
        if (args.length < m_valuesCount) return;
        if (m_count > m_measurementCalibrationCount) return;
        if (m_count < m_measurementCalibrationCount) {
            for (int i = 0; i < m_valuesCount; ++i) {
                m_measurements[i][m_count] = args[i];
                m_means[i] += args[i] / m_measurementCalibrationCount;
            }
            long now = System.nanoTime();
            m_freqMeanAux += (now - m_lastTimeStamp) / m_measurementCalibrationCount;
            m_lastTimeStamp = now;
        } else {
            for (int i = 0; i < m_valuesCount; ++i) {
                m_sigmas[i] = calculateSigma(m_sigmas[i], m_means[i], m_measurements[i]);
            }
            m_calculated = true;
            m_freqMean = 1.0 / (m_freqMeanAux * 1e-9);
        }
        ++m_count;
    }

    public double[] getSigmas() {
        return m_sigmas;
    }
    public boolean isM_calculated() {
        return m_calculated;
    }
    public double getFrequencyMean() { return m_freqMean; }

    public String deviationInfoString() {
        String res = "";
        for (int i = 0; i < m_valuesCount; ++i) {
//            res += String.format("%d:%f%f,", i, m_sigmas[i], m_means[i]);
            res += String.format("%d:%f,", i, m_means[i]);
        }
        res += String.format("Freq:%f", m_freqMean);
        return res;
    }
}
