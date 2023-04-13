#include "commons.h"
#include "sd_generator.h"
#include "sensor_data.h"
#include <iostream>
#include <vector>
#include <gtest/gtest.h>


int main_generator(int argc, char *argv[]) {
  UNUSED(argc);
  UNUSED(argv);

  gps_coordinate_t start_coordinate;
  start_coordinate.location = gps_location_t(0.0, 0.0);
  start_coordinate.speed = gps_speed_t(0.0, 0.0, 1.0);

  std::vector<movement_interval_t> intervals = {{0.0, 3.0, 5.0},
                                                {0.0, -6.0, 5.0}};

  double t = 1.5;
  for (const auto &interval : intervals) {
    gps_coordinate_t next_coord =
        sd_gps_coordinate(start_coordinate, interval, t);

    std::cout << next_coord.location.latitude << " "
              << next_coord.location.latitude << "\n";
    std::cout << next_coord.speed.value << " " << next_coord.speed.azimuth
              << "\n";
    start_coordinate = next_coord;
  }
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
