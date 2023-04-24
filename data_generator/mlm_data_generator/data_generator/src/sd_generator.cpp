#include "sd_generator.h"
#include "coordinate.h"
#include "sensor_data.h"
#include <assert.h>
#include <iostream>

gps_coordinate sd_gps_coordinate(const gps_coordinate &start,
                                 const movement_interval &interval, double t) {
  // todo move this vptr somewhere %)
  static coordinates_vptr vptr = coord_vptr_hq();
  assert(t <= interval.duration);

  double iaz_rad = interval.azimuth * M_PI / 180.0;
  double ax = interval.acceleration * cos(iaz_rad);
  double ay = interval.acceleration * sin(iaz_rad);

  double saz_rad = start.speed.azimuth * M_PI / 180.0;
  double v0_x = start.speed.value * cos(saz_rad);
  double v0_y = start.speed.value * sin(saz_rad);

  double sx = v0_x * t + ax * t * t / 2.0; // sx = v0x*t + a*t^2/2
  double sy = v0_y * t + ay * t * t / 2.0; // sy = v0y*t + a*t^2/2

  double s = sqrt(sx * sx + sy * sy);
  gps_coordinate res;
  res.location = vptr.point_ahead(start.location, s, interval.azimuth);

  double vx = v0_x + ax * t;
  double vy = v0_y + ay * t;

  double v = sqrt(vx * vx + vy * vy);
  double az_rad = atan2(vy, vx);
  double az_degrees = az_rad * M_PI / 180.0;
  res.speed = gps_speed(az_degrees, v, 1.0);
  return res;
}
//////////////////////////////////////////////////////////////
