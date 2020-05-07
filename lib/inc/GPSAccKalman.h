#ifndef GPSACCKALMAN_H
#define GPSACCKALMAN_H

#include <stdint.h>
#include "Kalman.h"
#include "Matrix.h"

typedef struct GPSAccKalmanFilter {
  double predictTime;
  double updateTime;
  double accDev;
  uint32_t predictCount;
  KalmanFilter_t *kf;
} GPSAccKalmanFilter_t;

GPSAccKalmanFilter_t* GPSAccKalmanAlloc(double x, double y,
    double xVel, double yVel,
    double accDev, double posDev,
    double timeStamp);

void GPSAccKalmanFree(GPSAccKalmanFilter_t *k);

void GPSAccKalmanPredict(GPSAccKalmanFilter_t *k, double timeNow, double xAcc, double yAcc);
void GPSAccKalmanUpdate(GPSAccKalmanFilter_t *k, double timeStamp, double x, double y, double xVel, double yVel, double posDev);

double GPSAccKalmanGetX(const GPSAccKalmanFilter_t *k);
double GPSAccKalmanGetY(const GPSAccKalmanFilter_t *k);
double GPSAccKalmanGetXVel(const GPSAccKalmanFilter_t *k);
double GPSAccKalmanGetYVel(const GPSAccKalmanFilter_t *k);

#endif // GPSACCKALMAN_H
