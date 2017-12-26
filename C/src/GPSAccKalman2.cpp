#include <assert.h>
#include "GPSAccKalman2.h"
#include "Matrix.h"
#include "Kalman.h"

GPSAccKalmanFilter2_t *GPSAccKalman2Alloc(double x, double y,
                                         double xVel, double yVel,
                                         double xDev, double yDev,
                                         double posDev, double timeStamp) {
  GPSAccKalmanFilter2_t *f = (GPSAccKalmanFilter2_t*) malloc(sizeof(GPSAccKalmanFilter2_t));

  assert(f);
  f->kf = KalmanFilterCreate(4, 2, 1);
  /*initialization*/
  f->timeStamp = timeStamp;

  MatrixSet(f->kf->Xk_k,
            x, y, xVel, yVel);

  MatrixSet(f->kf->H,
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0);

  MatrixSet(f->kf->Pk_k,
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);

  //warning
  MatrixSet(f->kf->R,
            posDev*posDev, 0.0,
            0.0, posDev*posDev);

  //todo change to G*G(t)*sigma
//  xDev = yDev = posDev / 2.0;
  MatrixSet(f->kf->Q,
            xDev*xDev, 0.0, 0.0, 0.0,
            0.0, yDev*yDev, 0.0, 0.0,
            0.0, 0.0, xDev*xDev, 0.0,
            0.0, 0.0, 0.0, yDev*yDev);

  //////////////////////////////////////////////////////////////////////////
  MatrixSet(f->kf->Uk, 1.0);
  return f;
}
//////////////////////////////////////////////////////////////////////////


void GPSAccKalman2Free(GPSAccKalmanFilter2_t *k) {
  assert(k);
  KalmanFilterFree(k->kf);
  free(k);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildStateTransitions(GPSAccKalmanFilter2_t *k, double dt) {
  MatrixSet(k->kf->F,
            1.0, 0.0, dt,  0.0,
            0.0, 1.0, 0.0, dt,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildControlMatrix(GPSAccKalmanFilter2_t *k,
                                 double dt,
                                 double xAcc,
                                 double yAcc) {
  MatrixSet(k->kf->B,
            0.5 * dt * dt * xAcc,
            0.5 * dt * dt * yAcc,
            dt * xAcc,
            dt * yAcc);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalman2Predict(GPSAccKalmanFilter2_t *k,
                         double timeNow,
                         double xAcc,
                         double yAcc) {
  double dt = (timeNow - k->timeStamp) / 1000.0;
  rebuildStateTransitions(k, dt);
  rebuildControlMatrix(k, dt, xAcc, yAcc);
  k->timeStamp = timeNow;
  KalmanFilterPredict(k->kf);
  MatrixCopy(k->kf->Xk_km1, k->kf->Xk_k);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalman2Update(GPSAccKalmanFilter2_t *k,
                         double x,
                         double y) {
  MatrixSet(k->kf->Zk, x, y, 0.0, 0.0); // ????
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////
