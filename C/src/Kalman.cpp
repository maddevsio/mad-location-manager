#include <assert.h>
#include "Kalman.h"
#include "Matrix.h"

static void allocateMemoryForKalman(KalmanFilter_t *f,
                                    int stateDimension,
                                    int measureDimension,
                                    int controlDimension);

KalmanFilter_t *GPSAccKalmanAlloc(double initPos,
                            double initVel,
                            double positionDeviation,
                            double accelerometerDeviation,
                            double currentTimeStamp) {
  KalmanFilter_t *f = (KalmanFilter_t*) malloc(sizeof(KalmanFilter_t));
  assert(f);
  allocateMemoryForKalman(f, 2, 2, 1);

  /*initialization*/
  f->timeStamp = currentTimeStamp;
  MatrixSet(f->currentState,
            initPos,
            initVel);

  MatrixSet(f->measurementModel,
            1.0, 0.0,
            0.0, 1.0);

  MatrixSet(f->updatedCovariance,
            1.0, 0.0,
            0.0, 1.0);

  MatrixSet(f->measureVariance,
            positionDeviation*positionDeviation , 0.0,
            0.0                                 , positionDeviation*positionDeviation);

  MatrixSet(f->processVariance,
            accelerometerDeviation*accelerometerDeviation , 0.0,
            0.0                                           , accelerometerDeviation*accelerometerDeviation);
  MatrixPrint(f->currentState);

  return f;
}
//////////////////////////////////////////////////////////////////////////

