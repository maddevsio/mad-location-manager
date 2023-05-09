#include "coordinate.h"
#include "sensor_data.h"
#include <gtest/gtest.h>

// http://www.onlineconversion.com/map_greatcircle_distance.htm
TEST(coordinates, longitudeToMetersHQTest) {
  coordinates_vptr vptr = coord_vptr_hq();
  double dd1 = abs(vptr.longitude_to_meters(12.348039));
  double dd2 = abs(vptr.longitude_to_meters(123.2344556));
  double dd3 = abs(vptr.longitude_to_meters(-122.33434553));
  ASSERT_DOUBLE_EQ(1374577.4137749006, dd1);
  ASSERT_DOUBLE_EQ(13718396.84557247, dd2);
  ASSERT_DOUBLE_EQ(13618197.050922213, dd3);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, latitudeToMetersHQTest) {
  coordinates_vptr vptr = coord_vptr_hq();
  double dd1 = abs(vptr.latitude_to_meters(36.323543));
  double dd2 = abs(vptr.latitude_to_meters(234.3242144));
  double dd3 = abs(vptr.latitude_to_meters(-127.342434));
  ASSERT_DOUBLE_EQ(4021443.6498243436, dd1);
  ASSERT_DOUBLE_EQ(6022003.6913477043, dd2);
  ASSERT_DOUBLE_EQ(5836513.2784795808, dd3);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, metersToGeopointHQTest) {
  coordinates_vptr vptr = coord_vptr_hq();
  geopoint t = vptr.meters_to_geopoint(1373039.2908091505, 4038993.6993554747);
  ASSERT_DOUBLE_EQ(t.latitude, 36.481699312213955);
  ASSERT_DOUBLE_EQ(t.longitude, 12.33422180630488);
}
//////////////////////////////////////////////////////////////////////////

TEST(coordinates, longitudeToMetersLQTest) {
  coordinates_vptr vptr = coord_vptr();
  double dd1 = abs(vptr.longitude_to_meters(12.348039));
  double dd2 = abs(vptr.longitude_to_meters(123.2344556));
  double dd3 = abs(vptr.longitude_to_meters(-122.33434553));

  ASSERT_DOUBLE_EQ(1373039.2908091506, dd1);
  ASSERT_DOUBLE_EQ(13703046.250524132, dd2);
  ASSERT_DOUBLE_EQ(13602958.577318452, dd3);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, latitudeToMetersLQTest) {
  coordinates_vptr vptr = coord_vptr();
  double dd1 = abs(vptr.latitude_to_meters(36.323543));
  double dd2 = abs(vptr.latitude_to_meters(234.3242144));
  double dd3 = abs(vptr.latitude_to_meters(-127.342434));

  ASSERT_DOUBLE_EQ(4038993.6993554751, dd1);
  ASSERT_DOUBLE_EQ(13974509.760789292, dd2);
  ASSERT_DOUBLE_EQ(14159832.607369564, dd3);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, metersToGeopointLQTest) {
  coordinates_vptr vptr = coord_vptr();
  geopoint t = vptr.meters_to_geopoint(1373039.2908091505, 4038993.6993554747);
  ASSERT_DOUBLE_EQ(t.latitude, 36.323542999999994);
  ASSERT_DOUBLE_EQ(t.longitude, 12.348039000000002);
}
//////////////////////////////////////////////////////////////////////////

TEST(coordinates, distanceBetweenPointsTest) {
  coordinates_vptr vptr = coord_vptr_hq();
  // Test data - GeoPoints with known latitude and longitude values
  geopoint point1(53.48095, -2.23743);  // Manchester, UK
  geopoint point2(51.50735, -0.12776);  // London, UK
  geopoint point3(40.71278, -74.00594); // New York, USA

  // Expected distances between the GeoPoints (calculated using a trusted
  // source)
  double expectedDistance1To2 = 262199.198;  // meters
  double expectedDistance1To3 = 5384336.126; // meters

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

  geopoint point1(53.48095, -2.23743); // Manchester, UK
  geopoint point2(51.50735, -0.12776); // London, UK
                                       //

  vptr.distance_between_points(point1.latitude, point1.longitude,
                               point2.latitude, point2.longitude);

  vptr.azimuth_between_points(point1.latitude, point1.longitude,
                              point2.latitude, point2.longitude);
}
