#ifndef KALMAN_H
#define KALMAN_H

#include <stdint.h>
#include "Matrix.h"

typedef struct KalmanGpsAccFilter {
  /* k */
  double timeStamp;

  /*these matrices should be provided by user*/
  /*
  [1, dt
   0, 1  ]*/
  matrix_t *stateTransitionMatrix; //Fk
  /*[ 1, 0
      0, 1 ]*/
  matrix_t *measurementModel; //Hk --> converts somehow measured data to model data. in our case Identity
  /*
    [0.5 * dt^2 ,
     dt ]
  */
  matrix_t *controlMatrix; //Bk
  /*
    [sigma^2, 0
     0      , sigma^2]
  */
  matrix_t *processVariance; //Q
  /*
    [sigma^2, 0
     0      , sigma^2]
  */
  matrix_t *measureVariance; //R

  /*these matrices will be updated by user*/
  /* [a] from accelerometer*/
  matrix_t *controlVector; //Uk (Accelerometer)
  /* GPS
   [ pos,
     vel ]
  */
  matrix_t *actualMeasurement; //Zk (GPS)

  /*
   [ pos,
     vel ]
     pos = pos0 + v*dt + a*dt^2*0.5
     vel = v0 + a*dt
  */
  matrix_t *predictedState; //Xk|k-1
  matrix_t *predictedCovariance; //Pk|k-1
  matrix_t *measurementInnovation; //Yk

  matrix_t *measurementInnovationCovariance; //Sk
  matrix_t *measurementInnovationCovarianceInverse; //Sk(-1)

  matrix_t *optimalKalmanGain; //Kk
  matrix_t *currentState; //Xk|k
  matrix_t *updatedCovariance; //Pk|k
  matrix_t *measurementPostfitResidual; //Yk|k

  /*auxiliary matrices*/
  matrix_t *auxMx1;
  matrix_t *auxMxM;

} KalmanGpsAccFilter_t;

KalmanGpsAccFilter_t* KalmanAlloc(double initPos, double initVel,
                                  double positionDeviation, double accelerometerDeviation,
                                  double currentTimeStamp);
void KalmanFree(KalmanGpsAccFilter_t *k);

void KalmanPredict(KalmanGpsAccFilter_t *k, double timeNow, double accelerationProection);
void KalmanUpdate(KalmanGpsAccFilter_t *k, double position, double velocityAxis,
                  double *positionError, double velocityError);

#endif // KALMAN_H
