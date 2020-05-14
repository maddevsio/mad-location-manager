#include <stdlib.h>
#include "Kalman.h"
#include "Matrix.h"

kalman_filter_t *kalman_filter_create(uint32_t stateDimension,
                                      uint32_t measureDimension,
                                      uint32_t controlDimension)
{
  kalman_filter_t *f = (kalman_filter_t*) malloc(sizeof(kalman_filter_t));
  f->F = matrix_alloc(stateDimension, stateDimension);
  f->H = matrix_alloc(measureDimension, stateDimension);
  f->Q = matrix_alloc(stateDimension, stateDimension);
  f->R = matrix_alloc(measureDimension, measureDimension);

  f->B = matrix_alloc(stateDimension, controlDimension);
  f->Uk = matrix_alloc(controlDimension, 1);

  f->Zk = matrix_alloc(measureDimension, 1);

  f->Xk_km1 = matrix_alloc(stateDimension, 1);
  f->Pk_km1 = matrix_alloc(stateDimension, stateDimension);

  f->Yk = matrix_alloc(measureDimension, 1);
  f->Sk = matrix_alloc(measureDimension, measureDimension);
  f->SkInv = matrix_alloc(measureDimension, measureDimension);

  f->K = matrix_alloc(stateDimension, measureDimension);

  f->Xk_k = matrix_alloc(stateDimension, 1);
  f->Pk_k = matrix_alloc(stateDimension, stateDimension);
  f->Yk_k = matrix_alloc(measureDimension, 1);

  f->auxBxU = matrix_alloc(stateDimension, 1);
  f->auxSDxSD = matrix_alloc(stateDimension, stateDimension);
  f->auxSDxMD = matrix_alloc(stateDimension, measureDimension);
  return f;
}
//////////////////////////////////////////////////////////////////////////

void kalman_filter_free(kalman_filter_t *k) {
  matrix_free(k->F);
  matrix_free(k->H);
  matrix_free(k->B);
  matrix_free(k->Q);
  matrix_free(k->R);

  matrix_free(k->Uk);
  matrix_free(k->Zk);

  matrix_free(k->Xk_km1);
  matrix_free(k->Pk_km1);
  matrix_free(k->Yk);

  matrix_free(k->Sk);
  matrix_free(k->SkInv);

  matrix_free(k->K);
  matrix_free(k->Xk_k);
  matrix_free(k->Pk_k);
  matrix_free(k->Yk_k);

  matrix_free(k->auxBxU);
  matrix_free(k->auxSDxSD);
  matrix_free(k->auxSDxMD);
  free(k);
}
//////////////////////////////////////////////////////////////////////////

void kalman_filter_predict(kalman_filter_t *k) {
  //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
  matrix_multiply(k->F, k->Xk_k, k->Xk_km1);
  matrix_multiply(k->B, k->Uk, k->auxBxU);
  matrix_add(k->Xk_km1, k->auxBxU, k->Xk_km1);

  //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
  matrix_multiply(k->F, k->Pk_k, k->auxSDxSD);
  matrix_multiply_by_transpose(k->auxSDxSD, k->F, k->Pk_km1);
  matrix_add(k->Pk_km1, k->Q, k->Pk_km1);
}
//////////////////////////////////////////////////////////////////////////

void kalman_filter_update(kalman_filter_t *k) {
  //Yk = Zk - Hk*Xk|k-1
  matrix_multiply(k->H, k->Xk_km1, k->Yk);
  matrix_subtract(k->Zk, k->Yk, k->Yk);

  //Sk = Rk + Hk*Pk|k-1*Hk(t)
  matrix_multiply_by_transpose(k->Pk_km1, k->H, k->auxSDxMD);
  matrix_multiply(k->H, k->auxSDxMD, k->Sk);
  matrix_add(k->R, k->Sk, k->Sk);

  //Kk = Pk|k-1*Hk(t)*Sk(inv)
  if (!(matrix_destructive_invert(k->Sk, k->SkInv)))
    return; //matrix hasn't inversion
  matrix_multiply(k->auxSDxMD, k->SkInv, k->K);

  //xk|k = xk|k-1 + Kk*Yk
  matrix_multiply(k->K, k->Yk, k->Xk_k);
  matrix_add(k->Xk_km1, k->Xk_k, k->Xk_k);

  //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
  matrix_multiply(k->K, k->H, k->auxSDxSD);
  matrix_subtract_from_identity(k->auxSDxSD);
  matrix_multiply(k->auxSDxSD, k->Pk_km1, k->Pk_k);

  //we don't use this
  //Yk|k = Zk - Hk*Xk|k
  matrix_multiply(k->H, k->Xk_k, k->Yk_k);
  matrix_subtract(k->Zk, k->Yk_k, k->Yk_k);
}
//////////////////////////////////////////////////////////////////////////

