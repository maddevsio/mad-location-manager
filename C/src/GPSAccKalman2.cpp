#include <assert.h>
#include "GPSAccKalman2.h"
#include "Matrix.h"
#include "Kalman.h"
#include <QFile>

GPSAccKalmanFilter2_t *GPSAccKalman2Alloc(double x,
                                          double y,
                                          double xVel,
                                          double yVel,
                                          double accDev,
                                          double posDev,
                                          double timeStamp) {
  GPSAccKalmanFilter2_t *f = (GPSAccKalmanFilter2_t*) malloc(sizeof(GPSAccKalmanFilter2_t));
  assert(f);
  f->kf = KalmanFilterCreate(4, 4, 2);
  /*initialization*/
  f->predictTime = f->updateTime = timeStamp;
  f->accDev = accDev;

  MatrixSet(f->kf->Xk_k,
            x, y, xVel, yVel);

  MatrixSetIdentity(f->kf->H); //state has 4d and measurement has 4d too. so here is identity

  MatrixSet(f->kf->Pk_k,
            posDev, 0.0, 0.0, 0.0,
            0.0, posDev, 0.0, 0.0,
            0.0, 0.0, posDev, 0.0,
            0.0, 0.0, 0.0, posDev); //todo get speed accuracy if possible

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

static void rebuildU(GPSAccKalmanFilter2_t *f,
                     double xAcc,
                     double yAcc) {
  MatrixSet(f->kf->Uk,
            xAcc,
            yAcc);
}

static void rebuildB(GPSAccKalmanFilter2_t *f,
                     double dt) {
  double dt05 = 0.5*dt;
  MatrixSet(f->kf->B,
            dt05, 0.0,
            0.0, dt05,
            dt, 0.0,
            0.0, dt);
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
  UNUSED_ARG(dt);
//  MatrixSetIdentity(f->kf->Q);
//  MatrixScale(f->kf->Q, accSigma * dt);

//  1st variant from Wiki. Shows bad results
//  MatrixMultiplyByTranspose(f->kf->B, f->kf->B, f->kf->Q);
//  MatrixScale(f->kf->Q, accSigma);

//  2nd Variant from Wiki. It shows much worse results. Maybe we need to check
  dt *= 1000.0;
  double dt2 = dt*dt;
  double dt3 = dt2*dt;
  double dt4 = dt3*dt;
  MatrixSet(f->kf->Q,
            0.25*dt4, 0.0, 0.5*dt3, 0.0,
            0.0, 0.25*dt4, 0.0, 0.5*dt3,
            0.5*dt3, 0.0, dt2, 0.0,
            0.0, 0.5*dt3, 0.0, dt2);
  MatrixScale(f->kf->Q, 0.0001 * accSigma * accSigma);
}

void GPSAccKalman2Predict(GPSAccKalmanFilter2_t *k,
                          double timeNow,
                          double xAcc,
                          double yAcc) {
  double dt1 = (timeNow - k->predictTime) / 1000.0;
  double dt2 = (timeNow - k->updateTime)  / 1000.0;

  rebuildF(k, dt1);
  rebuildB(k, dt1);
  rebuildU(k, xAcc, yAcc);
  rebuildQ(k, dt2, k->accDev);

  k->predictTime = timeNow;
  KalmanFilterPredict(k->kf);
  MatrixCopy(k->kf->Xk_km1, k->kf->Xk_k);
}
//////////////////////////////////////////////////////////////////////////

#include <QDebug>
double dt2max = 0.0;
void GPSAccKalman2Update(GPSAccKalmanFilter2_t *k,
                         double timeNow,
                         double x,
                         double y,
                         double xVel,
                         double yVel,
                         double posDev,
                         double velDev) {
  double dt2 = (timeNow - k->updateTime)  / 1000.0;
  dt2max = std::max(dt2max, dt2);
  qDebug() << dt2max;
  k->updateTime = timeNow;
  rebuildR(k, posDev, velDev);
  MatrixSet(k->kf->Zk, x, y, xVel, yVel);
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////
