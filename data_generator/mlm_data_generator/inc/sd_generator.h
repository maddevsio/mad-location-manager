#ifndef SD_GENERATOR_H
#define SD_GENERATOR_H

#include "sensor_data.h"
#include <cstdint>
#include <vector>

struct movement_interval_t {
  double azimuth;      // degrees
  double acceleration; // m/s^2
  double duration;     // seconds

  movement_interval_t() = delete;
  movement_interval_t(double azimuth, double acceleration, double duration)
      : azimuth(azimuth), acceleration(acceleration), duration(duration) {}
};

gps_coordinate_t gps_coordinate(const gps_coordinate_t &start,
                                const movement_interval_t &interval, double t);

#endif // SD_GENERATOR_H
