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
  MatrixSet(f->kf->Xk_k,
            x, y, xVel, yVel);

  MatrixSetIdentity(f->kf->H); //state has 4d and measurement has 4d too. so here is identity
  MatrixSet(f->kf->Pk_k,
            posDev, 0.0, 0.0, 0.0,
            0.0, posDev, 0.0, 0.0,
            0.0, 0.0, posDev, 0.0,
            0.0, 0.0, 0.0, posDev); //todo get speed accuracy if possible

  //process noise.
  MatrixSetIdentity(f->kf->Q);
  MatrixScale(f->kf->Q, accDev);

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

static void rebuildF(GPSAccKalmanFilter2_t *f, double dt) {
  MatrixSet(f->kf->F,
            1.0, 0.0, dt,  0.0,
            0.0, 1.0, 0.0, dt,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildB(GPSAccKalmanFilter2_t *f,
                     double dt,
                     double xAcc,
                     double yAcc) {
  double dt05 = 0.5*dt;
  double dx = dt*xAcc;
  double dy = dt*yAcc;
  MatrixSet(f->kf->B,
            dt05 * dx,
            dt05 * dy,
            dx,
            dy);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildR(GPSAccKalmanFilter2_t *f,
                     double posSigma,
                     double velSigma) {
  MatrixSet(f->kf->R,
            posSigma, 0.0, 0.0, 0.0,
            0.0, posSigma, 0.0, 0.0,
            0.0, 0.0, velSigma, 0.0,
            0.0, 0.0, 0.0, velSigma);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildQ(GPSAccKalmanFilter2_t *f,
                     double dt,
                     double accSigma) {
  dt *= 1000.0; //to milliseconds (because dt usual < 1.0)
  double dt2 = dt*dt;
  double dt3 = dt2*dt;
  double dt4 = dt3*dt;
  MatrixSet(f->kf->Q,
            0.25*dt4, 0.5*dt3, 0.0, 0.0,
            0.5*dt3, dt2, 0.0, 0.0,
            0.0, 0.0, 0.25*dt4, 0.5*dt3,
            0.0, 0.0, 0.5*dt3, dt2);
  MatrixScale(f->kf->Q, 0.0001 * accSigma);
}

void GPSAccKalman2Predict(GPSAccKalmanFilter2_t *k,
                          double timeNow,
                          double xAcc,
                          double yAcc) {
  double dt = (timeNow - k->timeStamp) / 1000.0;
  rebuildF(k, dt);
  rebuildB(k, dt, xAcc, yAcc);
//  rebuildQ(k, dt, 1.0); //todo comment after test
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
                         double posDev,
                         double velDev) {
  rebuildR(k, posDev, velDev);
  MatrixSet(k->kf->Zk, x, y, xVel, yVel);
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////
