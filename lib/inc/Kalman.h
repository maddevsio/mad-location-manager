#ifndef KALMAN_H
#define KALMAN_H

typedef struct matrix matrix_t;

typedef struct KalmanFilter {
  /*these matrices should be provided by user*/
  matrix_t *F; //state transition model
  matrix_t *H; //observation model
  matrix_t *B; //control matrix
  matrix_t *Q; //process noise covariance
  matrix_t *R; //observation noise covariance

  /*these matrices will be updated by user*/
  matrix_t *Uk; //control vector
  matrix_t *Zk; //actual values (measured)
  matrix_t *Xk_km1; //predicted state estimate
  matrix_t *Pk_km1; //predicted estimate covariance
  matrix_t *Yk; //measurement innovation

  matrix_t *Sk; //innovation covariance
  matrix_t *SkInv; //innovation covariance inverse

  matrix_t *K; //Kalman gain (optimal)
  matrix_t *Xk_k; //updated (current) state
  matrix_t *Pk_k; //updated estimate covariance
  matrix_t *Yk_k; //post fit residual

  /*auxiliary matrices*/
  matrix_t *auxBxU;
  matrix_t *auxSDxSD;
  matrix_t *auxSDxMD;

} KalmanFilter_t;

KalmanFilter_t *KalmanFilterCreate(int stateDimension,
                                   int measureDimension,
                                   int controlDimension);
void KalmanFilterFree(KalmanFilter_t *k);

void KalmanFilterPredict(KalmanFilter_t *k);
void KalmanFilterUpdate(KalmanFilter_t *k);

#endif // KALMAN_H
