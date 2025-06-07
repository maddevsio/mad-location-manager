#include "sd_generator.h"

#include <sensor_data.h>

#include <GeographicLib/Geodesic.hpp>
#include <cassert>
#include <cmath>
#include <random>

#include "GeographicLib/LocalCartesian.hpp"
#include "commons.h"

gps_coordinate sd_gps_coordinate_in_interval(const gps_coordinate &start,
                                             const movement_interval &interval,
                                             double time_of_interest)
{
  assert(time_of_interest <= interval.duration);

  GeographicLib::LocalCartesian m_lc;
  double x, y, z;
  m_lc.Reset(start.location.latitude,
             start.location.longitude,
             start.location.altitude);
  // start coordinates
  x = y = z = 0.;
  double iaz_rad = degree_to_rad(interval.cartezian_angle);
  double ax = interval.acceleration * cos(iaz_rad);
  double ay = interval.acceleration * sin(iaz_rad);

  double ssaz_rad = degree_to_rad(start.speed.azimuth);
  ssaz_rad = azimuth_to_cartezian_rad(ssaz_rad);
  double v0_x = start.speed.value * cos(ssaz_rad);
  double v0_y = start.speed.value * sin(ssaz_rad);

  double t = time_of_interest;
  x = v0_x * t + ax * t * t * 0.5;  // sx = v0x*t + a*t^2/2
  y = v0_y * t + ay * t * t * 0.5;  // sy = v0y*t + a*t^2/2

  double vx = v0_x + ax * t;
  double vy = v0_y + ay * t;
  double v = sqrt(vx * vx + vy * vy);
  double v_cart_rad = atan2(vy, vx);
  double v_az_rad = cartezian_to_azimuth_rad(v_cart_rad);
  double v_az_degrees = rad_to_degree(v_az_rad);

  gps_coordinate res;
  m_lc.Reverse(x,
               y,
               z,
               res.location.latitude,
               res.location.longitude,
               res.location.altitude);
  // TODO check the error of speed
  res.speed = gps_speed(v, v_az_degrees, 1e-6);
  return res;
}
//////////////////////////////////////////////////////////////

double sd_acc_between_two_points(double distance,
                                 double v0,
                                 double acceleration_time,
                                 double no_acceleration_time)
{
  double s = distance;
  double t1 = acceleration_time;
  double t2 = no_acceleration_time;
  // s = s1 + s2
  // s1 = v0*t1 + 0.5*a*t1^2
  // v2 = v0+a*t1
  // s2 = v2*t2
  // s = v0*t1 + 0.5*a*t1^2 + (v0+a*t1)*t2
  // s - v0*(t1+t2) = a * (0.5*t1^2 + t1*t2)
  // a = (s - v0*(t1+t2)) / (0.5*t1^2 + t1*t2)
  double a = (s - v0 * (t1 + t2)) / (0.5 * t1 * t1 + t1 * t2);
  return a;
}
//////////////////////////////////////////////////////////////

double sd_distance_between_two_points(const gps_coordinate &a,
                                      const gps_coordinate &b)
{
  const GeographicLib::Geodesic &geod = GeographicLib::Geodesic::WGS84();
  double distance = 0.0;
  geod.Inverse(a.location.latitude,
               a.location.longitude,
               b.location.latitude,
               b.location.longitude,
               distance);
  return distance;
}
//////////////////////////////////////////////////////////////

enu_accelerometer sd_abs_acc_between_two_geopoints(const gps_coordinate &a,
                                                   const gps_coordinate &b,
                                                   double acceleration_time,
                                                   double interval_time,
                                                   double time_of_interest)
{
  assert(acceleration_time <= interval_time);

  if (time_of_interest > acceleration_time) {
    return enu_accelerometer(0., 0., 0.);
  }

  double a_az_rad = degree_to_rad(a.speed.azimuth);
  a_az_rad = azimuth_to_cartezian_rad(a_az_rad);
  double v0_x = a.speed.value * cos(a_az_rad);
  double v0_y = a.speed.value * sin(a_az_rad);

  GeographicLib::LocalCartesian m_lc;
  double x, y, z;
  m_lc.Reset(a.location.latitude, a.location.longitude, a.location.altitude);
  m_lc.Forward(b.location.latitude,
               b.location.longitude,
               b.location.altitude,
               x,
               y,
               z);
  UNUSED(z);

  double no_acc_time = interval_time - acceleration_time;
  double ax =
      sd_acc_between_two_points(x, v0_x, acceleration_time, no_acc_time);
  double ay =
      sd_acc_between_two_points(y, v0_y, acceleration_time, no_acc_time);
  return enu_accelerometer(ax, ay, 0.);
}
//////////////////////////////////////////////////////////////

geopoint sd_noised_geopoint(const geopoint &src, double gps_noise)
{
  // Will be used to obtain a seed for the random number engine
  std::random_device rd;
  // Standard mersenne_twister_engine seeded with rd()
  std::mt19937 gen(rd());
  std::uniform_real_distribution<> gps_dist(0., gps_noise);
  static std::uniform_real_distribution<double> az_dist(-180.0, 180.0);
  double gps_error = gps_dist(gen);
  double az_rnd = az_dist(gen);

  geopoint res;
  const GeographicLib::Geodesic &geod = GeographicLib::Geodesic::WGS84();
  geod.Direct(src.latitude,
              src.longitude,
              az_rnd,
              gps_error,
              res.latitude,
              res.longitude);

  res.error = gps_noise;
  return res;
}
//////////////////////////////////////////////////////////////

enu_accelerometer sd_noised_acc(const enu_accelerometer &acc, double acc_noise)
{
  if (acc_noise <= 1e-6) {
    return enu_accelerometer(acc.x, acc.y, acc.z);
  }
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_real_distribution<double> gps_dist(-acc_noise / 2.0,
                                                  acc_noise / 2.0);

  double x_noised = acc.x + gps_dist(gen);
  double y_noised = acc.y + gps_dist(gen);
  enu_accelerometer nacc(x_noised, y_noised, acc.z);
  return nacc;
}
