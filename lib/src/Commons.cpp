#include "Commons.hpp"

#include <math.h>
#include <stdlib.h>
#include <assert.h>

double
degree_to_rad(double degree) {
  return degree*M_PI/180.0;
}
///////////////////////////////////////////////////////

double
rad_to_degree(double rad) {
  return rad*180.0/M_PI;
}
///////////////////////////////////////////////////////

double
miles_per_hour_to_meters_per_second(double mph) {
  return 2.23694 * mph;
}
///////////////////////////////////////////////////////

int
random_between_2_vals(int low, int hi) {
  assert(low <= hi);
  return (rand() % (hi - low)) + low;
}
///////////////////////////////////////////////////////
