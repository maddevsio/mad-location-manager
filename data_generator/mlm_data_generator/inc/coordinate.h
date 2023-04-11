#ifndef _COORDINATE_H_
#define _COORDINATE_H_

#include "sensor_data.h"

namespace coordinate_consts {
  const double EARTH_RADIUS = (6371.0 * 1000.0); // meters
  const double ACTUAL_GRAVITY = 9.80665;

  const double majAxis = 6378137.0;
  const double ellipse_coeff = (1.0 / 298.257223563);
  const double minAxis = ((1 - ellipse_coeff) * majAxis);
}; // namespace coordinate_consts
   

struct coordinates_vptr {
  double (*distance_between_points)(double lat1,
                                    double lon1,
                                    double lat2,
                                    double lon2);
  double (*longitude_to_meters)(double lon);
  double (*latitude_to_meters)(double lat);
  gps_location_t (*meters_to_geopoint)(double lon_meters, double lat_meters);
  gps_location_t (*point_ahead)(gps_location_t point, double distance, double azimuth_degrees);
};

// todo implement template magic OR migrate to C from C++
coordinates_vptr coord_vptr(void);
coordinates_vptr coord_vptr_hq(void);

#endif
