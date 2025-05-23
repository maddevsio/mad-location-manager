#ifndef SD_GENERATOR_H
#define SD_GENERATOR_H

#include "sensor_data.h"

/// movement_interval_t - interval of movement
/// @azimuth - in degrees
/// @acceleration - in m/s^2
/// @duration - seconds
struct movement_interval {
  double cartezian_angle;  // degrees
  double acceleration;     // m/s^2
  double duration;         // seconds

  movement_interval() = delete;
  movement_interval(double cartezian_angle,
                    double acceleration,
                    double duration)
      : cartezian_angle(cartezian_angle),
        acceleration(acceleration),
        duration(duration)
  {
  }
};
//////////////////////////////////////////////////////////////

gps_coordinate sd_gps_coordinate_in_interval(const gps_coordinate &start,
                                             const movement_interval &interval,
                                             double time_of_interest);

enu_accelerometer sd_abs_acc_between_two_geopoints(const gps_coordinate &a,
                                                   const gps_coordinate &b,
                                                   double acceleration_time,
                                                   double interval_time,
                                                   double time_of_interest);

double sd_acc_between_two_points(double distance,
                                 double v0,
                                 double acceleration_time,
                                 double no_acceleration_time);

double sd_distance_between_two_points(const gps_coordinate &a,
                                      const gps_coordinate &b);

geopoint sd_noised_geopoint(const geopoint &src, double gps_noise);
enu_accelerometer sd_noised_acc(const enu_accelerometer &acc, double acc_noise);
#endif
