#ifndef _COORDINATE_H_
#define _COORDINATE_H_

#include "sensor_data.h"

namespace coordinate_consts {
const double earth_radius = 6371.0 * 1000.0; // meters
// https://en.wikipedia.org/wiki/World_Geodetic_System#WGS_84
const double major_axis = 6378137.0;
const double flattening = 298.257223563; // f
const double inv_flattening = 1.0 / flattening; // e - eccentricity
const double minor_axis = (1 - inv_flattening) * major_axis;
}; // namespace coordinate_consts

// these typedefs just to make intellisence work
typedef double (*f_distance_between_points_t)(double lat1, double lon1,
                                              double lat2, double lon2);
typedef double (*f_longitude_to_meters_t)(double lon);
typedef double (*f_latitude_to_meters_t)(double lat);
typedef geopoint (*f_meters_to_geopoint_t)(double lon_meters,
                                           double lat_meters);
typedef geopoint (*f_point_ahead_t)(geopoint point, double distance,
                                    double azimuth);
typedef double (*f_azimuth_between_points_t)(double lat1, double lon1,
                                             double lat2, double lon2);

struct coordinates_vptr {
  /// distance_between_points - distance between geopoints in meters
  /// between two geopoints
  /// @lat1 - point1.latitude
  /// @lon1 - point1.longitude
  /// @lat2 - point2.latitude
  /// @lon2 - point2.longitude
  f_distance_between_points_t distance_between_points;

  /// longitude_to_meters - converts longitude to distance from zero point
  /// @lon - longitude
  f_longitude_to_meters_t longitude_to_meters;

  /// latitude_to_meters - converts latitude to distance from zero point
  /// @lat - latitude
  f_latitude_to_meters_t latitude_to_meters;

  /// meters_to_geopoint - converts meters from zero point to geopoint
  /// @lon_meters - longitude meters (see longitude_to_meters)
  f_meters_to_geopoint_t meters_to_geopoint;

  /// point_ahead - calculates next geopoint
  /// @point - start geopoint
  /// @distance - distance in meters
  /// @azimuth - angle between start point, north and destination point
  f_point_ahead_t point_ahead;

  /// azimuth_between_points - azimuth between geopoints in radians
  /// between two geopoints
  /// @lat1 - point1.latitude
  /// @lon1 - point1.longitude
  /// @lat2 - point2.latitude
  /// @lon2 - point2.longitude
  f_distance_between_points_t azimuth_between_points;
};

// todo implement template magic OR migrate to C from C++
coordinates_vptr coord_vptr(void);
coordinates_vptr coord_vptr_hq(void);

#endif
