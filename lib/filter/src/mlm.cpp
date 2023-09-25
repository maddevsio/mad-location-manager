#include "mlm.h"

#include <iostream>

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
  double speed_x = gps.speed.value * cos(az_rad);
  double speed_y = gps.speed.value * sin(az_rad);

  if (!m_got_start_point) {
    // 1e-3g for smarphones
    // TODO FIND BEST VALUE HERE
    // and move to fields :)
    const double accelerometer_deviation = 1e-3;
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
  m_lc.Reverse(m_fk.current_state().x,
               m_fk.current_state().y,
               0.,
               res.location.latitude,
               res.location.longitude,
               res.location.altitude);

  return res;
}
//////////////////////////////////////////////////////////////
