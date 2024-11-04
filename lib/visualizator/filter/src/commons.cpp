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
  double cartezian = M_PI_2 - rad;
  if (cartezian < 0.) {
    cartezian += M_PI * 2.;
  }
  return cartezian;
}
//////////////////////////////////////////////////////////////

double azimuth_to_cartezian_degrees(double degrees)
{
  double cartezian = 90. - degrees;
  if (cartezian < 0.) {
    cartezian += 360.;
  }
  return cartezian;
}
//////////////////////////////////////////////////////////////
