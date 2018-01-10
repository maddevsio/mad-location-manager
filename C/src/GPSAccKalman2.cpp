#include <assert.h>
#include "GPSAccKalman2.h"
#include "Matrix.h"
#include "Kalman.h"
#include <QFile>

QFile tf("/home/lezh1k/gps_test_data/log2");
GPSAccKalmanFilter2_t *GPSAccKalman2Alloc(double x,
                                          double y,
                                          double xVel,
                                          double yVel,
                                          double accDev,
                                          double posDev,
                                          double timeStamp) {
  tf.open(QFile::ReadWrite);
  GPSAccKalmanFilter2_t *f = (GPSAccKalmanFilter2_t*) malloc(sizeof(GPSAccKalmanFilter2_t));
  assert(f);
  f->kf = KalmanFilterCreate(4, 4, 1);
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
  tf.close();
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
  MatrixSetIdentity(f->kf->Q);
  MatrixScale(f->kf->Q, accSigma * dt);
}

void GPSAccKalman2Predict(GPSAccKalmanFilter2_t *k,
                          double timeNow,
                          double xAcc,
                          double yAcc) {
  double dt1 = (timeNow - k->predictTime) / 1000.0;
  double dt2 = (timeNow - k->updateTime)  / 1000.0;

  rebuildF(k, dt1);
  rebuildB(k, dt1, xAcc, yAcc);
  rebuildQ(k, dt2, k->accDev);

  k->predictTime = timeNow;
  KalmanFilterPredict(k->kf);
  MatrixCopy(k->kf->Xk_km1, k->kf->Xk_k);
}
//////////////////////////////////////////////////////////////////////////

void GPSAccKalman2Update(GPSAccKalmanFilter2_t *k,
                         double timeNow,
                         double x,
                         double y,
                         double xVel,
                         double yVel,
                         double posDev,
                         double velDev) {
  k->updateTime = timeNow;
  rebuildR(k, posDev, velDev);
  MatrixSet(k->kf->Zk, x, y, xVel, yVel);
  KalmanFilterUpdate(k->kf);
}
//////////////////////////////////////////////////////////////////////////
