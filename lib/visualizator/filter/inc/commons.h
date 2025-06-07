#ifndef COMMONS_H
#define COMMONS_H

#define UNUSED(x) ((void)x)

double degree_to_rad(double degree);
//////////////////////////////////////////////////////////////////////////

double rad_to_degree(double rad);
//////////////////////////////////////////////////////////////////////////

double azimuth_to_cartezian_rad(double rad);
double cartezian_to_azimuth_rad(double rad);

#endif  // COMMONS_H
