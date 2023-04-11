#include "commons.h"
#include "coordinate.h"
#include "sensor_data.h"
#include <math.h>

using namespace coordinate_consts;

// https://en.wikipedia.org/wiki/Great-circle_distance
static double geo_distance_meters(double lon1, double lat1, double lon2,
                                  double lat2);
static gps_location_t point_ahead(gps_location_t point, double distance,
                                  double azimuth_degrees);
static gps_location_t point_plus_distance_east(gps_location_t point,
                                               double distance);
static gps_location_t point_plus_distance_north(gps_location_t point,
                                                double distance);

static double coord_distance_between_points_meters(double lat1, double lon1,
                                                   double lat2, double lon2);
static double coord_longitude_to_meters(double lon);
static double coord_latitude_to_meters(double lat);
static gps_location_t coord_meters_to_geopoint(double lon_meters,
                                               double lat_meters);

/////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////

double geo_distance_meters(double lon1, double lat1, double lon2, double lat2) {
  double delta_lon = degree_to_rad(lon2 - lon1);
  double delta_lat = degree_to_rad(lat2 - lat1);
  double a = pow(sin(delta_lat / 2.0), 2.0) +
             cos(degree_to_rad(lat1)) * cos(degree_to_rad(lat2)) *
                 pow(sin(delta_lon / 2.0), 2.0);
  double c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a));
  return EARTH_RADIUS * c;
}
//////////////////////////////////////////////////////////////

gps_location_t point_ahead(gps_location_t point, double distance,
                           double azimuth_degees) {
  gps_location_t res;
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

gps_location_t point_plus_distance_east(gps_location_t point, double distance) {
  return point_ahead(point, distance, 90.0);
}
//////////////////////////////////////////////////////////////

gps_location_t point_plus_distance_north(gps_location_t point,
                                         double distance) {
  return point_ahead(point, distance, 0.0);
}
//////////////////////////////////////////////////////////////

double coord_distance_between_points_meters(double lat1, double lon1,
                                            double lat2, double lon2) {
  return geo_distance_meters(lon1, lat1, lon2, lat2);
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

gps_location_t coord_meters_to_geopoint(double lon_meters, double lat_meters) {
  gps_location_t point; // = {.latitude = 0.0, .longitude = 0.0};
  gps_location_t pointEast = point_plus_distance_east(point, lon_meters);
  gps_location_t pointNorthEast =
      point_plus_distance_north(pointEast, lat_meters);
  return pointNorthEast;
}
//////////////////////////////////////////////////////////////

coordinates_vptr coord_vptr(void) {
  coordinates_vptr res = {
      .distance_between_points = coord_distance_between_points_meters,
      .longitude_to_meters = coord_longitude_to_meters,
      .latitude_to_meters = coord_latitude_to_meters,
      .meters_to_geopoint = coord_meters_to_geopoint,
      .point_ahead = point_ahead,
  };
  return res;
}
//////////////////////////////////////////////////////////////
