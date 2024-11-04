#include "mlm.h"

#include "commons.h"

MLM::MLM(void) : m_got_start_point(false) {}
//////////////////////////////////////////////////////////////

MLM::~MLM(void) {}
//////////////////////////////////////////////////////////////

void MLM::process_acc_data(const abs_accelerometer &acc, double time_sec)
{
  if (!m_got_start_point)
    return;  // do nothing until first GPS coordinate
  m_fk.predict(acc.x, acc.y, time_sec);
}
//////////////////////////////////////////////////////////////

void MLM::process_gps_data(const gps_coordinate &gps,
                           double pos_deviation,
                           double vel_deviation)
{
  double x, y, z;
  double az_rad = degree_to_rad(gps.speed.azimuth);
  az_rad = azimuth_to_cartezian_rad(az_rad);
  double speed_x = gps.speed.value * cos(az_rad);
  double speed_y = gps.speed.value * sin(az_rad);

  if (!m_got_start_point) {
    // 1e-3g for smarphones
    // TODO FIND BEST VALUE HERE
    // and move to fields :)
    const double accelerometer_deviation = 1e-6;
    m_got_start_point = true;
    m_lc.Reset(gps.location.latitude, gps.location.longitude, 0.0);
    m_lc.Forward(gps.location.latitude, gps.location.longitude, 0.0, x, y, z);
    m_fk.reset(x, y, speed_x, speed_y, accelerometer_deviation, pos_deviation);
    return;
  }

  m_lc.Forward(gps.location.latitude, gps.location.longitude, 0.0, x, y, z);
  FusionFilterState st(x, y, speed_x, speed_y);
  m_fk.update(st, pos_deviation, vel_deviation);
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
