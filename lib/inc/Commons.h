#ifndef COMMONS_H
#define COMMONS_H

#include <math.h>
#include <stdlib.h>
#include <assert.h>

#define UNUSED_ARG(x) ((void)x)

inline double Degree2Rad(double degree) {
  static const double coef = M_PI/180.0;
  return degree*coef;
}
//////////////////////////////////////////////////////////////////////////

inline double Rad2Degree(double rad) {
  static const double coef = 180.0/M_PI;
  return rad*coef;
}
//////////////////////////////////////////////////////////////////////////

inline double MilesPerHour2MeterPerSecond(double mph) {
  return 2.23694 * mph;
}
//////////////////////////////////////////////////////////////////////////

inline int RandomBetween2Vals(int low,
                              int hi) {
  assert(low <= hi);
  return (rand() % (hi - low)) + low;
}
///////////////////////////////////////////////////////

inline double LowPassFilter(double prev,
                            double measured,
                            double alpha) {
  return prev + alpha * (measured - prev);
}
///////////////////////////////////////////////////////

#endif // COMMONS_H
