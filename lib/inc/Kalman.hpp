#ifndef KALMAN_H
#define KALMAN_H

#include <stdint.h>

typedef struct matrix matrix_t;

typedef struct kalman_filter {
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
} kalman_filter_t;

typedef enum kalman_filter_error {
  KFE_SUCCESS = 0,
  KFE_MATRIX_INV,
  KFE_UNKNOWN
} kalman_filter_error_t;

kalman_filter_t *kf_create(uint32_t state_dim,
                           uint32_t measure_dim,
                           uint32_t control_dim);
void kf_free(kalman_filter_t *k);

void kf_predict(kalman_filter_t *k);
kalman_filter_error_t kf_update(kalman_filter_t *k);

const char *kf_error_str(kalman_filter_error_t err);

#endif // KALMAN_H
