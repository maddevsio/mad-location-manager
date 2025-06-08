#include "gps_acc_fusion_filter.h"

GPSAccFusionFilter::GPSAccFusionFilter()
{
  H.setIdentity();
  Pk_k.setIdentity();
}
//////////////////////////////////////////////////////////////

const FusionFilterState GPSAccFusionFilter::current_state() const
{
  FusionFilterState res(Xk_k(0, 0), Xk_k(1, 0), Xk_k(2, 0), Xk_k(3, 0));
  return res;
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::reset(double x,
                               double y,
                               double ts,
                               double x_vel,
                               double y_vel,
                               double acc_sigma_2,
                               double pos_sigma_2)
{
  m_last_predict_sec = ts;
  Xk_k << x, y, x_vel, y_vel;
  Pk_k.setZero();
  Pk_k.diagonal() << 1, 1, 10, 10;
  m_acc_sigma_2 = acc_sigma_2;
  m_pos_sigma_2 = pos_sigma_2;
};
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::predict(double x_acc, double y_acc, double ts_sec)
{
  double dt_sec = ts_sec - m_last_predict_sec;
  rebuild_F(dt_sec);
  rebuild_B(dt_sec);
  rebuild_U(x_acc, y_acc);
  rebuild_Q(dt_sec);
  estimate();

  m_last_predict_sec = ts_sec;

  // we do many predict steps and THEN update.
  // to keep Xk and Pk actual and accumulate error in Pk - assign them at this
  // step.
  Xk_k = Xk_km1;
  Pk_k = Pk_km1;
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::update(const FusionFilterState& state,
                                double pos_sigma_2,
                                double vel_sigma_2)
{
  rebuild_R(pos_sigma_2, vel_sigma_2);

  // if we have gps speed as input
  Zk << state.x, state.y, state.x_vel, state.y_vel;

  // if no gps speed
  // Zk << state.x, state.y;
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
  double dt = dt_sec;
  double dt_2 = 0.5 * dt * dt;
  // clang-format off
  B << dt_2,    0.,
       0.,      dt_2,
       dt,      0.,
       0.,      dt;
  // clang-format on
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_Q(double dt_sec)
{
  // The correct continuous-white-noise model (Brown & Hwang, Bar-Shalom, etc.)
  double dt = dt_sec;
  const double dt2 = dt * dt;
  const double dt3 = dt2 * dt;
  const double dt4 = dt3 * dt;

  // clang-format off
  Q <<  dt4/4.0,   0,        dt3/2.0,   0,
        0,         dt4/4.0,  0,         dt3/2.0,
        dt3/2.0,   0,        dt2,       0,
        0,         dt3/2.0,  0,         dt2;
  // clang-format on

  Q *= m_acc_sigma_2;  // σa² already
}
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::rebuild_R(double pos_sigma_2, double vel_sigma_2)
{
  R.setZero();

  // if we have gps speed as input
  R.diagonal() << pos_sigma_2, pos_sigma_2, vel_sigma_2, vel_sigma_2;

  // if no gps speed
  // R.diagonal() << pos_sigma_2, pos_sigma_2;
}
//////////////////////////////////////////////////////////////
