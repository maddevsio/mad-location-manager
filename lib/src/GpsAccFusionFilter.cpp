//#include <assert.h>
//#include <stdlib.h>

//#include "GpsAccFusionFilter.hpp"
//#include "Matrix.hpp"
//#include "Kalman.hpp"

//static void rebuildF(gps_accelerometer_fusion_filter_t *f,
//                     double dt);
//static void rebuildU(gps_accelerometer_fusion_filter_t *f,
//                     double xAcc,
//                     double yAcc);
//static void rebuildB(gps_accelerometer_fusion_filter_t *f,
//                     double dt);
//static void rebuildQ(gps_accelerometer_fusion_filter_t *f,
//                     double acc_dev);

//static void rebuildR_2D(gps_accelerometer_fusion_filter_t *f,
//                        double pos_sigma);
//static void rebuildR_4D(gps_accelerometer_fusion_filter_t *f,
//                        double pos_sigma);

//gps_accelerometer_fusion_filter_t *
//gps_accelerometer_fusion_filter_alloc(gaff_state_t initial_state,
//                                      bool use_gps_speed,
//                                      double acc_dev,
//                                      double pos_dev,
//                                      timestamp_t time_stamp_ms) {
//  // state = [x, y, x', y'] - coordinate X,Y , velocity X',Y'
//#define state_dim 4
//  // control = [x'', y''] - acceleration
//#define control_dim 2
//  // measure = [x, y] - coordinate X,Y from GPS receiver
//#define measure_dim_no_gps_speed 2
//  // measure = [x, y, x', y'] - coordinate X,Y + speed from GPS receiver
//#define measure_dim_gps_speed 4

//  uint32_t measure_dim;
//  gps_accelerometer_fusion_filter_t *f = (gps_accelerometer_fusion_filter_t*)
//      malloc(sizeof(gps_accelerometer_fusion_filter_t));

//  if (!f) {
//    //todo log
//    return f;
//  }

//  measure_dim = measure_dim_no_gps_speed;
//  f->rebuildR = rebuildR_2D;
//  if (use_gps_speed) {
//    measure_dim = measure_dim_gps_speed;
//    f->rebuildR = rebuildR_4D;
//  }

//  f->kf = kf_create(state_dim, measure_dim, control_dim);
//  f->last_predict_ms = time_stamp_ms;
//  f->predicts_count = 0;
//  f->acc_deviation = acc_dev;

//  matrix_set(f->kf->Xk_k,
//             initial_state.x,
//             initial_state.y,
//             initial_state.x_vel,
//             initial_state.y_vel);

//  // for 2d H
//  // [ 1.0 0.0 0.0 0.0
//  //   0.0 1.0 0.0 0.0 ]
//  // for 4d H it's identity matrix
//  matrix_set_identity_diag(f->kf->H);
//  matrix_set_identity(f->kf->Pk_k);
//  matrix_scale(f->kf->Pk_k, pos_dev);

//  return f;

//#undef state_dim
//#undef control_dim
//#undef measure_dim_no_gps_speed
//#undef measure_dim_gps_speed
//}
////////////////////////////////////////////////////////////////////////////

//void
//gps_accelerometer_fusion_filter_free(gps_accelerometer_fusion_filter_t *f) {
//  if (f) {
//    kf_free(f->kf);
//  }
//  free(f);
//}
////////////////////////////////////////////////////////////////////////////

//void
//rebuildF(gps_accelerometer_fusion_filter_t *f,
//         double dt) {
//  matrix_set(f->kf->F,
//             1.0, 0.0, dt,  0.0,
//             0.0, 1.0, 0.0, dt,
//             0.0, 0.0, 1.0, 0.0,
//             0.0, 0.0, 0.0, 1.0);
//}
////////////////////////////////////////////////////////////////////////////

