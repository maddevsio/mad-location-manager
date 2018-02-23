#include <assert.h>
#include "GPSAccKalman.h"
#include "Matrix.h"
#include "Kalman.h"
#include <QFile>

GPSAccKalmanFilter_t *GPSAccKalmanAlloc(double x,
                                        double y,
                                        double xVel,
                                        double yVel,
                                        double accDev,
                                        double posDev,
                                        double timeStamp) {
  GPSAccKalmanFilter_t *f = (GPSAccKalmanFilter_t*) malloc(sizeof(GPSAccKalmanFilter_t));
  assert(f);
  f->kf = KalmanFilterCreate(4, 2, 2);
  /*initialization*/
  f->predictTime = f->updateTime = timeStamp;
  f->predictCount = 0;
  f->accDev = accDev;

  MatrixSet(f->kf->Xk_k,
            x, y, xVel, yVel);
  MatrixSetIdentityDiag(f->kf->H); //state has 4d and measurement has 4d too. so here is identity

  MatrixSetIdentity(f->kf->Pk_k);
  MatrixScale(f->kf->Pk_k, posDev); //todo get speed accuracy if possible

  return f;
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalmanFree(GPSAccKalmanFilter_t *k) {
  assert(k);
  KalmanFilterFree(k->kf);
  free(k);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildF(GPSAccKalmanFilter_t *f, double dt) {
  MatrixSet(f->kf->F,
            1.0, 0.0, dt,  0.0,
            0.0, 1.0, 0.0, dt,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildU(GPSAccKalmanFilter_t *f,
                     double xAcc,
                     double yAcc) {
  MatrixSet(f->kf->Uk,
            xAcc,
            yAcc);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildB(GPSAccKalmanFilter_t *f,
                     double dt) {
  double dt2 = 0.5*dt*dt;
  MatrixSet(f->kf->B,
            dt2, 0.0,
            0.0, dt2,
            dt, 0.0,
            0.0, dt);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildR(GPSAccKalmanFilter_t *f,
                     double posSigma) {
//  MatrixSetIdentity(f->kf->R);
//  MatrixScale(f->kf->R, posSigma*posSigma);
  double velSigma = posSigma * 1.0e-01;
  MatrixSet(f->kf->R,
            posSigma, 0.0, 0.0, 0.0,
            0.0, posSigma, 0.0, 0.0,
            0.0, 0.0, velSigma, 0.0,
            0.0, 0.0, 0.0, velSigma);
}
//////////////////////////////////////////////////////////////////////////

static void rebuildQ(GPSAccKalmanFilter_t *f,
              double accDeviation) {
//  MatrixSetIdentity(f->kf->Q);
//  MatrixScale(f->kf->Q, accSigma * dt);

  double velDev = accDeviation * f->predictCount;
  double posDev = velDev * f->predictCount / 2;
  double covDev = velDev*posDev;
  MatrixSet(f->kf->Q,
            posDev*posDev, 0.0, covDev, 0.0,
            0.0, posDev*posDev, 0.0, covDev,
            0.0, 0.0, velDev*velDev, 0.0,
            0.0, 0.0, 0.0, velDev*velDev);
}

void GPSAccKalmanPredict(GPSAccKalmanFilter_t *k,
                         double timeNow,
                         double xAcc,
                         double yAcc) {
  double dt = (timeNow - k->predictTime) / 1.0e+3;

  rebuildF(k, dt);
  rebuildB(k, dt);
  rebuildU(k, xAcc, yAcc);

  ++k->predictCount;
  rebuildQ(k, k->accDev);

  k->predictTime = timeNow;
  KalmanFilterPredict(k->kf);
  MatrixCopy(k->kf->Xk_km1, k->kf->Xk_k);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalmanUpdate(GPSAccKalmanFilter_t *k,
                        double timeNow,
                        double x,
                        double y,
                        double xVel,
                        double yVel,
                        double posDev) {
//  double dt = timeNow - k->updateTime;
  k->predictCount = 0;
  k->updateTime = timeNow;
  rebuildR(k, posDev);
  MatrixSet(k->kf->Zk, x, y, xVel, yVel);
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////

double GPSAccKalmanGetX(const GPSAccKalmanFilter_t *k) {
  return k->kf->Xk_k->data[0][0];
}

double GPSAccKalmanGetY(const GPSAccKalmanFilter_t *k) {
  return k->kf->Xk_k->data[1][0];
}

double GPSAccKalmanGetXVel(const GPSAccKalmanFilter_t *k) {
  return k->kf->Xk_k->data[2][0];
}

double GPSAccKalmanGetYVel(const GPSAccKalmanFilter_t *k) {
  return k->kf->Xk_k->data[3][0];
}
//////////////////////////////////////////////////////////////////////////
