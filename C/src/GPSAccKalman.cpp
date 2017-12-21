#include <assert.h>
#include "GPSAccKalman.h"
#include "Matrix.h"
#include "Kalman.h"

GPSAccKalmanFilter_t *GPSAccKalmanAlloc(double initPos,
                                        double initVel,
                                        double positionDeviation,
                                        double accelerometerDeviation,
                                        double currentTimeStamp) {
  GPSAccKalmanFilter_t *f = (GPSAccKalmanFilter_t*) malloc(sizeof(GPSAccKalmanFilter_t));
  assert(f);
  f->kf = KalmanFilterCreate(2, 2, 1);
  /*initialization*/
  f->timeStamp = currentTimeStamp;
  MatrixSet(f->kf->currentState,
            initPos,
            initVel);

  MatrixSet(f->kf->measurementModel,
            1.0, 0.0,
            0.0, 1.0);

  MatrixSet(f->kf->updatedCovariance,
            1.0, 0.0,
            0.0, 1.0);

  MatrixSet(f->kf->measureVariance,
            positionDeviation*positionDeviation , 0.0,
            0.0                                 , positionDeviation*positionDeviation);

  MatrixSet(f->kf->processVariance,
            accelerometerDeviation*accelerometerDeviation , 0.0,
            0.0                                           , accelerometerDeviation*accelerometerDeviation);
  return f;
}
//////////////////////////////////////////////////////////////////////////


void GPSAccKalmanFree(GPSAccKalmanFilter_t *k) {
  assert(k);
  KalmanFilterFree(k->kf);
  free(k);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildStateTransitions(GPSAccKalmanFilter_t *k, double deltaT) {
  MatrixSet(k->kf->stateTransitionMatrix,
            1.0, deltaT,
            0.0, 1.0);

}
//////////////////////////////////////////////////////////////////////////

static void rebuildControlMatrix(GPSAccKalmanFilter_t *k, double deltaT) {
  MatrixSet(k->kf->controlMatrix,
            0.5 * deltaT * deltaT,
            deltaT);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalmanPredict(GPSAccKalmanFilter_t *k,
                         double timeNow,
                         double accelerationProection) {
  /*these 5 operations should be out of kalman filter*/
  double deltaT = timeNow - k->timeStamp;
  rebuildControlMatrix(k, deltaT);
  rebuildStateTransitions(k, deltaT);
  MatrixSet(k->kf->controlVector, accelerationProection);
  k->timeStamp = timeNow;
  KalmanFilterPredict(k->kf);
  MatrixCopy(k->kf->predictedState, k->kf->currentState);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalmanUpdate(GPSAccKalmanFilter_t *k,
                        double position,
                        double velocityAxis,
                        double *positionError,
                        double velocityError) {
  /*prepare to kalman update*/
  MatrixSet(k->kf->actualMeasurement, position, velocityAxis);
  if (positionError)
    k->kf->measureVariance->data[0][0] = *positionError * *positionError;
  k->kf->measureVariance->data[1][1] = velocityError*velocityError;
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////
