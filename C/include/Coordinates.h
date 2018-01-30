#ifndef COORDINATES_H
#define COORDINATES_H

#include <vector>
class QString;

#define COORDINATES_HIGH_ACCURACY 0

struct geopoint_t {
  double Latitude, Longitude;
  geopoint_t() : Latitude(0.0), Longitude(0.0) {
  }
  geopoint_t(double lat, double lon) : Latitude(lat), Longitude(lon) {
  }
};

std::vector<geopoint_t> CoordGetFromFile(const QString& filePath, int interested);
std::vector<geopoint_t> CoordFilterByGeoHash(std::vector<geopoint_t> &lstSrc,
                                             int precision,
                                             int minPointCount);

#define EARTH_RADIUS (6371.0 * 1000.0) // meters
#define ACTUAL_GRAVITY 9.80665

/*todo write tests*/
double CoordDistanceBetweenPointsMeters(double lat1, double lon1, double lat2, double lon2);
double CoordLongitudeToMeters(double lon);
double CoordLatitudeToMeters(double lat);
geopoint_t CoordMetersToGeopoint(double lonMeters,
                            double latMeters);

double CoordCaclulateDistance(const std::vector<geopoint_t> &lst);

#endif // COORDINATES_H
