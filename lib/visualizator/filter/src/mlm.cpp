#include "mlm.h"

#include "commons.h"

MLM::MLM(void) : m_got_start_point(false) {}
//////////////////////////////////////////////////////////////

MLM::MLM(double acc_sigma_2, double loc_sigma_2, double vel_sigma_2)
    : m_got_start_point(false),
      m_acc_sigma_2(acc_sigma_2),
      m_loc_sigma_2(loc_sigma_2),
      m_vel_sigma_2(vel_sigma_2)
{
}
//////////////////////////////////////////////////////////////

MLM::~MLM(void) {}
//////////////////////////////////////////////////////////////

bool MLM::process_acc_data(const enu_accelerometer &acc, double time_sec)
{
  if (!m_got_start_point) {
    return false;  // do nothing until first GPS coordinate is processed
  }
  m_fk.predict(acc.x, acc.y, time_sec);
  return true;
}
//////////////////////////////////////////////////////////////

void MLM::process_gps_data(const gps_coordinate &gps, double time_sec)
{
  double x, y, z;
  double az_rad = degree_to_rad(gps.speed.azimuth);
  az_rad = azimuth_to_cartezian_rad(az_rad);
  double vel_x = gps.speed.value * cos(az_rad);
  double vel_y = gps.speed.value * sin(az_rad);

  if (!m_got_start_point) {
    m_got_start_point = true;
    m_lc.Reset(gps.location.latitude, gps.location.longitude, 0.0);
    m_lc.Forward(gps.location.latitude, gps.location.longitude, 0.0, x, y, z);
    m_fk.reset(x, y, time_sec, vel_x, vel_y, m_acc_sigma_2, m_loc_sigma_2);
    return;
  }

  m_lc.Forward(gps.location.latitude, gps.location.longitude, 0.0, x, y, z);
  FusionFilterState st(x, y, vel_x, vel_y);
  m_fk.update(st, gps.location.error, gps.speed.error);
  // this one used during tests in visualizator
  // m_fk.update(st, m_loc_sigma_2, m_vel_sigma_2);
}
//////////////////////////////////////////////////////////////

gps_coordinate MLM::predicted_coordinate() const
{
  gps_coordinate res;
  double x, y, z;
  x = m_fk.current_state().x;
  y = m_fk.current_state().y;
  z = 0.;
  m_lc.Reverse(x,
               y,
               z,
               res.location.latitude,
               res.location.longitude,
               res.location.altitude);

  double vx = m_fk.current_state().x_vel;
  double vy = m_fk.current_state().y_vel;
  res.speed.azimuth =
      rad_to_degree(atan2(vy, vx));  // todo convert into azimuth
  res.speed.value = std::sqrt(vx * vx + vy * vy);
  return res;
}
//////////////////////////////////////////////////////////////
