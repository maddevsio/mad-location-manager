#include "Kalman.h"
#include "Matrix.h"

KalmanFilter_t *KalmanFilterCreate(int stateDimension,
                                   int measureDimension,
                                   int controlDimension)
{
  KalmanFilter_t *f = (KalmanFilter_t*) malloc(sizeof(KalmanFilter_t));
  f->F = MatrixAlloc(stateDimension, stateDimension);
  f->H = MatrixAlloc(measureDimension, stateDimension);
  f->Q = MatrixAlloc(stateDimension, stateDimension);
  f->R = MatrixAlloc(measureDimension, measureDimension);

  f->B = MatrixAlloc(stateDimension, controlDimension);
  f->Uk = MatrixAlloc(controlDimension, 1);

  f->Zk = MatrixAlloc(measureDimension, 1);

  f->Xk_km1 = MatrixAlloc(stateDimension, 1);
  f->Pk_km1 = MatrixAlloc(stateDimension, stateDimension);

  f->Yk = MatrixAlloc(measureDimension, 1);
  f->Sk = MatrixAlloc(measureDimension, measureDimension);
  f->SkInv = MatrixAlloc(measureDimension, measureDimension);

  f->K = MatrixAlloc(stateDimension, measureDimension);

  f->Xk_k = MatrixAlloc(stateDimension, 1);
  f->Pk_k = MatrixAlloc(stateDimension, stateDimension);
  f->Yk_k = MatrixAlloc(measureDimension, 1);

  f->auxBxU = MatrixAlloc(stateDimension, 1);
  f->auxSDxSD = MatrixAlloc(stateDimension, stateDimension);
  f->auxSDxMD = MatrixAlloc(stateDimension, measureDimension);
  return f;
}
//////////////////////////////////////////////////////////////////////////

void KalmanFilterFree(KalmanFilter_t *k) {
  MatrixFree(k->F);
  MatrixFree(k->H);
  MatrixFree(k->B);
  MatrixFree(k->Q);
  MatrixFree(k->R);

  MatrixFree(k->Uk);
  MatrixFree(k->Zk);

  MatrixFree(k->Xk_km1);
  MatrixFree(k->Pk_km1);
  MatrixFree(k->Yk);

  MatrixFree(k->Sk);
  MatrixFree(k->SkInv);

  MatrixFree(k->K);
  MatrixFree(k->Xk_k);
  MatrixFree(k->Pk_k);
  MatrixFree(k->Yk_k);

  MatrixFree(k->auxBxU);
  MatrixFree(k->auxSDxSD);
  MatrixFree(k->auxSDxMD);
}
//////////////////////////////////////////////////////////////////////////

void KalmanFilterPredict(KalmanFilter_t *k) {
  //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
  MatrixMultiply(k->F, k->Xk_k, k->Xk_km1);
  MatrixMultiply(k->B, k->Uk, k->auxBxU);
  MatrixAdd(k->Xk_km1, k->auxBxU, k->Xk_km1);

  //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
  MatrixMultiply(k->F, k->Pk_k, k->auxSDxSD);
  MatrixMultiplyByTranspose(k->auxSDxSD, k->F, k->Pk_km1);
  MatrixAdd(k->Pk_km1, k->Q, k->Pk_km1);
}
//////////////////////////////////////////////////////////////////////////

void KalmanFilterUpdate(KalmanFilter_t *k) {
  //Yk = Zk - Hk*Xk|k-1
  MatrixMultiply(k->H, k->Xk_km1, k->Yk);
  MatrixSubtract(k->Zk, k->Yk, k->Yk);

  //Sk = Rk + Hk*Pk|k-1*Hk(t)
  MatrixMultiplyByTranspose(k->Pk_km1, k->H, k->auxSDxMD);
  MatrixMultiply(k->H, k->auxSDxMD, k->Sk);
  MatrixAdd(k->R, k->Sk, k->Sk);

  //Kk = Pk|k-1*Hk(t)*Sk(inv)
  if (!(MatrixDestructiveInvert(k->Sk, k->SkInv)))
    return; //matrix hasn't inversion
  MatrixMultiply(k->auxSDxMD, k->SkInv, k->K);

  //xk|k = xk|k-1 + Kk*Yk
  MatrixMultiply(k->K, k->Yk, k->Xk_k);
  MatrixAdd(k->Xk_km1, k->Xk_k, k->Xk_k);

  //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
  MatrixMultiply(k->K, k->H, k->auxSDxSD);
  MatrixSubtractFromIdentity(k->auxSDxSD);
  MatrixMultiply(k->auxSDxSD, k->Pk_km1, k->Pk_k);

  //we don't use this
  //Yk|k = Zk - Hk*Xk|k
  MatrixMultiply(k->H, k->Xk_k, k->Yk_k);
  MatrixSubtract(k->Zk, k->Yk_k, k->Yk_k);
}
//////////////////////////////////////////////////////////////////////////

