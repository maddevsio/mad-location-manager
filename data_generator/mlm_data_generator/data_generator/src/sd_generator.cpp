#include "sd_generator.h"
#include "commons.h"
#include "coordinate.h"
#include "sensor_data.h"
#include <assert.h>
#include <iostream>

gps_coordinate sd_gps_coordinate_in_interval(const gps_coordinate &start,
                                             const movement_interval &interval,
                                             double t) {
  // todo move this vptr somewhere %)
  static coordinates_vptr vptr = coord_vptr_hq();
  assert(t <= interval.duration);

  double iaz_rad = degree_to_rad(interval.azimuth);
  double ax = interval.acceleration * cos(iaz_rad);
  double ay = interval.acceleration * sin(iaz_rad);

  double saz_rad = degree_to_rad(start.speed.azimuth);
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
  double az_degrees = rad_to_degree(az_rad);
  res.speed = gps_speed(az_degrees, v, 1.0);
  return res;
}
//////////////////////////////////////////////////////////////

abs_accelerometer sd_abs_acc_between_two_geopoints(const gps_coordinate &a,
                                                   const gps_coordinate &b,
                                                   double acceleration_time,
                                                   double interval_time,
                                                   double time_of_interest) {
  double a_az_rad = degree_to_rad(a.speed.azimuth);
  double v0_x = a.speed.value * cos(a_az_rad);
  double v0_y = a.speed.value * sin(a_az_rad);
  abs_accelerometer res(0,0);
  return res;
}
//////////////////////////////////////////////////////////////
