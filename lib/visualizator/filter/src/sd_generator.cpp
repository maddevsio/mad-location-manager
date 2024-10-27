#include "sd_generator.h"

#include <sensor_data.h>

#include <GeographicLib/Geodesic.hpp>
#include <cassert>

gps_coordinate sd_gps_coordinate_in_interval(const gps_coordinate &start,
                                             const movement_interval &interval,
                                             double time_of_interest)
{
  assert(time_of_interest <= interval.duration);

  double iaz_rad = degree_to_rad(interval.azimuth);
  double ax = interval.acceleration * cos(iaz_rad);
  double ay = interval.acceleration * sin(iaz_rad);

  double ssaz_rad = degree_to_rad(start.speed.azimuth);
  double v0_x = start.speed.value * cos(ssaz_rad);
  double v0_y = start.speed.value * sin(ssaz_rad);

  double t = time_of_interest;
  double sx = v0_x * t + ax * t * t * 0.5;  // sx = v0x*t + a*t^2/2
  double sy = v0_y * t + ay * t * t * 0.5;  // sy = v0y*t + a*t^2/2

  double s = sqrt(sx * sx + sy * sy);
  double s_az_rad = atan2(sy, sx);
  double s_az_degrees = rad_to_degree(s_az_rad);

  double vx = v0_x + ax * t;
  double vy = v0_y + ay * t;
  double v = sqrt(vx * vx + vy * vy);
  double v_az_rad = atan2(vy, vx);
  double v_az_degrees = rad_to_degree(v_az_rad);

  gps_coordinate res;
  const GeographicLib::Geodesic &geod = GeographicLib::Geodesic::WGS84();
  geod.Direct(start.location.latitude,
              start.location.longitude,
              s_az_degrees,
              s,
              res.location.latitude,
              res.location.longitude);
  res.speed = gps_speed(v_az_degrees, v, 1.0);

  return res;
}
//////////////////////////////////////////////////////////////

double acc_between_two_points(double distance,
                              double v0,
                              double acceleration_time,
                              double no_acceleration_time)
{
  double s = distance;
  double t1 = acceleration_time;
  double t2 = no_acceleration_time;
  double a = (s - v0 * (t1 + t2)) / ((0.5 * t1 + t2) * t1);
  return a;
}
//////////////////////////////////////////////////////////////

abs_accelerometer sd_abs_acc_between_two_geopoints(const gps_coordinate &a,
                                                   const gps_coordinate &b,
                                                   double acceleration_time,
                                                   double interval_time,
                                                   double time_of_interest)
{
  assert(acceleration_time <= interval_time);

  if (time_of_interest > acceleration_time) {
    return abs_accelerometer(0., 0., 0.);
  }

  double a_az_rad = degree_to_rad(a.speed.azimuth);
  double v0_x = a.speed.value * cos(a_az_rad);
  double v0_y = a.speed.value * sin(a_az_rad);
  double az_ab, az_ba, s_ab;
  const GeographicLib::Geodesic &geod = GeographicLib::Geodesic::WGS84();
  geod.Inverse(a.location.latitude,
               a.location.longitude,
               b.location.latitude,
               b.location.longitude,
               s_ab,
               az_ab,
               az_ba);
  UNUSED(az_ba);

  double ab_az_rad = degree_to_rad(az_ab);
  double sx = s_ab * cos(ab_az_rad);
  double sy = s_ab * sin(ab_az_rad);

  double ax = acc_between_two_points(sx,
                                     v0_x,
                                     acceleration_time,
                                     interval_time - acceleration_time);
  double ay = acc_between_two_points(sy,
                                     v0_y,
                                     acceleration_time,
                                     interval_time - acceleration_time);

  return abs_accelerometer(ax, ay, 0.);
}
//////////////////////////////////////////////////////////////
