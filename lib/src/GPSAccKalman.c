#include <assert.h>
#include <stdlib.h>

#include "GPSAccKalman.h"
#include "Matrix.h"
#include "Kalman.h"

static void rebuildF(gps_accelerometer_fusion_filter_t *f, double dt);
static void rebuildU(gps_accelerometer_fusion_filter_t *f,
                     double xAcc,
                     double yAcc);
static void rebuildB(gps_accelerometer_fusion_filter_t *f,
                     double dt);
static void rebuildR(gps_accelerometer_fusion_filter_t *f,
                     double posSigma);
static void rebuildQ(gps_accelerometer_fusion_filter_t *f,
                     double accDeviation);

gps_accelerometer_fusion_filter_t *
gps_accelerometer_fusion_filter_alloc(double x,
                                      double y,
                                      double xVel,
                                      double yVel,
                                      double accDev,
                                      double posDev,
                                      double timeStamp) {
  gps_accelerometer_fusion_filter_t *f =
      malloc(sizeof(gps_accelerometer_fusion_filter_t));
  assert(f);

  f->kf = kalman_filter_create(4, 2, 2);
  f->predictTime = f->updateTime = timeStamp;
  f->predictCount = 0;
  f->accDeviation = accDev;

  matrix_set(f->kf->Xk_k,
             x, y,
             xVel, yVel);
  matrix_set_identity_diag(f->kf->H); //state has 4d and measurement has 4d too. so here is identity

  matrix_set_identity(f->kf->Pk_k);
  matrix_scale(f->kf->Pk_k, posDev); //todo get speed accuracy if possible

  return f;
}
//////////////////////////////////////////////////////////////////////////

void
gps_accelerometer_fusion_filter_free(gps_accelerometer_fusion_filter_t *k) {
  assert(k);
  kalman_filter_free(k->kf);
  free(k);
}
//////////////////////////////////////////////////////////////////////////

void
rebuildF(gps_accelerometer_fusion_filter_t *f,
         double dt) {
  matrix_set(f->kf->F,
             1.0, 0.0, dt,  0.0,
             0.0, 1.0, 0.0, dt,
             0.0, 0.0, 1.0, 0.0,
             0.0, 0.0, 0.0, 1.0);
}
//////////////////////////////////////////////////////////////////////////

void
rebuildU(gps_accelerometer_fusion_filter_t *f,
         double xAcc,
         double yAcc) {
  matrix_set(f->kf->Uk,
             xAcc,
             yAcc);
}
//////////////////////////////////////////////////////////////////////////

void
rebuildB(gps_accelerometer_fusion_filter_t *f,
         double dt) {
  double dt2 = 0.5*dt*dt;
  matrix_set(f->kf->B,
             dt2, 0.0,
             0.0, dt2,
             dt, 0.0,
             0.0, dt);
}
//////////////////////////////////////////////////////////////////////////

void
rebuildR(gps_accelerometer_fusion_filter_t *f,
         double posSigma) {
  //  MatrixSetIdentity(f->kf->R);
  //  MatrixScale(f->kf->R, posSigma*posSigma);
  double velSigma = posSigma * 1.0e-01;
  matrix_set(f->kf->R,
             posSigma, 0.0, 0.0, 0.0,
             0.0, posSigma, 0.0, 0.0,
             0.0, 0.0, velSigma, 0.0,
             0.0, 0.0, 0.0, velSigma);
}
//////////////////////////////////////////////////////////////////////////

void
rebuildQ(gps_accelerometer_fusion_filter_t *f,
         double accDeviation) {
  double velDev = accDeviation * f->predictCount;
  double posDev = velDev * f->predictCount / 2;
  double covDev = velDev*posDev;
  matrix_set(f->kf->Q,
             posDev*posDev, 0.0,           covDev,        0.0,
             0.0,           posDev*posDev, 0.0,           covDev,
             covDev,        0.0,           velDev*velDev, 0.0,
             0.0,           covDev,        0.0,           velDev*velDev);
}

void
gps_accelerometer_fusion_filter_predict(gps_accelerometer_fusion_filter_t *k,
                                        double timeNow,
                                        double xAcc,
                                        double yAcc) {
  double dt = (timeNow - k->predictTime) / 1.0e+3;

  rebuildF(k, dt);
  rebuildB(k, dt);
  rebuildU(k, xAcc, yAcc);

  ++k->predictCount;
  rebuildQ(k, k->accDeviation);

  k->predictTime = timeNow;
  kalman_filter_predict(k->kf);
  matrix_copy(k->kf->Xk_km1, k->kf->Xk_k);
}
//////////////////////////////////////////////////////////////////////////

void
gps_accelerometer_fusion_filter_update(gps_accelerometer_fusion_filter_t *k,
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
  matrix_set(k->kf->Zk, x, y, xVel, yVel);
  kalman_filter_update(k->kf);
}
//////////////////////////////////////////////////////////////////////////

double
gps_accelerometer_fusion_filter_get_x(const gps_accelerometer_fusion_filter_t *k) {
  return k->kf->Xk_k->data[0][0];
}

double
gps_accelerometer_fusion_filter_get_y(const gps_accelerometer_fusion_filter_t *k) {
  return k->kf->Xk_k->data[1][0];
}

double
gps_accelerometer_fusion_filter_get_vel_x(const gps_accelerometer_fusion_filter_t *k) {
  return k->kf->Xk_k->data[2][0];
}

double
gps_accelerometer_fusion_filter_get_vel_y(const gps_accelerometer_fusion_filter_t *k) {
  return k->kf->Xk_k->data[3][0];
}
//////////////////////////////////////////////////////////////////////////
