#include "SensorControllerTest.h"
#include "SensorController.h"
#include <assert.h>

static void testParse();


void TestSensorController() {
  testParse();
}
//////////////////////////////////////////////////////////////////////////

void testParse() {
  static const char *str1 = "{\"timestamp\":1.482995731281e+09,\"gps_lat\":0,\"gps_lon\":0,\"gps_alt\":0,"
                            "\"pitch\":-3.8990333,\"yaw\":122.288956,\"roll\":1.2419541,\"abs_north_acc\":0.0016001404,"
                            "\"abs_east_acc\":-0.011930285,\"abs_up_acc\":0.002399087,\"vel_north\":0,\"vel_east\":0,"
                            "\"vel_down\":0,\"vel_error\":0,\"altitude_error\":0}";
  static const char *str2 = "{\"timestamp\":1.482995731206e+09,\"gps_lat\":37.948626,\"gps_lon\":-122.0487442,"
                            "\"gps_alt\":22.538999557495117,\"pitch\":-3.5277486,\"yaw\":122.62749,\"roll\":1.4046404,"
                            "\"abs_north_acc\":0.0016441022,\"abs_east_acc\":-0.0171618,\"abs_up_acc\":0.0009460343,"
                            "\"vel_north\":0.02800000086426735,\"vel_east\":0.006000000052154064,"
                            "\"vel_down\":0.054999999701976776,\"vel_error\":0.3449999988079071,"
                            "\"altitude_error\":4.0960001945495605}";
  static const char *str3 = "[]";
  SensorData_t data1, data2, data3;
  assert(SensorControllerParseDataString(str1, &data1));
  assert(SensorControllerParseDataString(str2, &data2));
  assert(!SensorControllerParseDataString(str3, &data3));

  assert(data1.timestamp == 1.482995731281e+09);
  assert(data1.gpsLat == 0.0);
  assert(data1.gpsLon == 0.0);
  assert(data1.gpsAlt == 0.0);
  assert(data1.pitch == -3.8990333);
  assert(data1.yaw == 122.288956);
  assert(data1.roll == 1.2419541);
  assert(data1.absNorthAcc == 0.0016001404);
  assert(data1.absEastAcc == -0.011930285);
  assert(data1.absUpAcc == 0.002399087);
  assert(data1.velNorth == 0.0);
  assert(data1.velEast == 0.0);
  assert(data1.velDown == 0.0);
  assert(data1.velError == 0.0);
  assert(data1.altitudeError == 0.0);

  assert(data2.timestamp == 1.482995731206e+09);
  assert(data2.gpsLat == 37.948626);
  assert(data2.gpsLon == -122.0487442);
  assert(data2.gpsAlt == 22.538999557495117);
  assert(data2.pitch == -3.5277486);
  assert(data2.yaw == 122.62749);
  assert(data2.roll == 1.4046404);
  assert(data2.absNorthAcc == 0.0016441022);
  assert(data2.absEastAcc == -0.0171618);
  assert(data2.absUpAcc == 0.0009460343);
  assert(data2.velNorth == 0.02800000086426735);
  assert(data2.velEast == 0.006000000052154064);
  assert(data2.velDown == 0.054999999701976776);
  assert(data2.velError == 0.3449999988079071);
  assert(data2.altitudeError == 4.0960001945495605);
}
