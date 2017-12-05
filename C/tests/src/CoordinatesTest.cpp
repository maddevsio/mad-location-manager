#include <assert.h>
#include <math.h>
#include "CoordinatesTest.h"
#include "Coordinates.h"

static void longitudeToMetersTest();
static void latitudeToMetersTest();
static void metersToGeopointTest();

//http://www.onlineconversion.com/map_greatcircle_distance.htm
void longitudeToMetersTest() {
  double eps = 1e-08;
  assert(abs(1373039.2908091505 - abs(LongitudeToMeters(12.348039))) < eps);
  assert(abs(13703046.250524132 - abs(LongitudeToMeters(123.2344556))) < eps);
  assert(abs(13602958.57731845 - abs(LongitudeToMeters(-122.33434553))) < eps);
}
//////////////////////////////////////////////////////////////////////////

void latitudeToMetersTest() {
  double eps = 1e-08;
  assert(abs(4038993.6993554747 - abs(LatitudeToMeters(36.323543))) < eps);
  assert(abs(13974509.760789291 - abs(LatitudeToMeters(234.3242144))) < eps);
  assert(abs(14159832.607369563 - abs(LatitudeToMeters(-127.342434))) < eps);
}
//////////////////////////////////////////////////////////////////////////

void metersToGeopointTest() {
  double eps = 1e-08;
  geopoint_t t;
  t = MetersToGeopoint(1373039.2908091505, 4038993.6993554747);
  assert(abs(t.Latitude - 36.323543) < eps);
  assert(abs(t.Longitude - 12.348039) < eps);
}
//////////////////////////////////////////////////////////////////////////

void TestCoordinates() {
  longitudeToMetersTest();
  latitudeToMetersTest();
  metersToGeopointTest();
}
