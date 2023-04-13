#include <gtest/gtest.h>
#include "coordinate.h"

//http://www.onlineconversion.com/map_greatcircle_distance.htm
TEST (coordinates, longitudeToMetersTest) {
  coordinates_vptr vptr = coord_vptr_hq();
  double dd1 = abs(vptr.longitude_to_meters(12.348039));
  // double dd2 = abs(vptr.longitude_to_meters(123.2344556));
  // double dd3 = abs(vptr.longitude_to_meters(-122.33434553));
  ASSERT_DOUBLE_EQ(dd1, 1374577.4137749006);
  // ASSERT_TRUE(abs(13718396.84557247  - dd2) < eps);
  // ASSERT_TRUE(abs(13618197.050922213 - dd3) < eps);
}
//////////////////////////////////////////////////////////////////////////
