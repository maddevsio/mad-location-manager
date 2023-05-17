#include <gtest/gtest.h>

#include <cmath>
#include <exception>
#include <iostream>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>

#include "commons.h"
#include "sd_generator.h"
#include "sensor_data.h"

static geopoint parse_coordinate(const std::string &str);

int main_generator(int argc, char *argv[]) {
  UNUSED(argc);
  UNUSED(argv);

  // todo parse arguments and get all these constants
  const double acceleration_time = 1.0;
  const double interval_time = 1.5;
  const double accelerometer_freq = 1e-3;

  std::string input;
  bool is_first_coordinate = true;

  gps_coordinate prev_coord, current_coord;
  while (std::getline(std::cin, input)) {
    try {
      geopoint curr_point = parse_coordinate(input);
      current_coord.location = curr_point;
      std::cout << "GPS: " << curr_point.latitude << " , " << curr_point.longitude << "\n";
      if (is_first_coordinate) {
        is_first_coordinate = false;
        prev_coord = current_coord;
        continue;
      }

      abs_accelerometer acc = sd_abs_acc_between_two_geopoints(
          prev_coord, current_coord, acceleration_time, interval_time, 0.);

      std::cout << "acc: " << acc.x << " " << acc.y << " " << acc.z << "\n";
      // we have GPS points but have not speed here. so:
      std::vector<movement_interval> intervals = {
          {acc.azimuth(), acc.acceleration(), acceleration_time},
          {0., 0., interval_time - acceleration_time},
      };

      for (auto interval : intervals) {
        current_coord = sd_gps_coordinate_in_interval(current_coord, interval,
                                                      interval.duration);
      }
      // now we have GPS coordinate and speed
      prev_coord = current_coord;

    } catch (std::exception &exc) {
      std::cerr << exc.what() << std::endl;
      continue;  // maybe throw?
    }
  }

  return 0;
}
//////////////////////////////////////////////////////////////

geopoint parse_coordinate(const std::string &str) {
  std::istringstream iss(str);
  char delim = 0;
  double lat, lon;
  iss >> lat >> std::ws >> delim >> std::ws >> lon;
  if (!iss || delim != ',') {
    throw std::invalid_argument(std::string("could not parse input string ") +
                                str);
  }
  return geopoint(lat, lon);
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
