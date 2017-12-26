package com.example.lezh1k.sensordatacollector.Filters;

import android.util.Log;

import com.example.lezh1k.sensordatacollector.Commons;

/**
 * Created by lezh1k on 12/11/17.
 */

public class GPSAccKalmanFilter {
    private double m_timeStampMs;
    private KalmanFilter m_kf;

    public GPSAccKalmanFilter(double initPos,
                              double initVel,
                              double positionDeviation,
                              double accelerometerDeviation,
                              double currentTimeStamp) {
        m_kf = new KalmanFilter(2, 2, 1);
  /*initialization*/
        m_timeStampMs = currentTimeStamp;
        m_kf.currentState.Set(
                initPos,
                initVel);

        m_kf.measurementModel.Set(
                1.0, 0.0,
                0.0, 1.0);

        m_kf.updatedCovariance.Set(
                1.0, 0.0,
                0.0, 1.0);

        m_kf.measureVariance.Set(
                positionDeviation , 0.0,
                0.0 , positionDeviation);

        m_kf.processVariance.Set(
                accelerometerDeviation, 0.0,
                0.0 , accelerometerDeviation);
    }


    private void rebuildStateTransitions(double deltaT) {
        m_kf.stateTransitionMatrix.Set(
                1.0, deltaT,
                0.0, 1.0);

    }

    private void rebuildControlMatrix(double deltaT) {
        m_kf.controlMatrix.Set(
                0.5 * deltaT * deltaT,
                deltaT);
    }

    public double getCurrentPosition() {
        return m_kf.currentState.data[0][0];
    }

    public double getCurrentVelocity() {
        return m_kf.currentState.data[1][0];
    }

    public void Predict(double timeNowMs,
                        double accAxis) {
        double deltaTInSeconds = (timeNowMs - m_timeStampMs) / 1000.0;
        rebuildControlMatrix(deltaTInSeconds);
        rebuildStateTransitions(deltaTInSeconds);
        m_kf.controlVector.data[0][0] =  accAxis;
        m_timeStampMs = timeNowMs;
        m_kf.Predict();

        //this is not right. but we have something like this :
        //predict, predict, predict, update, predict, predict, predict, update
        //so we just integrate predictions.
        Matrix.MatrixClone(m_kf.predictedState, m_kf.currentState);
    }

    public void Update(double position, double velocityAxis,
                       double positionError, double velocityError) {
        m_kf.actualMeasurement.Set(position, velocityAxis);
        if (positionError != 0.0)
            m_kf.measureVariance.data[0][0] = positionError * positionError;
        m_kf.measureVariance.data[1][1] = velocityError*velocityError;
        m_kf.Update();
    }
}
