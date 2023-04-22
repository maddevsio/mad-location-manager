#include "commons.h"
#include "sd_generator.h"
#include "sensor_data.h"
#include <cmath>
#include <gtest/gtest.h>
#include <iostream>
#include <vector>

int main_generator(int argc, char *argv[]) {
  UNUSED(argc);
  UNUSED(argv);

  gps_coordinate sc;
  sc.location = geopoint(0.0, 0.0);
  sc.speed = gps_speed(0.0, 0.0, 1.0);

  double start_time = 0.0;
  double gps_interval = 1.5;
  double acc_interval = 0.001; // 1000 per second
  double next_gps_time = gps_interval;

  std::vector<movement_interval> intervals = {
      {0.0, 5.0, 5.0},
      //{0.0, 0.0, 15.0},
      {180.0, 5.0, 5.0},
  };

  for (const auto &interval : intervals) {
    double end_time = start_time + interval.duration;
    abs_accelerometer acc(interval.acceleration, interval.azimuth);

    static const double EPS = 1e-9;
    // while (end_time > start_time) ..
    while (fabs(end_time - start_time) > EPS) {
      start_time += acc_interval;
      sc = sd_gps_coordinate(sc, interval, acc_interval);
      if (fabs(start_time - next_gps_time) < EPS) {
        next_gps_time += gps_interval;
        std::cout << start_time << "\tGC\t" << sc.location.latitude << "\t"
                  << sc.location.longitude << "\n";
        std::cout << start_time << "\tGS\t" << sc.speed.azimuth << "\t"
                  << sc.speed.value << "\t" << sc.speed.accuracy << "\n";
      }
    } // while (end_time > start_time);

    std::cout << start_time << "\tGS\t" << sc.speed.azimuth << "\t"
              << sc.speed.value << "\t" << sc.speed.accuracy << "\n";
  } // for (const auto &interval : intervals)
  std::cout << start_time << "\tGS\t" << sc.speed.azimuth << "\t"
            << sc.speed.value << "\t" << sc.speed.accuracy << "\n";
  return 0;
}
//////////////////////////////////////////////////////////////

#ifdef _UNIT_TESTS_
int main_tests(int argc, char *argv[]) {
  testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}
#endif
//////////////////////////////////////////////////////////////

int main(int argc, char *argv[]) {
#ifdef _UNIT_TESTS_
  return main_tests(argc, argv);
#else
  return main_generator(argc, argv);
#endif
}
//////////////////////////////////////////////////////////////
