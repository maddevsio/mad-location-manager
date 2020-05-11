#include <gtest/gtest.h>
#include <math.h>
#include "Coordinates.h"


//http://www.onlineconversion.com/map_greatcircle_distance.htm
TEST (coordinates, longitudeToMetersTest) {
  double eps = 1e-08;
  double dd1 = abs(CoordLongitudeToMeters(12.348039));
  double dd2 = abs(CoordLongitudeToMeters(123.2344556));
  double dd3 = abs(CoordLongitudeToMeters(-122.33434553));
#if COORDINATES_HIGH_ACCURACY==1
  ASSERT_TRUE(abs(1374577.4137749006 - dd1) < eps);
  ASSERT_TRUE(abs(13718396.84557247  - dd2) < eps);
  ASSERT_TRUE(abs(13618197.050922213 - dd3) < eps);
#else  
  ASSERT_TRUE(abs(1373039.2908091506 - dd1) < eps);
  ASSERT_TRUE(abs(13703046.250524132  - dd2) < eps);
  ASSERT_TRUE(abs(13602958.577318452 - dd3) < eps);
#endif
}
//////////////////////////////////////////////////////////////////////////

TEST (coordinates, latitudeToMetersTest) {
  double eps = 1e-08;
  double dd1 = abs(CoordLatitudeToMeters(36.323543));
  double dd2 = abs(CoordLatitudeToMeters(234.3242144));
  double dd3 = abs(CoordLatitudeToMeters(-127.342434));
#if COORDINATES_HIGH_ACCURACY==1
  ASSERT_TRUE(abs(4021443.6498243436 - dd1) < eps);
  ASSERT_TRUE(abs(6022003.6913477043 - dd2) < eps);
  ASSERT_TRUE(abs(5836513.2784795808 - dd3) < eps);
#else
  ASSERT_TRUE(abs(4038993.6993554751 - dd1) < eps);
  ASSERT_TRUE(abs(13974509.760789292 - dd2) < eps);
  ASSERT_TRUE(abs(14159832.607369564 - dd3) < eps);
#endif
}
//////////////////////////////////////////////////////////////////////////

TEST (coordinates, metersToGeopointTest) {
  double eps = 1e-08;
  geopoint_t t;
  t = CoordMetersToGeopoint(1373039.2908091505, 4038993.6993554747);
#if COORDINATES_HIGH_ACCURACY==1
  ASSERT_TRUE(abs(t.Latitude - 36.481699312213955) < eps);
  ASSERT_TRUE(abs(t.Longitude - 12.33422180630488) < eps);
#else
  ASSERT_TRUE(abs(t.Latitude - 36.323542999999994) < eps);
  ASSERT_TRUE(abs(t.Longitude - 12.348039000000002) < eps);
#endif
}
//////////////////////////////////////////////////////////////////////////
