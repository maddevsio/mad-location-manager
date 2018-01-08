package com.example.lezh1k.sensordatacollector.Filters;

import android.util.Log;

import com.example.lezh1k.sensordatacollector.Commons;

/**
 * Created by lezh1k on 12/11/17.
 */

public class GPSAccKalmanFilter {
    private double m_timeStampMs;
    private KalmanFilter m_kf;

    public GPSAccKalmanFilter(double x, double y,
                              double xVel, double yVel,
                              double accDev, double posDev,
                              double timeStamp) {
        m_kf = new KalmanFilter(4, 4, 1);
        m_timeStampMs = timeStamp;
        m_kf.Xk_k.Set(x, y, xVel, yVel);

        m_kf.H.SetIdentity(); //state has 4d and measurement has 4d too. so here is identity
        m_kf.Pk_k.Set(
                posDev, 0.0, 0.0, 0.0,
                0.0, posDev, 0.0, 0.0,
                0.0, 0.0, posDev, 0.0,
                0.0, 0.0, 0.0, posDev); //todo get speed accuracy if possible

        //process noise.
        m_kf.Q.SetIdentity();
        m_kf.Q.Scale(accDev);
        m_kf.Uk.Set( 1.0);
    }

    private void rebuildF(double dt) {
        double f[] = {
                1.0, 0.0, dt, 0.0,
                0.0, 1.0, 0.0, dt,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
        };
        m_kf.F.Set(f);
    }

    private void rebuildB(double dt,
                          double xAcc,
                          double yAcc) {
        double dt05 = 0.5*dt;
        double dx = dt*xAcc;
        double dy = dt*yAcc;
        double b[] = {
                dt05 * dx,
                dt05 * dy,
                dx,
                dy
        };
        m_kf.B.Set(b);
    }

    private void rebuildR(double posSigma,
                          double velSigma) {
        double R[] = {
                posSigma, 0.0, 0.0, 0.0,
                0.0, posSigma, 0.0, 0.0,
                0.0, 0.0, velSigma, 0.0,
                0.0, 0.0, 0.0, velSigma};
        m_kf.R.Set(R);
    }

    public void predict(double timeNowMs,
                        double xAcc,
                        double yAcc) {
        double dt = (timeNowMs - m_timeStampMs) / 1000.0;
        rebuildF(dt);
        rebuildB(dt, xAcc, yAcc);
        m_timeStampMs = timeNowMs;
        m_kf.Predict();
        Matrix.MatrixClone(m_kf.Xk_km1, m_kf.Xk_k);
    }

    public void update(double x,
                       double y,
                       double xVel,
                       double yVel,
                       double posDev,
                       double velDev) {
        rebuildR(posDev, velDev);
        m_kf.Zk.Set(x, y, xVel, yVel);
        m_kf.Predict();
    }

    public double getCurrentX() {
        return m_kf.Xk_k.data[0][0];
    }

    public double getCurrentY() {
        return m_kf.Xk_k.data[1][0];
    }

    public double getCurrentXVel() {
        return m_kf.Xk_k.data[2][0];
    }

    public double getCurrentYVel() {
        return m_kf.Xk_k.data[3][0];
    }
}
