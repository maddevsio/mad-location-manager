#include "gps_acc_fusion_filter.h"
#include <assert.h>
#include <stdlib.h>

GPSAccFusionFilter::GPSAccFusionFilter() {
  H = Matrix<double, _measure_dim, _state_dim>::Identity();
  Pk_k = Matrix<double, _state_dim, _state_dim>::Identity();
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::reset(double x, // longitude in meters
                               double y, // latitude in meters
                               double x_vel, double y_vel, double acc_deviation,
                               double pos_deviation) {
  Xk_k << x, y, x_vel, y_vel;
  Pk_k = Matrix<double, _state_dim, _state_dim>::Identity();
  Pk_k *= pos_deviation;
  m_acc_deviation = acc_deviation;
};
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::predict(double xAcc, double yAcc, double time_ms) {
  // ms to sec. cause we use m/sec, m/sec^2 etc.
  double dt_sec = (time_ms - m_last_predict_ms) / 1.0e+3;

  rebuild_F(dt_sec);
  rebuild_B(dt_sec);
  rebuild_U(xAcc, yAcc);

  ++m_predicts_count;
  rebuild_Q(m_acc_deviation);
  m_last_predict_ms = time_ms;
  estimate();
  Xk_km1 = Xk_k;
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::update(const FusionFilterState &state,
                                double pos_deviation, double vel_deviation) {
  m_predicts_count = 0;
  rebuild_R(pos_deviation, vel_deviation);
  Zk << state.x, state.y, state.x_vel, state.y_vel;
  correct();
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_F(double dt_ms) {
  // clang-format off
  F << 1.,	0.,  dt_ms,  0.,
       0.,	1.,  0.,     dt_ms,
       0.,	0.,  1.,     0.,
       0.,	0.,  0.,     1.;
  // clang-format on
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_U(double xAcc, double yAcc) {
  Uk << xAcc, yAcc;
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_B(double dt_ms) {
  double dt_2 = 0.5 * dt_ms * dt_ms;
  // clang-format off
  B << dt_2,    0.,
       0.,      dt_2,
       dt_ms,   0.,
       0.,      dt_ms;
  // clang-format on
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_Q(double acc_deviation) {
  // TODO RE-IMPLEMENT and find good velocity and position deviations.

  double vel_dev = acc_deviation * m_predicts_count;
  double pos_dev = vel_dev * m_predicts_count; // check!
  double cov_dev = vel_dev * pos_dev;

  double pos_dev_2 = pos_dev * pos_dev;
  double vel_dev_2 = vel_dev * vel_dev;

  // clang-format off
  Q <<  pos_dev_2,  0.0,        cov_dev,    0.0, 
        0.0,        pos_dev_2,  0.0,        cov_dev, 
        cov_dev,    0.0,        vel_dev_2,  0.0, 
        0.0,        cov_dev,    0.0,        vel_dev_2;
  // clang-format on
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_R(double pos_sigma, double vel_sigma) {
  // clang-format off
  /* vel_sigma = pos_sigma * 1.0e-01; */
  R << 
    pos_sigma,  0.0,        0.0,        0.0, 
    0.0,        pos_sigma,  0.0,        0.0, 
    0.0,        0.0,        vel_sigma,  0.0, 
    0.0,        0.0,        0.0,        vel_sigma;
  // clang-format on
}
//////////////////////////////////////////////////////////////
