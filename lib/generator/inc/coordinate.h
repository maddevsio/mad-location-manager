#ifndef _COORDINATE_H_
#define _COORDINATE_H_

#include "sensor_data.h"

namespace coordinate_consts
{
const double earth_radius = 6371.0 * 1000.0;  // meters
// https://en.wikipedia.org/wiki/World_Geodetic_System#WGS_84
const double major_axis = 6378137.0;
const double flattening = 298.257223563;         // f
const double inv_flattening = 1.0 / flattening;  // e - eccentricity
const double minor_axis = (1 - inv_flattening) * major_axis;
};  // namespace coordinate_consts

// these typedefs just to make intellisence work
typedef double (*f_distance_between_points_t)(double lat1,
                                              double lon1,
                                              double lat2,
                                              double lon2);
typedef geopoint (*f_point_ahead_t)(geopoint point,
                                    double distance,
                                    double azimuth);
typedef double (*f_azimuth_between_points_t)(double lat1,
                                             double lon1,
                                             double lat2,
                                             double lon2);

struct coordinates_vptr {
  /// distance_between_points - distance between geopoints in meters
  /// between two geopoints
  /// @lat1 - point1.latitude
  /// @lon1 - point1.longitude
  /// @lat2 - point2.latitude
  /// @lon2 - point2.longitude
  f_distance_between_points_t distance_between_points;

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

coordinates_vptr coord_vptr(void);
coordinates_vptr coord_vptr_hq(void);

#endif
