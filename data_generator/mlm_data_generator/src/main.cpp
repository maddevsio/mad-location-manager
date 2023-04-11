#include "commons.h"
#include "coordinate.h"
#include "sd_generator.h"
#include "sensor_data.h"
#include <iostream>
#include <vector>

int main(int argc, char *argv[]) {
  UNUSED(argc);
  UNUSED(argv);

  std::vector<movement_interval_t> intervals = {
    {0.0, 3.0, 5.0},
  };

  for (auto interval : intervals) {
    std::cout << interval.azimuth << " " << interval.duration << " " << interval.acceleration << "\n";
  }

}
