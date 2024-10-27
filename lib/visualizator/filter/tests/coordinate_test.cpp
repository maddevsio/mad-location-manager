#include <gtest/gtest.h>

#include <GeographicLib/Geodesic.hpp>
#include <cmath>

#include "sensor_data.h"

TEST(coordinates, distance_between_hardcoded_points_test)
{
  const GeographicLib::Geodesic &geod = GeographicLib::Geodesic::WGS84();
  // Test data - GeoPoints with known latitude and longitude values
  geopoint point1(11.11111, 11.11111);
  geopoint point2(11.11112, 11.11112);

  // Calculate distances using the calculateDistance function
  double d12, d21;
  geod.Inverse(point1.latitude,
               point1.longitude,
               point2.latitude,
               point2.longitude,
               d12);
  geod.Inverse(point2.latitude,
               point2.longitude,
               point1.latitude,
               point1.longitude,
               d21);
  EXPECT_NEAR(d12, d21, 1e-7);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, distance_between_points_test)
{
  const GeographicLib::Geodesic &geod = GeographicLib::Geodesic::WGS84();
  // Test data - GeoPoints with known latitude and longitude values
  geopoint point1(53.48095, -2.23743);   // Manchester, UK
  geopoint point2(51.50735, -0.12776);   // London, UK
  geopoint point3(40.71278, -74.00594);  // New York, USA

  // Expected distances between the GeoPoints (calculated using a trusted
  // source)
  double expectedDistance1To2 = 262199.198;   // meters
  double expectedDistance1To3 = 5384336.126;  // meters

  double d12, d13;
  geod.Inverse(point1.latitude,
               point1.longitude,
               point2.latitude,
               point2.longitude,
               d12);
  geod.Inverse(point1.latitude,
               point1.longitude,
               point3.latitude,
               point3.longitude,
               d13);

  // Check if the calculated distances are close to the expected distances with
  // a tolerance of 0.01
  EXPECT_NEAR(d12, expectedDistance1To2, 0.01);
  EXPECT_NEAR(d13, expectedDistance1To3, 0.01);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, azimuth_between_points_test)
{
  const GeographicLib::Geodesic &geod = GeographicLib::Geodesic::WGS84();

  geopoint point1(53.48095, -2.23743);  // Manchester, UK
  geopoint point2(51.50735, -0.12776);  // London, UK

  double d12, az12, az21;
  geod.Inverse(point1.latitude,
               point1.longitude,
               point2.latitude,
               point2.longitude,
               d12,
               az12,
               az21);
  ASSERT_NEAR(az12, 146.0365943, 1e-7);
  ASSERT_NEAR(d12, 262199.19817, 1e-7);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, distance_point_ahead_test)
{
  const GeographicLib::Geodesic &geod = GeographicLib::Geodesic::WGS84();
  const double distance = 300.;
  const double azimuth = 12.;
  geopoint point1(53.48095, -2.23743);  // Manchester, UK
  geopoint point2;

  geod.Direct(point1.latitude,
              point1.longitude,
              azimuth,
              distance,
              point2.latitude,
              point2.longitude);

  double s, az12, az21;
  geod.Inverse(point1.latitude,
               point1.longitude,
               point2.latitude,
               point2.longitude,
               s,
               az12,
               az21);

  EXPECT_NEAR(s, distance, 1e-7);
  EXPECT_NEAR(az12, azimuth, 1e-7);
}
//////////////////////////////////////////////////////////////
