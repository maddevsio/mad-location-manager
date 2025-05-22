#include "gps_acc_fusion_filter.h"

GPSAccFusionFilter::GPSAccFusionFilter()
{
  H = Eigen::Matrix<double, _measure_dim, _state_dim>::Identity();
  Pk_k = Eigen::Matrix<double, _state_dim, _state_dim>::Identity();
}
//////////////////////////////////////////////////////////////

const FusionFilterState GPSAccFusionFilter::current_state() const
{
  FusionFilterState res(Xk_k(0, 0), Xk_k(1, 0), Xk_k(2, 0), Xk_k(3, 0));
  return res;
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::reset(double x,  // longitude in meters
                               double y,  // latitude in meters
                               double ts,
                               double x_vel,
                               double y_vel,
                               double acc_deviation,
                               double pos_deviation)
{
  m_last_predict_sec = ts;
  Xk_k << x, y, x_vel, y_vel;
  Pk_k = Eigen::Matrix<double, _state_dim, _state_dim>::Identity();
  Pk_k *= pos_deviation;
  m_acc_sigma_2 = acc_deviation;
};
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::predict(double xAcc, double yAcc, double time_sec)
{
  double dt_sec = time_sec - m_last_predict_sec;
  // probably unnecessary
  if (fabs(dt_sec) < 1e-9) {
    return;  // do nothing
  }

  rebuild_F(dt_sec);
  rebuild_B(dt_sec);
  rebuild_U(xAcc, yAcc);

  rebuild_Q(dt_sec);
  estimate();

  m_last_predict_sec = time_sec;

  // this copy is not necessary. it's supposed
  // to provide current state on each step
  // will be updated during correct() step
  Xk_k = Xk_km1;
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::update(const FusionFilterState& state,
                                double pos_deviation,
                                double vel_deviation)
{
  rebuild_R(pos_deviation, vel_deviation);
  Zk << state.x, state.y;  //, state.x_vel, state.y_vel;
  correct();
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_F(double dt_sec)
{
  double dt = dt_sec;
  // clang-format off
  F << 1.,	0.,  dt, 0.,
       0.,	1.,  0., dt,
       0.,	0.,  1., 0.,
       0.,	0.,  0., 1.;
  // clang-format on
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_U(double xAcc, double yAcc)
{
  Uk << xAcc, yAcc;
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_B(double dt_sec)
{
  double dt_2 = 0.5 * dt_sec * dt_sec;
  // clang-format off
  B << dt_2,    0.,
       0.,      dt_2,
       dt_sec,  0.,
       0.,      dt_sec;
  // clang-format on
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_Q(double dt_sec)
{
  // The correct continuous-white-noise model (Brown & Hwang, Bar-Shalom, etc.) gives
  // Q = σa² · [ dt³/3   dt²/2
  //             dt²/2   dt   ]
  double dt = dt_sec;
  const double dt2 = dt * dt;
  const double dt3 = dt2 * dt;

  // clang-format off
  Q <<  dt3 / 3.,   0.,       dt2 / 2,    0, 
        0.,         dt3 / 3,  0,          dt2 / 2, 
        dt2 / 2,    0,        dt,         0, 
        0,          dt2 / 2,  0,          dt;
  // clang-format on

  Q *= m_acc_sigma_2;  // σa² already
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_R(double pos_sigma, double vel_sigma)
{
  // clang-format off
  /* vel_sigma = pos_sigma * 1.0e-01; */
  /* R <<  */
  /*   pos_sigma,  0.0,        0.0,        0.0,  */
  /*   0.0,        pos_sigma,  0.0,        0.0,  */
  /*   0.0,        0.0,        vel_sigma,  0.0,  */
  /*   0.0,        0.0,        0.0,        vel_sigma; */

  R << 
    pos_sigma,  0.0,
    0.0,        pos_sigma;
  // clang-format on
}
//////////////////////////////////////////////////////////////
