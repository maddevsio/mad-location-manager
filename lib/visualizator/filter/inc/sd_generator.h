#ifndef SD_GENERATOR_H
#define SD_GENERATOR_H

#include <sensor_data.h>


/// movement_interval_t - interval of movement
/// @azimuth - in degrees
/// @acceleration - in m/s^2
/// @duration - seconds
struct movement_interval {
  double azimuth;       // degrees
  double acceleration;  // m/s^2
  double duration;      // seconds

  movement_interval() = delete;
  movement_interval(double azimuth, double acceleration, double duration)
      : azimuth(azimuth), acceleration(acceleration), duration(duration)
  {
  }
};
//////////////////////////////////////////////////////////////

gps_coordinate sd_gps_coordinate_in_interval(const gps_coordinate &start,
                                             const movement_interval &interval,
                                             double time_of_interest);

abs_accelerometer sd_abs_acc_between_two_geopoints(const gps_coordinate &a,
                                                   const gps_coordinate &b,
                                                   double acceleration_time,
                                                   double interval_time,
                                                   double time_of_interest);

double acc_between_two_points(double distance,
                              double v0,
                              double acceleration_time,
                              double no_acceleration_time);

#endif
