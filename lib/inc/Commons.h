#ifndef COMMONS_H
#define COMMONS_H

#include <stdint.h>

#define UNUSED_ARG(x) ((void)x)

#ifdef __cplusplus
extern "C" {
#endif

double degree_to_rad(double degree);
//////////////////////////////////////////////////////////////////////////

double rad_to_degree(double rad);
//////////////////////////////////////////////////////////////////////////

double miles_per_hour_to_meters_per_second(double mph);
//////////////////////////////////////////////////////////////////////////

int random_between_2_vals(int low,
                          int hi);
///////////////////////////////////////////////////////

double low_pass_filter(double prev,
                       double measured,
                       double alpha);
///////////////////////////////////////////////////////

#ifdef __cplusplus
}
#endif // extern "C"

#endif // COMMONS_H
