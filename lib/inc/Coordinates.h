#ifndef COORDINATES_H
#define COORDINATES_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct geopoint {
  double Latitude, Longitude;
} geopoint_t;

typedef struct coordinates_vptr {
  double (*distance_between_points)(double lat1, double lon1, double lat2, double lon2);
  double (*longitude_to_meters)(double lon);
  double (*latitude_to_meters)(double lat);
  geopoint_t (*meters_to_geopoint)(double lon_meters, double lat_meters);
} coordinates_vptr_t;

coordinates_vptr_t coord_vptr(void);
coordinates_vptr_t coord_vptr_hq(void);

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

geopoint_t coord_meters_to_geopoint(double lon_meters,
                                    double lat_meters);
geopoint_t coord_meters_to_geopoint_hq(double lon_meters,
                                       double lat_meters);

#ifdef __cplusplus
}
#endif // extern "C"
#endif // COORDINATES_H
