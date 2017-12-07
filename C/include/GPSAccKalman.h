#ifndef GPSACCKALMAN_H
#define GPSACCKALMAN_H

#include <stdint.h>
#include "Kalman.h"
#include "Matrix.h"

typedef struct GPSAccKalmanFilter {
  double timeStamp;
  KalmanFilter_t *kf;
} GPSAccKalmanFilter_t;

GPSAccKalmanFilter_t* GPSAccKalmanAlloc(
    double initPos,
    double initVel,
    double positionDeviation,
    double accelerometerDeviation,
    double currentTimeStamp);

void GPSAccKalmanFree(GPSAccKalmanFilter_t *k);

void GPSAccKalmanPredict(GPSAccKalmanFilter_t *k, double timeNow, double accelerationProection);
void GPSAccKalmanUpdate(GPSAccKalmanFilter_t *k, double position, double velocityAxis,
                  double *positionError, double velocityError);

#endif // GPSACCKALMAN_H
