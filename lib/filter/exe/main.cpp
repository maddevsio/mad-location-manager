#include <cmath>
#include <gtest/gtest.h>
#include <iostream>

#include "commons.h"

#include <GeographicLib/LocalCartesian.hpp>

#ifdef _UNIT_TESTS_
int main_tests(int argc, char *argv[]) {
  testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}
#endif
//////////////////////////////////////////////////////////////

int main(int argc, char *argv[], char **env) {
#ifdef _UNIT_TESTS_
  UNUSED(env);
  return main_tests(argc, argv);
#else
  UNUSED(argc);
  UNUSED(argv);
  UNUSED(env);

  double start_lat = 36.5519514;
  double start_lon = 31.9801362;

  GeographicLib::LocalCartesian lc;
  lc.Reset(start_lat, start_lon);

  double x, y, z;
  for (double d_lat = 0.0; d_lat < 0.01; d_lat += 0.001) {
    lc.Forward(start_lat + d_lat, start_lon, 0., x, y, z);
    std::cout << start_lat + d_lat << "::" << start_lon << "\t:\t" << x << "\t"
              << y << "\t" << z << "\n";
    double lat, lon, alt;
    lc.Reverse(x, y, z, lat, lon, alt);

    std::cout << x << "::" << y << "::" << z << "\t:\t" << lat << "\t" << lon
              << "\t" << alt << "\n";
  }
  return 0;
#endif
}
/////////////////////////////////////////////////////////////
