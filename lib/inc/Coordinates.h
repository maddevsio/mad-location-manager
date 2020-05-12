#ifndef COORDINATES_H
#define COORDINATES_H

struct geopoint_t {
  double Latitude, Longitude;
  geopoint_t() : Latitude(0.0), Longitude(0.0) {
  }
  geopoint_t(double lat, double lon) : Latitude(lat), Longitude(lon) {
  }
};

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



#endif // COORDINATES_H
