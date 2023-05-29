#include <gtest/gtest.h>

#include <boost/program_options.hpp>
#include <cmath>
#include <exception>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>

#include "commons.h"
#include "sd_generator.h"
#include "sensor_data.h"

static geopoint parse_coordinate(const std::string &str);
static std::ostream &mlm_cout() { return std::cout << std::setprecision(12); }
namespace po = boost::program_options;

int main_generator(int argc, char *argv[]) {
  UNUSED(argc);
  UNUSED(argv);

  po::options_description desc("Allowed options");
  // clang-format off
  desc.add_options()
    ("help,h", "Print help message")
    ("acceleration-time,a", po::value<double>(), "Acceleration time in seconds (float value)")
    ("accelerometer-freq,f", po::value<double>(), "Accelerometer data output frequency in seconds (float value)")
    ("gps-freq,g", po::value<double>(), "Interval between 2 GPS points in seconds (float value)")
    ("output,o", po::value<std::string>(), "Output file. Default is STDOUT");
  // clang-format on

  double acceleration_time = 1.0;
  double accelerometer_freq = 1e-1;
  double gps_freq = 1.5;
  double no_acceleration_time = gps_freq - acceleration_time;
  std::string output;

  po::variables_map vm;
  try {
    po::store(po::parse_command_line(argc, argv, desc), vm);
    po::notify(vm);

    if (vm.count("help")) {
      std::cout << desc << std::endl;
      return 0;
    }
    /*******/

    // clang-format off
    double *fields[] = {&acceleration_time, &accelerometer_freq, &gps_freq, NULL};
    const char *po_args[] = {"acceleration-time", "accelerometer-freq", "gps-freq", NULL};
    // clang-format on

    double **pf = fields;
    const char **pa = po_args;
    for (; *pf && *pa; ++pf, ++pa) {
      if (vm.count(*pa)) {
        **pf = vm[*pa].as<double>();
      }
    }

    /*******/
    if (vm.count("output")) {
      output = vm["output"].as<std::string>();
    }
  } catch (const std::exception &exc) {
    std::cout << exc.what() << std::endl;
    return 1;
  }

  std::string input;
  bool is_first_coordinate = true;
  double ts = -gps_freq;

  gps_coordinate prev_coord, current_coord;
  while (std::getline(std::cin, input)) {
    geopoint input_point;
    try {
      input_point = parse_coordinate(input);
    } catch (std::exception &exc) {
      std::cerr << exc.what() << std::endl;
      continue;  // maybe throw?
    }

    ts += gps_freq;
    current_coord.location = input_point;
    if (is_first_coordinate) {
      is_first_coordinate = false;
      prev_coord = current_coord;
      mlm_cout() << "GPS: " << ts << " , " << current_coord.location.latitude
                 << " , " << current_coord.location.longitude << "\n";
      mlm_cout() << "GPSS: " << ts << " , " << current_coord.speed.value
                 << " , " << current_coord.speed.azimuth << " , "
                 << current_coord.speed.accuracy << "\n";
      continue;
    }

    abs_accelerometer acc = sd_abs_acc_between_two_geopoints(
        prev_coord, current_coord, acceleration_time, gps_freq, 0.);

    for (double ats = 0.; ats < acceleration_time; ats += accelerometer_freq) {
      mlm_cout() << "ACC: " << ts + ats - gps_freq << " , " << acc.x << " , "
                 << acc.y << " , " << acc.z << "\n";
      mlm_cout() << "ACCR: " << ts + ats - gps_freq << " , "
                 << acc.acceleration() << " , " << acc.azimuth() << "\n";
    }

    for (double ats = acceleration_time; ats < gps_freq;
         ats += accelerometer_freq) {
      mlm_cout() << "ACC: " << ts + ats - gps_freq << " , " << 0.0 << " , "
                 << 0.0 << " , " << 0.0 << "\n";
    }

    // we have GPS points but have not speed here. so:
    std::vector<movement_interval> intervals = {
        {acc.azimuth(), acc.acceleration(), acceleration_time},
        {0., 0., no_acceleration_time},
    };

    current_coord = prev_coord;
    for (const auto &interval : intervals) {
      current_coord = sd_gps_coordinate_in_interval(current_coord, interval,
                                                    interval.duration);

      mlm_cout() << "\tGPS: " << ts << " , " << current_coord.location.latitude
                 << " , " << current_coord.location.longitude << "\n";
      mlm_cout() << "\tGPSS: " << ts << " , " << current_coord.speed.value
                 << " , " << current_coord.speed.azimuth << " , "
                 << current_coord.speed.accuracy << "\n";
    }
    // now we have GPS coordinate and speed

    mlm_cout() << "GPS: " << ts << " , " << current_coord.location.latitude
               << " , " << current_coord.location.longitude << "\n";
    mlm_cout() << "GPSS: " << ts << " , " << current_coord.speed.value << " , "
               << current_coord.speed.azimuth << " , "
               << current_coord.speed.accuracy << "\n";
    prev_coord = current_coord;
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
