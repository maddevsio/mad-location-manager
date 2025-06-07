#include "commons.h"

#include <cmath>

double degree_to_rad(double degree)
{
  return degree * M_PI / 180.0;
}
///////////////////////////////////////////////////////

double rad_to_degree(double rad)
{
  return rad * 180.0 / M_PI;
}
///////////////////////////////////////////////////////

double azimuth_to_cartezian_rad(double rad)
{
  const double two_pi = 2.0 * M_PI;
  double theta = M_PI_2 - rad;
  theta = std::fmod(theta + two_pi, two_pi);
  return theta;
}
//////////////////////////////////////////////////////////////

double cartezian_to_azimuth_rad(double rad)
{
  const double two_pi = 2.0 * M_PI;
  double az = fmod(M_PI_2 - rad + two_pi, two_pi);
  return az;
}
//////////////////////////////////////////////////////////////
