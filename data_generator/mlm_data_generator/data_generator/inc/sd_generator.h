#ifndef SD_GENERATOR_H
#define SD_GENERATOR_H

#include "sensor_data.h"
#include <cstdint>
#include <vector>

/// movement_interval_t - interval of movement
/// @azimuth - in degees
/// @acceleration - in m/s^2
/// @duration - seconds
struct movement_interval {
  double azimuth;      // degrees
  double acceleration; // m/s^2
  double duration;     // seconds

  movement_interval() = delete;
  movement_interval(double azimuth, double acceleration, double duration)
      : azimuth(azimuth), acceleration(acceleration), duration(duration) {}
};
//////////////////////////////////////////////////////////////

gps_coordinate sd_gps_coordinate_in_interval(const gps_coordinate &start,
                                             const movement_interval &interval,
                                             double t);

abs_accelerometer sd_abs_acc_between_two_geopoints(const gps_coordinate &a,
                                                   const gps_coordinate &b,
                                                   double acceleration_time,
                                                   double interval_time,
                                                   double time_of_interest);

#endif // sd_generator_h
