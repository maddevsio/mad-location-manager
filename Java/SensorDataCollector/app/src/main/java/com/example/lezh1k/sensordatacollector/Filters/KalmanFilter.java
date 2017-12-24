package com.example.lezh1k.sensordatacollector.Filters;

/**
 * Created by lezh1k on 12/11/17.
 */

public class KalmanFilter {

    /*these matrices should be provided by user*/
    public Matrix stateTransitionMatrix; //Fk
    public Matrix measurementModel; //Hk --> converts somehow measured data to model data.
    public Matrix controlMatrix; //Bk
    public Matrix processVariance; //Q
    public Matrix measureVariance; //R

    /*these matrices will be updated by user*/
    public Matrix controlVector; //Uk
    public Matrix actualMeasurement; //Zk
    public Matrix predictedState; //Xk|k-1
    public Matrix predictedCovariance; //Pk|k-1
    public Matrix measurementInnovation; //Yk

    public Matrix measurementInnovationCovariance; //Sk
    public Matrix measurementInnovationCovarianceInverse; //Sk(-1)

    public Matrix optimalKalmanGain; //Kk
    public Matrix currentState; //Xk|k
    public Matrix updatedCovariance; //Pk|k
    public Matrix measurementPostfitResidual; //Yk|k

    /*auxiliary matrices*/
    private Matrix auxBxU;
    private Matrix auxSDxSD;
    private Matrix auxSDxMD;

    public KalmanFilter(int stateDimension,
                        int measureDimension,
                        int controlDimension) {
        this.stateTransitionMatrix = new Matrix(stateDimension, stateDimension);
        this.measurementModel = new Matrix(measureDimension, stateDimension);
        this.processVariance = new Matrix(stateDimension, stateDimension);
        this.measureVariance = new Matrix(measureDimension, measureDimension);

        this.controlMatrix = new Matrix(stateDimension, 1);
        this.controlVector = new Matrix(controlDimension, 1);
        this.actualMeasurement = new Matrix(measureDimension, 1);

        this.predictedState = new Matrix(stateDimension, 1);
        this.predictedCovariance = new Matrix(stateDimension, stateDimension);

        this.measurementInnovation = new Matrix(measureDimension, 1);
        this.measurementInnovationCovariance = new Matrix(measureDimension, measureDimension);
        this.measurementInnovationCovarianceInverse = new Matrix(measureDimension, measureDimension);

        this.optimalKalmanGain = new Matrix(stateDimension, measureDimension);

        this.currentState = new Matrix(stateDimension, 1);
        this.updatedCovariance = new Matrix(stateDimension, stateDimension);
        this.measurementPostfitResidual = new Matrix(measureDimension, 1);

        this.auxBxU = new Matrix(stateDimension, controlDimension);
        this.auxSDxSD = new Matrix(stateDimension, stateDimension);
        this.auxSDxMD = new Matrix(stateDimension, measureDimension);
    }

    public void Predict() {
        //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
        Matrix.MatrixMultiply(stateTransitionMatrix, currentState, predictedState);
        Matrix.MatrixMultiply(controlMatrix, controlVector, auxBxU);
        Matrix.MatrixAdd(predictedState, auxBxU, predictedState);

        //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
        Matrix.MatrixMultiply(stateTransitionMatrix, updatedCovariance, auxSDxSD);
        Matrix.MatrixMultiplyByTranspose(auxSDxSD, stateTransitionMatrix, predictedCovariance);
        Matrix.MatrixAdd(predictedCovariance, processVariance, predictedCovariance);
    }

    public void Update() {
        //Yk = Zk - Hk*Xk|k-1
        Matrix.MatrixMultiply(measurementModel, predictedState, measurementInnovation);
        Matrix.MatrixSubtract(actualMeasurement, measurementInnovation, measurementInnovation);

        //Sk = Rk + Hk*Pk|k-1*Hk(t)
        Matrix.MatrixMultiplyByTranspose(predictedCovariance, measurementModel, auxSDxMD);
        Matrix.MatrixMultiply(measurementModel, auxSDxMD, measurementInnovationCovariance);
        Matrix.MatrixAdd(measureVariance, measurementInnovationCovariance, measurementInnovationCovariance);

        //Kk = Pk|k-1*Hk(t)*Sk(inv)
        if (!(Matrix.MatrixDestructiveInvert(measurementInnovationCovariance, measurementInnovationCovarianceInverse)))
            return; //matrix hasn't inversion
        Matrix.MatrixMultiply(auxSDxMD, measurementInnovationCovarianceInverse, optimalKalmanGain);

        //xk|k = xk|k-1 + Kk*Yk
        Matrix.MatrixMultiply(optimalKalmanGain, measurementInnovation, currentState);
        Matrix.MatrixAdd(predictedState, currentState, currentState);

        //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
        Matrix.MatrixMultiply(optimalKalmanGain, measurementModel, auxSDxSD);
        auxSDxSD.SubtractFromIdentity();
        Matrix.MatrixMultiply(auxSDxSD, predictedCovariance, updatedCovariance);

        //Yk|k = Zk - Hk*Xk|k
        Matrix.MatrixMultiply(measurementModel, currentState, measurementPostfitResidual);
        Matrix.MatrixSubtract(actualMeasurement, measurementPostfitResidual, measurementPostfitResidual);
    }
}