//void
//rebuildU(gps_accelerometer_fusion_filter_t *f,
//         double xAcc,
//         double yAcc) {
//  matrix_set(f->kf->Uk,
//             xAcc,
//             yAcc);
//}
////////////////////////////////////////////////////////////////////////////

//void
//rebuildB(gps_accelerometer_fusion_filter_t *f,
//         double dt) {
//  double dt2 = 0.5*dt*dt; //dt^2 / 2
//  matrix_set(f->kf->B,
//             dt2, 0.0,
//             0.0, dt2,
//             dt, 0.0,
//             0.0, dt);
//}
////////////////////////////////////////////////////////////////////////////

//void
//rebuildR_4D(gps_accelerometer_fusion_filter_t *f,
//            double pos_sigma) {
//  //WARNING! We don't know how buch better velocity measurement. we assume, that 10 times better.
//  double velSigma = pos_sigma * 1.0e-01;
//  matrix_set(f->kf->R,
//             pos_sigma, 0.0, 0.0, 0.0,
//             0.0, pos_sigma, 0.0, 0.0,
//             0.0, 0.0, velSigma, 0.0,
//             0.0, 0.0, 0.0, velSigma);
//}
////////////////////////////////////////////////////////////////////////////

//void
//rebuildR_2D(gps_accelerometer_fusion_filter_t *f,
//            double pos_sigma) {
//  matrix_set(f->kf->R,
//             pos_sigma, 0.0,
//             0.0, pos_sigma);
//}
////////////////////////////////////////////////////////////////////////////

//void
//rebuildQ(gps_accelerometer_fusion_filter_t *f,
//         double acc_dev) {
//  // todo WARNING! This is not good approach. Review and make velocity deviation some constant maybe.
//  double velDev = acc_dev * f->predicts_count;
//  double posDev = velDev * f->predicts_count / 2;
//  double covDev = velDev*posDev;
//  matrix_set(f->kf->Q,
//             posDev*posDev, 0.0,           covDev,        0.0,
//             0.0,           posDev*posDev, 0.0,           covDev,
//             covDev,        0.0,           velDev*velDev, 0.0,
//             0.0,           covDev,        0.0,           velDev*velDev);
//}

//void
//gps_accelerometer_fusion_filter_predict(gps_accelerometer_fusion_filter_t *filter,
//                                        timestamp_t time_now_ms,
//                                        double xAcc,
//                                        double yAcc) {
//  double dt_sec = (time_now_ms - filter->last_predict_ms) / 1.0e+3; //ms to sec. cause we use m/sec, m/sec^2 etc.
//  rebuildF(filter, dt_sec);
//  rebuildB(filter, dt_sec);
//  rebuildU(filter, xAcc, yAcc);

//  ++filter->predicts_count;
//  rebuildQ(filter, filter->acc_deviation);

//  filter->last_predict_ms = time_now_ms;
//  kf_predict(filter->kf);
//  matrix_copy(filter->kf->Xk_km1, filter->kf->Xk_k);
//}
////////////////////////////////////////////////////////////////////////////

//void
//gps_accelerometer_fusion_filter_update(gps_accelerometer_fusion_filter_t *filter,
//                                       gaff_state_t state,
//                                       double pos_dev) {
//  //  double dt = timeNow - k->updateTime;
//  filter->predicts_count = 0;
//  filter->rebuildR(filter, pos_dev);
//  matrix_set(filter->kf->Zk,
//             state.x,
//             state.y,
//             state.x_vel,
//             state.y_vel);
//  kf_update(filter->kf);
//}
////////////////////////////////////////////////////////////////////////////

//gaff_state_t
//gps_accelerometer_fusion_filter_current_state(const gps_accelerometer_fusion_filter_t *filter) {
//  gaff_state_t res = {
//    filter->kf->Xk_k->data[0][0], //X
//    filter->kf->Xk_k->data[1][0], //Y
//    filter->kf->Xk_k->data[2][0], //x_vel
//    filter->kf->Xk_k->data[3][0], //y_vel
//  };
//  return res;
//}
