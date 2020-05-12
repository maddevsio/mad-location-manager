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
double CoordDistanceBetweenPointsMeters(double lat1,
                                        double lon1,
                                        double lat2,
                                        double lon2);
double CoordDistanceBetweenPointsMetersHQ(double lat1,
                                          double lon1,
                                          double lat2,
                                          double lon2);
double CoordLongitudeToMeters(double lon);
double CoordLongitudeToMetersHQ(double lon);

double CoordLatitudeToMeters(double lat);
double CoordLatitudeToMetersHQ(double lat);

geopoint_t CoordMetersToGeopoint(double lonMeters,
                                 double latMeters);
geopoint_t CoordMetersToGeopointHQ(double lonMeters,
                                   double latMeters);

#ifdef __cplusplus
}
#endif // extern "C"
#endif // COORDINATES_H
