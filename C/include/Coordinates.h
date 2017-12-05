#ifndef COORDINATES_H
#define COORDINATES_H

#include <vector>
class QString;

struct geopoint_t {
  double Latitude, Longitude;
  geopoint_t() : Latitude(0.0), Longitude(0.0) {
  }
  geopoint_t(double lat, double lon) : Latitude(lat), Longitude(lon) {
  }
};

std::vector<geopoint_t> GetCoordsFromFile(const QString& filePath);
std::vector<geopoint_t> FilterByGeoHash(std::vector<geopoint_t> &lstSrc, int precision, int minPointCount);


#define EARTH_RADIUS (6371.0 * 1000.0) // meters
#define ACTUAL_GRAVITY 9.80665

/*todo write tests*/
double LongitudeToMeters(double lon);
double LatitudeToMeters(double lat);
geopoint_t MetersToGeopoint(double lonMeters,
                            double latMeters);


#endif // COORDINATES_H
