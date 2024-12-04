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

double cartezian_to_azimuth_rad(double rad)
{
  double az = fmod((M_PI / 2.) - rad + 2. * M_PI, 2. * M_PI);
  return az;
}
//////////////////////////////////////////////////////////////

double cartezian_to_azimuth_degrees(double degrees)
{
  double az = fmod((360. / 2.) - degrees + 360., 360.);
  return az;
}
//////////////////////////////////////////////////////////////
