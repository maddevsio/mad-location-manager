#include "gps_acc_fusion_filter.h"

#include <assert.h>
#include <stdlib.h>

#include <iostream>
#include "commons.h"

std::ostream& operator<<(std::ostream& os, const FusionFilterState& obj)
{
  os << "x: " << obj.x << "; y: " << obj.y << "\n";
  os << "x_vel: " << obj.x_vel;
  os << "; y_vel: " << obj.y_vel << std::endl;
  return os;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////

GPSAccFusionFilter::GPSAccFusionFilter() : m_predicts_count(0)
{
  H = Matrix<double, _measure_dim, _state_dim>::Identity();
  Pk_k = Matrix<double, _state_dim, _state_dim>::Identity();
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
                               double x_vel,
                               double y_vel,
                               double acc_deviation,
                               double pos_deviation)
{
  Xk_k << x, y, x_vel, y_vel;
  Pk_k = Matrix<double, _state_dim, _state_dim>::Identity();
  Pk_k *= pos_deviation;
  m_acc_deviation = acc_deviation;
};
//////////////////////////////////////////////////////////////

void GPSAccFusionFilter::predict(double xAcc, double yAcc, double time_sec)
{
  // ms to sec. cause we use m/sec, m/sec^2 etc.
  double dt_sec = time_sec - m_last_predict_sec;

  rebuild_F(dt_sec);
  rebuild_B(dt_sec);
  rebuild_U(xAcc, yAcc);

  ++m_predicts_count;
  rebuild_Q(m_acc_deviation);

  m_last_predict_sec = time_sec;
  estimate();

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
  m_predicts_count = 0;
  Zk << state.x, state.y, state.x_vel, state.y_vel;
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

void GPSAccFusionFilter::rebuild_Q(double acc_deviation)
{
  double vel_dev = acc_deviation * m_predicts_count;
  double pos_dev = vel_dev * m_predicts_count;  // todo check!
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

void GPSAccFusionFilter::rebuild_R(double pos_sigma, double vel_sigma)
{
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
