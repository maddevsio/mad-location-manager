#include "gps_acc_fusion_filter.h"
#include <assert.h>
#include <stdlib.h>

GPSAccFusionFilter::GPSAccFusionFilter(const FusionFilterState &init_state,
                                       double acc_deviation,
                                       double pos_deviation,
                                       double last_predict_ms)
    : m_last_predict_ms(last_predict_ms), m_acc_deviation(acc_deviation),
      m_current_state(init_state), m_predicts_count(0) {
  Xk_k.set({init_state.x, init_state.y, init_state.x_vel, init_state.y_vel});

  H = Matrix<double, measure_dim, state_dim>::Identity();
  Pk_k = Matrix<double, state_dim, state_dim>::Identity();
  Pk_k *= pos_deviation;
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::predict(double xAcc, double yAcc, double time_ms) {

  double dt_sec = (time_ms - m_last_predict_ms) /
                  1.0e+3; // ms to sec. cause we use m/sec, m/sec^2 etc.
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
  Zk.set({state.x, state.y, state.x_vel, state.y_vel});
  correct();
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_F(double dt_ms) {
  F.set({1.0, 0.0, dt_ms, 0.0, 0.0, 1.0, 0.0, dt_ms, 0.0, 0.0, 1.0, 0.0, 0.0,
         0.0, 0.0, 1.0});
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_U(double xAcc, double yAcc) {
  Uk.set({xAcc, yAcc});
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_B(double dt_ms) {
  double dt_2 = 0.5 * dt_ms * dt_ms;
  B.set({dt_2, 0.0, 0.0, dt_2, dt_ms, 0.0, 0.0, dt_ms});
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_Q(double acc_deviation) {
  // TODO RE-IMPLEMENT and find good velocity and position deviations.

  double vel_dev = acc_deviation * m_predicts_count;
  double pos_dev = vel_dev * m_predicts_count;
  double cov_dev = vel_dev * pos_dev;

  double pos_dev_2 = pos_dev * pos_dev;
  double vel_dev_2 = vel_dev * vel_dev;

  Q.set({
      pos_dev_2,
      0.0,
      cov_dev,
      0.0,
      0.0,
      pos_dev_2,
      0.0,
      cov_dev,
      cov_dev,
      0.0,
      vel_dev_2,
      0.0,
      0.0,
      cov_dev,
      0.0,
      vel_dev_2,
  });
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_R(double pos_sigma, double vel_sigma) {
  // TODO FIND HOW MANY TIMES GPS VELOCITY
  // ACCURACY BETTER THEN GPS COORDINATES ACCURACY
  // NOW it's assumed that velocity sigma 10 times worse
  // than position sigma
  vel_sigma = pos_sigma * 1.0e-01;
  R.set({pos_sigma, 0.0, 0.0, 0.0, 0.0, pos_sigma, 0.0, 0.0, 0.0, 0.0,
         vel_sigma, 0.0, 0.0, 0.0, 0.0, vel_sigma});
}
//////////////////////////////////////////////////////////////
