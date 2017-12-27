#include <assert.h>
#include "GPSAccKalman2.h"
#include "Matrix.h"
#include "Kalman.h"

GPSAccKalmanFilter2_t *GPSAccKalman2Alloc(double x,
                                          double y,
                                          double xVel,
                                          double yVel,
                                          double accDev,
                                          double posDev,
                                          double timeStamp) {

  GPSAccKalmanFilter2_t *f = (GPSAccKalmanFilter2_t*) malloc(sizeof(GPSAccKalmanFilter2_t));
  assert(f);
  f->kf = KalmanFilterCreate(4, 4, 1);
  /*initialization*/
  f->timeStamp = timeStamp;
  f->accSigma = accDev*accDev;
  MatrixSet(f->kf->Xk_k,
            x, y, xVel, yVel);

  MatrixSet(f->kf->H,
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);

  MatrixSetIdentity(f->kf->Pk_k);
  MatrixScale(f->kf->Pk_k, posDev*posDev);

  MatrixSetIdentity(f->kf->R);
  MatrixScale(f->kf->R, posDev*posDev);

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

static void rebuildF(GPSAccKalmanFilter2_t *k, double dt) {
  MatrixSet(k->kf->F,
            1.0, 0.0, dt,  0.0,
            0.0, 1.0, 0.0, dt,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildB(GPSAccKalmanFilter2_t *k,
                     double dt,
                     double xAcc,
                     double yAcc) {
  double dt05 = 0.5*dt;
  double dx = dt*xAcc;
  double dy = dt*yAcc;
  MatrixSet(k->kf->B,
            dt05 * dx,
            dt05 * dy,
            dx,
            dy);
}
//////////////////////////////////////////////////////////////////////////

static void rebuilQ(GPSAccKalmanFilter2_t *k,
                    double dt) {
  double dt2 = dt*dt;
  double dt3 = dt2*dt;
  double dt4 = dt3*dt;
  static double sigma = k->accSigma; //todo change to acceleration deviation ** 2
  MatrixSet(k->kf->Q,
            0.25*dt4, 0.5*dt3,
            0.5*dt3, dt2);
  MatrixScale(k->kf->Q, sigma);
}

void GPSAccKalman2Predict(GPSAccKalmanFilter2_t *k,
                          double timeNow,
                          double xAcc,
                          double yAcc) {
  double dt = (timeNow - k->timeStamp) / 1000.0;
  rebuildF(k, dt);
  rebuildB(k, dt, xAcc, yAcc);
  rebuilQ(k, dt);
  k->timeStamp = timeNow;
  KalmanFilterPredict(k->kf);
  MatrixCopy(k->kf->Xk_km1, k->kf->Xk_k);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalman2Update(GPSAccKalmanFilter2_t *k,
                         double x,
                         double y,
                         double xVel,
                         double yVel) {
  MatrixSet(k->kf->Zk, x, y, xVel, yVel);
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////
