#include "sd_generator.h"
#include "sensor_data.h"
#include <cmath>
#include <gtest/gtest.h>
#include <vector>

TEST(sd_generator, test_speed_generation) {
  gps_coordinate sc;
  sc.location = geopoint(0.0, 0.0);
  sc.speed = gps_speed(0.0, 0.0, 1.0);

  std::vector<movement_interval> intervals = {
      {0.0, 5.0, 5.0},
      {0.0, 0.0, 15.0},
      {180.0, 5.0, 5.0},
  };

  double start_time = 0.0;
  double acc_interval = 0.001; // 1000 per second

  static const double EPS = 1e-9;
  for (const auto &interval : intervals) {
    double end_time = start_time + interval.duration;
    abs_accelerometer acc(interval.acceleration, interval.azimuth);
    // while (end_time > start_time) ..
    while (fabs(end_time - start_time) > EPS) {
      start_time += acc_interval;
      sc = sd_gps_coordinate_in_interval(sc, interval, acc_interval);
    } // while (end_time > start_time);
  }   // for (const auto &interval : intervals)

  EXPECT_NEAR(0.0, sc.speed.value, EPS);
}
//////////////////////////////////////////////////////////////
