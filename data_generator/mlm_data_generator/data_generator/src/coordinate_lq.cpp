#include "commons.h"
#include "coordinate.h"
#include "sensor_data.h"
#include <cmath>

using namespace coordinate_consts;

// https://en.wikipedia.org/wiki/Great-circle_distance
static double geo_distance_meters(double lat1, double lon1, double lat2,
                                  double lon2);
static geopoint point_ahead(geopoint point, double distance,
                            double azimuth_degrees);
static geopoint point_plus_distance_east(geopoint point, double distance);
static geopoint point_plus_distance_north(geopoint point, double distance);

static double coord_longitude_to_meters(double lon);
static double coord_latitude_to_meters(double lat);
static geopoint coord_meters_to_geopoint(double lon_meters, double lat_meters);

/////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////

double geo_distance_meters(double lon1, double lat1, double lon2, double lat2) {
  // Convert latitude and longitude from degrees to radians
  double lat1Rad = lat1 * M_PI / 180.0;
  double lon1Rad = lon1 * M_PI / 180.0;
  double lat2Rad = lat2 * M_PI / 180.0;
  double lon2Rad = lon2 * M_PI / 180.0;

  // Calculate differences in latitudes and longitudes
  double delta_lat = lat2Rad - lat1Rad;
  double delta_lon = lon2Rad - lon1Rad;

  double a =
      sin(delta_lat / 2.0) * sin(delta_lat / 2.0) +
      cos(lat1Rad) * cos(lat2Rad) * 
      sin(delta_lon / 2.0) * sin(delta_lon / 2.0);
  double c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a));
  return EARTH_RADIUS * c;
}
//////////////////////////////////////////////////////////////

geopoint point_ahead(geopoint point, double distance, double azimuth_degees) {
  geopoint res;
  double radius_fraction = distance / EARTH_RADIUS;
  double bearing = degree_to_rad(azimuth_degees);
  double lat1 = degree_to_rad(point.latitude);
  double lng1 = degree_to_rad(point.longitude);

  double lat2_part1 = sin(lat1) * cos(radius_fraction);
  double lat2_part2 = cos(lat1) * sin(radius_fraction) * cos(bearing);
  double lat2 = asin(lat2_part1 + lat2_part2);

  double lng2_part1 = sin(bearing) * sin(radius_fraction) * cos(lat1);
  double lng2_part2 = cos(radius_fraction) - sin(lat1) * sin(lat2);
  double lng2 = lng1 + atan2(lng2_part1, lng2_part2);
  lng2 = fmod(lng2 + 3.0 * M_PI, 2.0 * M_PI) - M_PI;

  res.latitude = rad_to_degree(lat2);
  res.longitude = rad_to_degree(lng2);
  return res;
}
//////////////////////////////////////////////////////////////

geopoint point_plus_distance_east(geopoint point, double distance) {
  return point_ahead(point, distance, 90.0);
}
//////////////////////////////////////////////////////////////

geopoint point_plus_distance_north(geopoint point, double distance) {
  return point_ahead(point, distance, 0.0);
}
//////////////////////////////////////////////////////////////

double coord_longitude_to_meters(double lon) {
  double distance = geo_distance_meters(lon, 0.0, 0.0, 0.0);
  return distance * (lon < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////

double coord_latitude_to_meters(double lat) {
  double distance = geo_distance_meters(0.0, lat, 0.0, 0.0);
  return distance * (lat < 0.0 ? -1.0 : 1.0);
}
//////////////////////////////////////////////////////////////

geopoint coord_meters_to_geopoint(double lon_meters, double lat_meters) {
  geopoint point(0.0, 0.0);
  geopoint pointEast = point_plus_distance_east(point, lon_meters);
  geopoint pointNorthEast = point_plus_distance_north(pointEast, lat_meters);
  return pointNorthEast;
}
//////////////////////////////////////////////////////////////

coordinates_vptr coord_vptr(void) {
  coordinates_vptr res = {
      .distance_between_points = geo_distance_meters,
      .longitude_to_meters = coord_longitude_to_meters,
      .latitude_to_meters = coord_latitude_to_meters,
      .meters_to_geopoint = coord_meters_to_geopoint,
      .point_ahead = point_ahead,
  };
  return res;
}
//////////////////////////////////////////////////////////////