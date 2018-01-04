#ifndef GPSACCKALMAN2_H
#define GPSACCKALMAN2_H

#include <stdint.h>
#include "Kalman.h"
#include "Matrix.h"

typedef struct GPSAccKalmanFilter2 {
  double timeStamp;
  KalmanFilter_t *kf;
} GPSAccKalmanFilter2_t;

GPSAccKalmanFilter2_t* GPSAccKalman2Alloc(double x, double y,
    double xVel, double yVel,
    double accDev, double posDev,
    double timeStamp);

void GPSAccKalman2Free(GPSAccKalmanFilter2_t *k);

void GPSAccKalman2Predict(GPSAccKalmanFilter2_t *k, double timeNow, double xAcc, double yAcc);
void GPSAccKalman2Update(GPSAccKalmanFilter2_t *k, double x, double y, double xVel, double yVel, double posDev, double velDev);


#endif // GPSACCKALMAN2_H
