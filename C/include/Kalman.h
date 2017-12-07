#ifndef KALMAN_H
#define KALMAN_H

typedef struct matrix matrix_t;

typedef struct KalmanFilter {
  /*these matrices should be provided by user*/
  matrix_t *stateTransitionMatrix; //Fk
  matrix_t *measurementModel; //Hk --> converts somehow measured data to model data. in our case Identity
  matrix_t *controlMatrix; //Bk
  matrix_t *processVariance; //Q
  matrix_t *measureVariance; //R

  /*these matrices will be updated by user*/
  matrix_t *controlVector; //Uk
  matrix_t *actualMeasurement; //Zk
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
  matrix_t *auxBxU;
  matrix_t *auxSDxMD;

} KalmanFilter_t;

KalmanFilter_t *KalmanFilterCreate(int stateDimension,
                                   int measureDimension,
                                   int controlDimension);
void KalmanFilterFree(KalmanFilter_t *k);

void KalmanFilterPredict(KalmanFilter_t *k);
void KalmanFilterUpdate(KalmanFilter_t *k);

#endif // KALMAN_H
