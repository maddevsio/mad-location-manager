#include <assert.h>
#include "GPSAccKalman2.h"
#include "Matrix.h"
#include "Kalman.h"

GPSAccKalmanFilter2_t *GPSAccKalman2Alloc(double x, double y,
                                         double xVel, double yVel,
                                         double xAcc, double yAcc,
                                         double xDev, double yDev,
                                         double timeStamp) {
  GPSAccKalmanFilter2_t *f = (GPSAccKalmanFilter2_t*) malloc(sizeof(GPSAccKalmanFilter2_t));

  assert(f);
  f->kf = KalmanFilterCreate(6, 2, 1);
  /*initialization*/
  f->timeStamp = timeStamp;
  f->sigmaX = xDev;
  f->sigmaY = yDev;

  MatrixSet(f->kf->Xk_k,
            x, y, xVel, yVel, xAcc, yAcc);

  MatrixSet(f->kf->H,
            1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0, 0.0, 0.0);

  MatrixSet(f->kf->Pk_k,
            1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 1.0);

  //warning
  MatrixSet(f->kf->R,
            4.0, 0.0,
            0.0, 4.0);

  MatrixSet(f->kf->Q,
            0.04, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.04, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.04, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.04, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.04, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.04);
  //////////////////////////////////////////////////////////////////////////

  MatrixSet(f->kf->Uk, 0.0);

  return f;
}
//////////////////////////////////////////////////////////////////////////


void GPSAccKalman2Free(GPSAccKalmanFilter2_t *k) {
  assert(k);
  KalmanFilterFree(k->kf);
  free(k);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildProcessVariance(GPSAccKalmanFilter2_t *k) {
  MatrixMultiplyByTranspose(k->kf->B, k->kf->B, k->kf->Q);
}

static void rebuildStateTransitions(GPSAccKalmanFilter2_t *k, double dt) {
  double dt2 = dt*dt;
  double dt25 = 0.5*dt2;
  MatrixSet(k->kf->F,
            1.0, 0.0, dt,  0.0, dt25, 0.0,
            0.0, 1.0, 0.0, dt,  0.0,  dt25,
            0.0, 0.0, 1.0, 0.0, dt,   0.0,
            0.0, 0.0, 0.0, 1.0, 0.0,  dt,
            0.0, 0.0, 0.0, 0.0, 1.0,  0.0,
            0.0, 0.0, 0.0, 0.0, 0.0,  1.0);

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
            dt * yAcc,
            xAcc,
            yAcc);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalman2Predict(GPSAccKalmanFilter2_t *k,
                         double timeNow,
                         double xAcc,
                         double yAcc) {
  double dt = timeNow - k->timeStamp;
  rebuildStateTransitions(k, dt);
  rebuildControlMatrix(k, dt, xAcc, yAcc);
//  rebuildProcessVariance(k);
  k->timeStamp = timeNow;
  KalmanFilterPredict(k->kf);
  MatrixCopy(k->kf->Xk_km1, k->kf->Xk_k);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalman2Update(GPSAccKalmanFilter2_t *k,
                        double x, double y) {
  MatrixSet(k->kf->Zk, x, y);
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////
