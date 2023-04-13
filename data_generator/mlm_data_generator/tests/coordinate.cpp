#include "coordinate.h"
#include "sensor_data.h"
#include <gtest/gtest.h>

// http://www.onlineconversion.com/map_greatcircle_distance.htm
TEST(coordinates, longitudeToMetersTest) {
  coordinates_vptr vptr = coord_vptr_hq();
  double dd1 = abs(vptr.longitude_to_meters(12.348039));
  double dd2 = abs(vptr.longitude_to_meters(123.2344556));
  double dd3 = abs(vptr.longitude_to_meters(-122.33434553));
  ASSERT_DOUBLE_EQ(1374577.4137749006, dd1);
  ASSERT_DOUBLE_EQ(13718396.84557247, dd2);
  ASSERT_DOUBLE_EQ(13618197.050922213, dd3);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, latitudeToMetersTest) {
  coordinates_vptr vptr = coord_vptr_hq();
  double dd1 = abs(vptr.latitude_to_meters(36.323543));
  double dd2 = abs(vptr.latitude_to_meters(234.3242144));
  double dd3 = abs(vptr.latitude_to_meters(-127.342434));
  ASSERT_DOUBLE_EQ(4021443.6498243436, dd1);
  ASSERT_DOUBLE_EQ(6022003.6913477043, dd2);
  ASSERT_DOUBLE_EQ(5836513.2784795808, dd3);
}
//////////////////////////////////////////////////////////////

TEST(coordinates, metersToGeopointTest) {
  coordinates_vptr vptr = coord_vptr_hq();
  gps_location_t t =
      vptr.meters_to_geopoint(1373039.2908091505, 4038993.6993554747);
  ASSERT_DOUBLE_EQ(t.latitude, 36.481699312213955);
  ASSERT_DOUBLE_EQ(t.longitude, 12.33422180630488);
}
//////////////////////////////////////////////////////////////////////////
