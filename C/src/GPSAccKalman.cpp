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
  MatrixSet(f->kf->Xk_k,
            initPos,
            initVel);

  MatrixSet(f->kf->H,
            1.0, 0.0,
            0.0, 1.0);

  MatrixSet(f->kf->Pk_k,
            1.0, 0.0,
            0.0, 1.0);

  MatrixSet(f->kf->R,
            positionDeviation*positionDeviation , 0.0,
            0.0                                 , positionDeviation*positionDeviation);

  MatrixSet(f->kf->Q,
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
  MatrixSet(k->kf->F,
            1.0, deltaT,
            0.0, 1.0);

}
//////////////////////////////////////////////////////////////////////////

static void rebuildControlMatrix(GPSAccKalmanFilter_t *k, double deltaT) {
  MatrixSet(k->kf->B,
            0.5 * deltaT * deltaT,
            deltaT);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalmanPredict(GPSAccKalmanFilter_t *k,
                         double timeNow,
                         double accelerationProection) {
  double deltaT = timeNow - k->timeStamp;
  rebuildControlMatrix(k, deltaT);
  rebuildStateTransitions(k, deltaT);
  MatrixSet(k->kf->Uk, accelerationProection);
  k->timeStamp = timeNow;
  KalmanFilterPredict(k->kf);
  MatrixCopy(k->kf->Xk_km1, k->kf->Xk_k);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalmanUpdate(GPSAccKalmanFilter_t *k,
                        double position,
                        double velocityAxis,
                        double *positionError,
                        double velocityError) {
  MatrixSet(k->kf->Zk, position, velocityAxis);
  if (positionError)
    k->kf->R->data[0][0] = *positionError * *positionError;
  k->kf->R->data[1][1] = velocityError*velocityError;
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////
