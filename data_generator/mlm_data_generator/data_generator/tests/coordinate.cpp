#include "coordinate.h"

#include <gtest/gtest.h>

#include "sensor_data.h"

TEST(coordinates, distanceBetweenPointsTest) {
  coordinates_vptr vptr = coord_vptr_hq();
  // Test data - GeoPoints with known latitude and longitude values
  geopoint point1(53.48095, -2.23743);   // Manchester, UK
  geopoint point2(51.50735, -0.12776);   // London, UK
  geopoint point3(40.71278, -74.00594);  // New York, USA

  // Expected distances between the GeoPoints (calculated using a trusted
  // source)
  double expectedDistance1To2 = 262199.198;   // meters
  double expectedDistance1To3 = 5384336.126;  // meters

  // Calculate distances using the calculateDistance function
  double d12 = vptr.distance_between_points(point1.latitude, point1.longitude,
                                            point2.latitude, point2.longitude);
  double d13 = vptr.distance_between_points(point1.latitude, point1.longitude,
                                            point3.latitude, point3.longitude);
  // Check if the calculated distances are close to the expected distances with
  // a tolerance of 0.01
  EXPECT_NEAR(d12, expectedDistance1To2, 0.01);
  EXPECT_NEAR(d13, expectedDistance1To3, 0.01);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, azimuthBetweenPointsTest) {
  coordinates_vptr vptr = coord_vptr_hq();

  geopoint point1(53.48095, -2.23743);  // Manchester, UK
  geopoint point2(51.50735, -0.12776);  // London, UK
                                        //

  vptr.distance_between_points(point1.latitude, point1.longitude,
                               point2.latitude, point2.longitude);

  vptr.azimuth_between_points(point1.latitude, point1.longitude,
                              point2.latitude, point2.longitude);
}
