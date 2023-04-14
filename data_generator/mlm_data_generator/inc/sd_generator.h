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

gps_coordinate sd_gps_coordinate(const gps_coordinate &start,
                                   const movement_interval &interval,
                                   double t);

#endif // SD_GENERATOR_H