void allocateMemoryForKalman(KalmanFilter_t *f,
                             int stateDimension,
                             int measureDimension,
                             int controlDimension) {

  f->stateTransitionMatrix = MatrixAlloc(stateDimension, stateDimension);
  f->measurementModel = MatrixAlloc(measureDimension, measureDimension);
  f->controlMatrix = MatrixAlloc(stateDimension, 1);
  f->processVariance = MatrixAlloc(stateDimension, stateDimension);
  f->measureVariance = MatrixAlloc(measureDimension, measureDimension);

  f->controlVector = MatrixAlloc(controlDimension, 1);
  f->actualMeasurement = MatrixAlloc(stateDimension, 1);

  f->predictedState = MatrixAlloc(stateDimension, 1);
  f->predictedCovariance = MatrixAlloc(stateDimension, stateDimension);

  f->measurementInnovation = MatrixAlloc(measureDimension, 1);
  f->measurementInnovationCovariance = MatrixAlloc(measureDimension, measureDimension);
  f->measurementInnovationCovarianceInverse = MatrixAlloc(measureDimension, measureDimension);

  f->optimalKalmanGain = MatrixAlloc(stateDimension, measureDimension);

  f->currentState = MatrixAlloc(stateDimension, 1);
  f->updatedCovariance = MatrixAlloc(stateDimension, stateDimension);
  f->measurementPostfitResidual = MatrixAlloc(measureDimension, 1);

  f->auxMx1 = MatrixAlloc(stateDimension, 1);
  f->auxMxM = MatrixAlloc(stateDimension, stateDimension);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalmanFree(KalmanFilter_t *k) {
  MatrixFree(k->stateTransitionMatrix); //Fk
  MatrixFree(k->measurementModel); //Hk
  MatrixFree(k->controlMatrix); //Bk
  MatrixFree(k->processVariance); //Q
  MatrixFree(k->measureVariance); //R

  MatrixFree(k->controlVector); //Uk (Accelerometer)
  MatrixFree(k->actualMeasurement); //Zk (GPS)

  MatrixFree(k->predictedState); //Xk|k-1
  MatrixFree(k->predictedCovariance); //Pk|k-1
  MatrixFree(k->measurementInnovation); //Yk

  MatrixFree(k->measurementInnovationCovariance); //Sk
  MatrixFree(k->measurementInnovationCovarianceInverse); //Sk(-1)

  MatrixFree(k->optimalKalmanGain); //Kk
  MatrixFree(k->currentState); //Xk|k
  MatrixFree(k->updatedCovariance); //Pk|k
  MatrixFree(k->measurementPostfitResidual); //Yk|k

  MatrixFree(k->auxMx1);
  MatrixFree(k->auxMxM);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildStateTransitions(KalmanFilter_t *k, double deltaT) {
  MatrixSet(k->stateTransitionMatrix,
            1.0, deltaT,
            0.0, 1.0);

}
//////////////////////////////////////////////////////////////////////////

static void rebuildControlMatrix(KalmanFilter_t *k, double deltaT) {
  MatrixSet(k->controlMatrix,
            0.5 * deltaT * deltaT,
            deltaT);
}
//////////////////////////////////////////////////////////////////////////

static void kalmanPredict(KalmanFilter_t *k) {
  //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
  MatrixMultiply(k->stateTransitionMatrix, k->currentState, k->predictedState);
  MatrixMultiply(k->controlMatrix, k->controlVector, k->auxMx1);
  MatrixAdd(k->predictedState, k->auxMx1, k->predictedState);

  //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
  MatrixMultiply(k->stateTransitionMatrix, k->updatedCovariance, k->auxMxM);
  MatrixMultiplyByTranspose(k->auxMxM, k->stateTransitionMatrix, k->predictedCovariance);
  MatrixAdd(k->predictedCovariance, k->processVariance, k->predictedCovariance);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalmanPredict(KalmanFilter_t *k,
                   double timeNow,
                   double accelerationProection) {
  /*these 5 operations should be out of kalman filter*/
  double deltaT = timeNow - k->timeStamp;
  rebuildControlMatrix(k, deltaT);
  rebuildStateTransitions(k, deltaT);
  MatrixSet(k->controlVector, accelerationProection);
  k->timeStamp = timeNow;
  kalmanPredict(k);
}
//////////////////////////////////////////////////////////////////////////

static void kalmanUpdate(KalmanFilter_t *k) {
  //Yk = Zk - Hk*Xk|k-1
  MatrixMultiply(k->measurementModel, k->predictedState, k->measurementInnovation);
  MatrixSubtract(k->actualMeasurement, k->measurementInnovation, k->measurementInnovation);

  //Sk = Rk + Hk*Pk|k-1*Hk(t)
  MatrixMultiplyByTranspose(k->predictedCovariance, k->measurementModel, k->auxMxM);
  MatrixMultiply(k->measurementModel, k->auxMxM, k->measurementInnovationCovariance);
  MatrixAdd(k->measureVariance, k->measurementInnovationCovariance, k->measurementInnovationCovariance);

  //Kk = Pk|k-1*Hk(t)*Sk(inv)
  if (!(MatrixDestructiveInvert(k->measurementInnovationCovariance, k->measurementInnovationCovarianceInverse)))
    return; //matrix hasn't inversion
  MatrixMultiply(k->auxMxM, k->measurementInnovationCovarianceInverse, k->optimalKalmanGain);

  //xk|k = xk|k-1 + Kk*Yk
  MatrixMultiply(k->optimalKalmanGain, k->measurementInnovation, k->currentState);
  MatrixAdd(k->predictedState, k->currentState, k->currentState);

  //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
  MatrixMultiply(k->optimalKalmanGain, k->measurementModel, k->auxMxM);
  MatrixSubtractFromIdentity(k->auxMxM);
  MatrixMultiply(k->auxMxM, k->predictedCovariance, k->updatedCovariance);

  //Yk|k = Zk - Hk*Xk|k
  MatrixMultiply(k->measurementModel, k->currentState, k->measurementPostfitResidual);
  MatrixSubtract(k->actualMeasurement, k->measurementPostfitResidual, k->measurementPostfitResidual);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalmanUpdate(KalmanFilter_t *k,
                  double position,
                  double velocityAxis,
                  double *positionError,
                  double velocityError) {
  /*prepare to kalman update*/
  MatrixSet(k->actualMeasurement, position, velocityAxis);
  if (positionError)
    k->measureVariance->data[0][0] = *positionError * *positionError;
  k->measureVariance->data[1][1] = velocityError*velocityError;
  kalmanUpdate(k);
}
//////////////////////////////////////////////////////////////////////////
