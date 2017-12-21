#ifndef GPSACCKALMAN2_H
#define GPSACCKALMAN2_H

#include "Kalman.h"

typedef struct GPSAccKalmanFilter2 {
  double timeStamp;
  KalmanFilter_t *kf;
} GPSAccKalmanFilter2_t;

GPSAccKalmanFilter2_t* GPSAccKalman2Alloc(double x, double y,
                                          double xvel, double yvel,
                                          double xacc, double yacc,
                                          double timeStamp);

void GPSAccKalman2Free(GPSAccKalmanFilter2_t *k);



#endif // GPSACCKALMAN2_H
