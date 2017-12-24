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
  f->kf = KalmanFilterCreate(4, 4, 1);
  /*initialization*/
  f->timeStamp = timeStamp;
  f->sigmaX = xDev;
  f->sigmaY = yDev;

  MatrixSet(f->kf->Xk_k,
            x, y, xVel, yVel);

  MatrixSet(f->kf->H,
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);

  MatrixSet(f->kf->Pk_k,
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);

  //warning
  MatrixSet(f->kf->R,
            posDev*posDev, 0.0, 0.0, 0.0,
            0.0, posDev*posDev, 0.0, 0.0,
            0.0, 0.0, posDev*posDev, 0.0,
            0.0, 0.0, 0.0, posDev*posDev);

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
  double dt = timeNow - k->timeStamp;
  rebuildStateTransitions(k, dt);
  rebuildControlMatrix(k, dt, xAcc, yAcc);
  k->timeStamp = timeNow;
  KalmanFilterPredict(k->kf);
  MatrixCopy(k->kf->Xk_km1, k->kf->Xk_k);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalman2Update(GPSAccKalmanFilter2_t *k,
                         double x,
                         double y,
                         double xVel,
                         double yVel,
                         double xVelErr,
                         double yVelErr) {
  MatrixSet(k->kf->Zk, x, y, xVel, yVel);
  MatrixSet(k->kf->R,
            0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, xVelErr*xVelErr, 0.0,
            0.0, 0.0, 0.0, yVelErr*yVelErr);
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////
