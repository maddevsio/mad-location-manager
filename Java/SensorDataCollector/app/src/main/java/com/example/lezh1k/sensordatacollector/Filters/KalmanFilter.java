package com.example.lezh1k.sensordatacollector.Filters;

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

    public void Predict() {
        //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
        Matrix.MatrixMultiply(this.F, this.Xk_k, this.Xk_km1);
        Matrix.MatrixMultiply(this.B, this.Uk, this.auxBxU);
        Matrix.MatrixAdd(this.Xk_km1, this.auxBxU, this.Xk_km1);

        //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
        Matrix.MatrixMultiply(this.F, this.Pk_k, this.auxSDxSD);
        Matrix.MatrixMultiplyByTranspose(this.auxSDxSD, this.F, this.Pk_km1);
        Matrix.MatrixAdd(this.Pk_km1, this.Q, this.Pk_km1);
    }

    public void Update() {
        //Yk = Zk - Hk*Xk|k-1
        Matrix.MatrixMultiply(this.H, this.Xk_km1, this.Yk);
        Matrix.MatrixSubtract(this.Zk, this.Yk, this.Yk);

        //Sk = Rk + Hk*Pk|k-1*Hk(t)
        Matrix.MatrixMultiplyByTranspose(this.Pk_km1, this.H, this.auxSDxMD);
        Matrix.MatrixMultiply(this.H, this.auxSDxMD, this.Sk);
        Matrix.MatrixAdd(this.R, this.Sk, this.Sk);

        //Kk = Pk|k-1*Hk(t)*Sk(inv)
        if (!(Matrix.MatrixDestructiveInvert(this.Sk, this.SkInv)))
            return; //matrix hasn't inversion
        Matrix.MatrixMultiply(this.auxSDxMD, this.SkInv, this.K);

        //xk|k = xk|k-1 + Kk*Yk
        Matrix.MatrixMultiply(this.K, this.Yk, this.Xk_k);
        Matrix.MatrixAdd(this.Xk_km1, this.Xk_k, this.Xk_k);

        //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
        Matrix.MatrixMultiply(this.K, this.H, this.auxSDxSD);
        Matrix.MatrixSubtractFromIdentity(this.auxSDxSD);
        Matrix.MatrixMultiply(this.auxSDxSD, this.Pk_km1, this.Pk_k);

        //Yk|k = Zk - Hk*Xk|k
        Matrix.MatrixMultiply(this.H, this.Xk_k, this.Yk_k);
        Matrix.MatrixSubtract(this.Zk, this.Yk_k, this.Yk_k);
    }
}
