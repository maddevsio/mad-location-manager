#ifndef COORDINATES_H
#define COORDINATES_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif
typedef struct geopoint {
  double Latitude, Longitude;
} geopoint_t;

/*todo write tests*/
double coord_distance_between_points_meters(double lat1,
                                            double lon1,
                                            double lat2,
                                            double lon2);
double coord_distance_between_points_meters_hq(double lat1,
                                               double lon1,
                                               double lat2,
                                               double lon2);
double coord_longitude_to_meters(double lon);
double coord_longitude_to_meters_hq(double lon);

double coord_latitude_to_meters(double lat);
double coord_latitude_to_meters_hq(double lat);

geopoint_t coord_meters_to_geopoint(double lonMeters,
                                    double latMeters);
geopoint_t coord_meters_to_geopoint_hq(double lonMeters,
                                       double latMeters);

#ifdef __cplusplus
}
#endif // extern "C"
#endif // COORDINATES_H
