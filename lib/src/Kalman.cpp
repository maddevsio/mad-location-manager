//#include <stdlib.h>
//#include "Kalman.hpp"
//#include "Matrix.hpp"

//kalman_filter_t *
//kf_create(uint32_t state_dim,
//          uint32_t measure_dim,
//          uint32_t control_dim)
//{
//  /*
//      rebuildF 44
//      rebuildU 21
//      rebuildB 42
//      rebuildR 44
//      rebuildQ 44 */

//  kalman_filter_t *f = (kalman_filter_t*) malloc(sizeof(kalman_filter_t));
//  f->F = matrix_alloc(state_dim, state_dim);
//  f->H = matrix_alloc(measure_dim, state_dim);
//  f->B = matrix_alloc(state_dim, control_dim);
//  f->Q = matrix_alloc(state_dim, state_dim);
//  f->R = matrix_alloc(measure_dim, measure_dim);

//  f->Uk = matrix_alloc(control_dim, 1);
//  f->Zk = matrix_alloc(measure_dim, 1);
//  f->Xk_km1 = matrix_alloc(state_dim, 1);
//  f->Pk_km1 = matrix_alloc(state_dim, state_dim);

//  f->Yk = matrix_alloc(measure_dim, 1);
//  f->Sk = matrix_alloc(measure_dim, measure_dim);
//  f->SkInv = matrix_alloc(measure_dim, measure_dim);

//  f->K = matrix_alloc(state_dim, measure_dim);
//  f->Xk_k = matrix_alloc(state_dim, 1);
//  f->Pk_k = matrix_alloc(state_dim, state_dim);
//  f->Yk_k = matrix_alloc(measure_dim, 1);

//  f->auxBxU = matrix_alloc(state_dim, 1);
//  f->auxSDxSD = matrix_alloc(state_dim, state_dim);
//  f->auxSDxMD = matrix_alloc(state_dim, measure_dim);
//  return f;
//}
////////////////////////////////////////////////////////////////////////////

//void
//kf_free(kalman_filter_t *k) {
//  matrix_free(k->F);
//  matrix_free(k->B);
//  matrix_free(k->H);
//  matrix_free(k->Q);
//  matrix_free(k->R);

//  matrix_free(k->Uk);
//  matrix_free(k->Zk);
//  matrix_free(k->Xk_km1);
//  matrix_free(k->Pk_km1);

//  matrix_free(k->Yk);
//  matrix_free(k->Sk);
//  matrix_free(k->SkInv);

//  matrix_free(k->K);
//  matrix_free(k->Xk_k);
//  matrix_free(k->Pk_k);
//  matrix_free(k->Yk_k);

//  matrix_free(k->auxBxU);
//  matrix_free(k->auxSDxSD);
//  matrix_free(k->auxSDxMD);
//  free(k);
//}
////////////////////////////////////////////////////////////////////////////

//void
//kf_predict(kalman_filter_t *k) {
//  //Xk|k-1 = Fk*Xk-1|k-1 + Bk*Uk
//  matrix_multiply(k->F, k->Xk_k, k->Xk_km1);
//  matrix_multiply(k->B, k->Uk, k->auxBxU);
//  matrix_add(k->Xk_km1, k->auxBxU, k->Xk_km1);

//  //Pk|k-1 = Fk*Pk-1|k-1*Fk(t) + Qk
//  matrix_multiply(k->F, k->Pk_k, k->auxSDxSD);
//  matrix_multiply_by_transpose(k->auxSDxSD, k->F, k->Pk_km1);
//  matrix_add(k->Pk_km1, k->Q, k->Pk_km1);
//}
////////////////////////////////////////////////////////////////////////////

//kalman_filter_error_t
//kf_update(kalman_filter_t *k) {
//  //Yk = Zk - Hk*Xk|k-1
//  matrix_multiply(k->H, k->Xk_km1, k->Yk);
//  matrix_subtract(k->Zk, k->Yk, k->Yk);

//  //Sk = Rk + Hk*Pk|k-1*Hk(t)
//  matrix_multiply_by_transpose(k->Pk_km1, k->H, k->auxSDxMD);
//  matrix_multiply(k->H, k->auxSDxMD, k->Sk);
//  matrix_add(k->R, k->Sk, k->Sk);

//  //Kk = Pk|k-1*Hk(t)*Sk(inv)
//  if (!(matrix_destructive_invert(k->Sk, k->SkInv))) //we can use distructive invert here, because Sk calculated above :)
//    return KFE_MATRIX_INV; //matrix hasn't inversion
//  matrix_multiply(k->auxSDxMD, k->SkInv, k->K);

//  //xk|k = xk|k-1 + Kk*Yk
//  matrix_multiply(k->K, k->Yk, k->Xk_k);
//  matrix_add(k->Xk_km1, k->Xk_k, k->Xk_k);

//  //Pk|k = (I - Kk*Hk) * Pk|k-1 - SEE WIKI!!!
//  matrix_multiply(k->K, k->H, k->auxSDxSD);
//  matrix_subtract_from_identity(k->auxSDxSD);
//  matrix_multiply(k->auxSDxSD, k->Pk_km1, k->Pk_k);

//  //we don't use this, but it's presented in paper. so!
//  //Yk|k = Zk - Hk*Xk|k
//  matrix_multiply(k->H, k->Xk_k, k->Yk_k);
//  matrix_subtract(k->Zk, k->Yk_k, k->Yk_k);
//  return KFE_SUCCESS;
//}
////////////////////////////////////////////////////////////////////////////

//const char *
//kf_error_str(kalman_filter_error_t err) {
//  //see kalman_filter_error_t enum
//  static const char * errors[] = {
//    "Success",
//    "Matrix hasn't inversion",
//    "Unknown",
//  };
//  return errors[err];
//}
