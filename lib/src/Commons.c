#include <math.h>
#include <stdlib.h>
#include <assert.h>

double
Degree2Rad(double degree) {
  return degree*M_PI/180.0;
}
///////////////////////////////////////////////////////

double
Rad2Degree(double rad) {
  return rad*180.0/M_PI;
}
///////////////////////////////////////////////////////

double
MilesPerHour2MeterPerSecond(double mph) {
  return 2.23694 * mph;
}
///////////////////////////////////////////////////////

int
RandomBetween2Vals(int low, int hi) {
  assert(low <= hi);
  return (rand() % (hi - low)) + low;
}
///////////////////////////////////////////////////////

double
LowPassFilter(double prev, double measured, double alpha) {
  return prev + alpha * (measured - prev);
}
///////////////////////////////////////////////////////
