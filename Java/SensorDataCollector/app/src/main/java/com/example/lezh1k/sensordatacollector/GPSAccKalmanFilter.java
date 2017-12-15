package com.example.lezh1k.sensordatacollector;

/**
 * Created by lezh1k on 12/11/17.
 */

public class GPSAccKalmanFilter {
    private double m_timeStamp;
    private KalmanFilter m_kf;

    public GPSAccKalmanFilter(double initPos,
                              double initVel,
                              double positionDeviation,
                              double accelerometerDeviation,
                              double currentTimeStamp) {
        m_kf = new KalmanFilter(2, 2, 1);
  /*initialization*/
        m_timeStamp = currentTimeStamp;
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
                positionDeviation*positionDeviation , 0.0,
                0.0                                 , positionDeviation*positionDeviation);

        m_kf.processVariance.Set(
                accelerometerDeviation*accelerometerDeviation , 0.0,
                0.0                                           , accelerometerDeviation*accelerometerDeviation);
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

    public double getPredictedPosition() { return m_kf.predictedState.data[0][0]; }

    public double getPredictedVelocity() {
        return m_kf.predictedState.data[1][0];
    }


    public void Predict(double timeNow, double accAxis) {
        double deltaT = timeNow - m_timeStamp;
        rebuildControlMatrix(deltaT);
        rebuildStateTransitions(deltaT);
        m_kf.controlVector.Set(accAxis);
        m_timeStamp = timeNow;
        m_kf.Predict();
    }

    /*ToDo pass position error as REFERENCE!!! and check if it's null*/
    public void Update(double position, double velocityAxis,
                       double positionError, double velocityError) {
        m_kf.actualMeasurement.Set(position, velocityAxis);
        if (positionError != 0.0)
            m_kf.measureVariance.data[0][0] = positionError * positionError;
        m_kf.measureVariance.data[1][1] = velocityError*velocityError;
        m_kf.Update();
    }
}
