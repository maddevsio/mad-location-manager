#include "sd_generator.h"
#include "coordinate.h"
#include "sensor_data.h"
#include <assert.h>

gps_coordinate sd_gps_coordinate(const gps_coordinate &start,
                                   const movement_interval &interval,
                                   double t) {
  // todo move this vptr somewhere %)
  static coordinates_vptr vptr = coord_vptr_hq();
  assert(t <= interval.duration);

  double a = interval.acceleration;
  double v0 = start.speed.value;
  double s = v0 * t + a * t * t / 2.0; // s = v0*t + a*t^2/2
  gps_coordinate res;
  res.location = vptr.point_ahead(start.location, s, interval.azimuth);
  // check that accuracy in [0, 1]. if not - change this val
  double speed_accuracy = 1.0;
  res.speed = gps_speed(interval.azimuth, v0 + a * t, speed_accuracy);
  return res;
}
