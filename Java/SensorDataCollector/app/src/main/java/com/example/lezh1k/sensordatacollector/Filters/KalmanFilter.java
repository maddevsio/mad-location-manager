package com.example.lezh1k.sensordatacollector.Filters;

import com.example.lezh1k.sensordatacollector.CommonClasses.Commons;
import com.example.lezh1k.sensordatacollector.CommonClasses.Matrix;

/**
 * Created by lezh1k on 12/11/17.
 */

public class KalmanFilter {

    /*these matrices should be provided by user*/
    public Matrix F; //state transition model
    public Matrix H; //observation model
    public Matrix B; //control matrix
    public Matrix Q; //process noise covariance
    public Matrix R; //observation noise covariance

  /*these matrices will be updated by user*/
    public Matrix Uk; //control vector
    public Matrix Zk; //actual values (measured)
    public Matrix Xk_km1; //predicted state estimate
    public Matrix Pk_km1; //predicted estimate covariance
    public Matrix Yk; //measurement innovation

    public Matrix Sk; //innovation covariance
    public Matrix SkInv; //innovation covariance inverse

    public Matrix K; //Kalman gain (optimal)
    public Matrix Xk_k; //updated (current) state
    public Matrix Pk_k; //updated estimate covariance
    public Matrix Yk_k; //post fit residual

  /*auxiliary matrices*/
    public Matrix auxBxU;
    public Matrix auxSDxSD;
    public Matrix auxSDxMD;

    public KalmanFilter(int stateDimension,
                        int measureDimension,
                        int controlDimension) {
        this.F = new Matrix(stateDimension, stateDimension);
        this.H = new Matrix(measureDimension, stateDimension);
        this.Q = new Matrix(stateDimension, stateDimension);
        this.R = new Matrix(measureDimension, measureDimension);

        this.B = new Matrix(stateDimension, 1);
        this.Uk = new Matrix(controlDimension, 1);

        this.Zk = new Matrix(measureDimension, 1);

        this.Xk_km1 = new Matrix(stateDimension, 1);
        this.Pk_km1 = new Matrix(stateDimension, stateDimension);

        this.Yk = new Matrix(measureDimension, 1);
        this.Sk = new Matrix(measureDimension, measureDimension);
        this.SkInv = new Matrix(measureDimension, measureDimension);

        this.K = new Matrix(stateDimension, measureDimension);

        this.Xk_k = new Matrix(stateDimension, 1);
        this.Pk_k = new Matrix(stateDimension, stateDimension);
        this.Yk_k = new Matrix(measureDimension, 1);

        this.auxBxU = new Matrix(stateDimension, controlDimension);
        this.auxSDxSD = new Matrix(stateDimension, stateDimension);
        this.auxSDxMD = new Matrix(stateDimension, measureDimension);
    }

    public void predict() {
        //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
        Matrix.matrixMultiply(this.F, this.Xk_k, this.Xk_km1);
        Matrix.matrixMultiply(this.B, this.Uk, this.auxBxU);
        Matrix.matrixAdd(this.Xk_km1, this.auxBxU, this.Xk_km1);

        //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
        Matrix.matrixMultiply(this.F, this.Pk_k, this.auxSDxSD);
        Matrix.matrixMultiplyByTranspose(this.auxSDxSD, this.F, this.Pk_km1);
        Matrix.matrixAdd(this.Pk_km1, this.Q, this.Pk_km1);
    }

    public void update() {
        //Yk = Zk - Hk*Xk|k-1
        Matrix.matrixMultiply(this.H, this.Xk_km1, this.Yk);
        Matrix.matrixSubtract(this.Zk, this.Yk, this.Yk);

        //Sk = Rk + Hk*Pk|k-1*Hk(t)
        Matrix.matrixMultiplyByTranspose(this.Pk_km1, this.H, this.auxSDxMD);
        Matrix.matrixMultiply(this.H, this.auxSDxMD, this.Sk);
        Matrix.matrixAdd(this.R, this.Sk, this.Sk);

        //Kk = Pk|k-1*Hk(t)*Sk(inv)
        if (!(Matrix.matrixDestructiveInvert(this.Sk, this.SkInv)))
            return; //matrix hasn't inversion
        Matrix.matrixMultiply(this.auxSDxMD, this.SkInv, this.K);

        //xk|k = xk|k-1 + Kk*Yk
        Matrix.matrixMultiply(this.K, this.Yk, this.Xk_k);
        Matrix.matrixAdd(this.Xk_km1, this.Xk_k, this.Xk_k);

        //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
        Matrix.matrixMultiply(this.K, this.H, this.auxSDxSD);
        Matrix.matrixSubtractFromIdentity(this.auxSDxSD);
        Matrix.matrixMultiply(this.auxSDxSD, this.Pk_km1, this.Pk_k);

        //Yk|k = Zk - Hk*Xk|k
        Matrix.matrixMultiply(this.H, this.Xk_k, this.Yk_k);
        Matrix.matrixSubtract(this.Zk, this.Yk_k, this.Yk_k);
    }
}
