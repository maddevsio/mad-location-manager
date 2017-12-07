#include "Kalman.h"
#include "Matrix.h"

KalmanFilter_t *KalmanFilterCreate(int stateDimension,
                                   int measureDimension,
                                   int controlDimension)
{
  KalmanFilter_t *f = (KalmanFilter_t*) malloc(sizeof(KalmanFilter_t));
  f->stateTransitionMatrix = MatrixAlloc(stateDimension, stateDimension);
  f->measurementModel = MatrixAlloc(measureDimension, stateDimension);
  f->controlMatrix = MatrixAlloc(stateDimension, 1);
  f->processVariance = MatrixAlloc(stateDimension, stateDimension);
  f->measureVariance = MatrixAlloc(measureDimension, measureDimension);

  f->controlVector = MatrixAlloc(controlDimension, 1);
  f->actualMeasurement = MatrixAlloc(measureDimension, 1);

  f->predictedState = MatrixAlloc(stateDimension, 1);
  f->predictedCovariance = MatrixAlloc(stateDimension, stateDimension);

  f->measurementInnovation = MatrixAlloc(measureDimension, 1);
  f->measurementInnovationCovariance = MatrixAlloc(measureDimension, measureDimension);
  f->measurementInnovationCovarianceInverse = MatrixAlloc(measureDimension, measureDimension);

  f->optimalKalmanGain = MatrixAlloc(stateDimension, measureDimension);

  f->currentState = MatrixAlloc(stateDimension, 1);
  f->updatedCovariance = MatrixAlloc(stateDimension, stateDimension);
  f->measurementPostfitResidual = MatrixAlloc(measureDimension, 1);

  f->auxBxU = MatrixAlloc(stateDimension, controlDimension);
  f->auxSDxMD = MatrixAlloc(stateDimension, measureDimension);
  return f;
}
//////////////////////////////////////////////////////////////////////////

void KalmanFilterFree(KalmanFilter_t *k) {
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

  MatrixFree(k->auxBxU);
  MatrixFree(k->auxSDxMD);
}
//////////////////////////////////////////////////////////////////////////

void KalmanFilterPredict(KalmanFilter_t *k) {
  //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
  MatrixMultiply(k->stateTransitionMatrix, k->currentState, k->predictedState);
  MatrixMultiply(k->controlMatrix, k->controlVector, k->auxBxU);
  MatrixAdd(k->predictedState, k->auxBxU, k->predictedState);

  //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
  MatrixMultiply(k->stateTransitionMatrix, k->updatedCovariance, k->auxSDxMD);
  MatrixMultiplyByTranspose(k->auxSDxMD, k->stateTransitionMatrix, k->predictedCovariance);
  MatrixAdd(k->predictedCovariance, k->processVariance, k->predictedCovariance);
}
//////////////////////////////////////////////////////////////////////////

void KalmanFilterUpdate(KalmanFilter_t *k) {
  //Yk = Zk - Hk*Xk|k-1
  MatrixMultiply(k->measurementModel, k->predictedState, k->measurementInnovation);
  MatrixSubtract(k->actualMeasurement, k->measurementInnovation, k->measurementInnovation);

  //Sk = Rk + Hk*Pk|k-1*Hk(t)
  MatrixMultiplyByTranspose(k->predictedCovariance, k->measurementModel, k->auxSDxMD);
  MatrixMultiply(k->measurementModel, k->auxSDxMD, k->measurementInnovationCovariance);
  MatrixAdd(k->measureVariance, k->measurementInnovationCovariance, k->measurementInnovationCovariance);

  //Kk = Pk|k-1*Hk(t)*Sk(inv)
  if (!(MatrixDestructiveInvert(k->measurementInnovationCovariance, k->measurementInnovationCovarianceInverse)))
    return; //matrix hasn't inversion
  MatrixMultiply(k->auxSDxMD, k->measurementInnovationCovarianceInverse, k->optimalKalmanGain);

  //xk|k = xk|k-1 + Kk*Yk
  MatrixMultiply(k->optimalKalmanGain, k->measurementInnovation, k->currentState);
  MatrixAdd(k->predictedState, k->currentState, k->currentState);

  //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
  MatrixMultiply(k->optimalKalmanGain, k->measurementModel, k->auxSDxMD);
  MatrixSubtractFromIdentity(k->auxSDxMD);
  MatrixMultiply(k->auxSDxMD, k->predictedCovariance, k->updatedCovariance);

  //Yk|k = Zk - Hk*Xk|k
  MatrixMultiply(k->measurementModel, k->currentState, k->measurementPostfitResidual);
  MatrixSubtract(k->actualMeasurement, k->measurementPostfitResidual, k->measurementPostfitResidual);
}
//////////////////////////////////////////////////////////////////////////

