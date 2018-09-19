#include "Geohash.h"
#include "GeohashTest.h"

#include <string.h>
#include <stdint.h>
#include <assert.h>
#include <stddef.h>
#include <math.h>
#include <stdio.h>
#include <inttypes.h>
#include <iostream>
//todo add ifdef bmi2
#include <immintrin.h>

typedef struct TestEncodeItem {
  double lat;
  double lon;
  uint32_t precision;
  const char *expected;
} TestEncodeItem_t;

static uint64_t interleave(uint64_t x, uint64_t y) {
  x = (x | (x << 16)) & 0x0000ffff0000ffff;
  x = (x | (x << 8)) & 0x00ff00ff00ff00ff;
  x = (x | (x << 4)) & 0x0f0f0f0f0f0f0f0f;
  x = (x | (x << 2)) & 0x3333333333333333;
  x = (x | (x << 1)) & 0x5555555555555555;

  y = (y | (y << 16)) & 0x0000ffff0000ffff;
  y = (y | (y << 8)) & 0x00ff00ff00ff00ff;
  y = (y | (y << 4)) & 0x0f0f0f0f0f0f0f0f;
  y = (y | (y << 2)) & 0x3333333333333333;
  y = (y | (y << 1)) & 0x5555555555555555;

  return x | (y << 1);

  //use pdep instructions
//  return _pdep_u64(x, 0x5555555555555555) | _pdep_u64(y, 0xaaaaaaaaaaaaaaaa);
}
///////////////////////////////////////////////////////

static uint64_t geohash_u64(double lat, double lon) {
  lat = lat/180.0 + 1.5;
  lon = lon/360.0 + 1.5;
  uint64_t ilat = *((uint64_t*)&lat);
  uint64_t ilon = *((uint64_t*)&lon);
  ilat >>= 20;
  ilon >>= 20;
  ilat &= 0x00000000ffffffff;
  ilon &= 0x00000000ffffffff;
  return interleave(ilat, ilon);
}

static void tesnEncodeU64() {
  double lat = 27.988056;
  double lon = 86.925278;
  uint64_t exp = 0xceb7f254240fd612;

  uint64_t act = geohash_u64(lat, lon);
  std::cout << std::hex << exp << std::endl;
  std::cout << std::hex << act << std::endl;
  assert(exp == act);
}
///////////////////////////////////////////////////////

static void testEncode() {
  static char buff[GEOHASH_MAX_PRECISION+1] = {0};
  static TestEncodeItem_t posTests[] = {
    {44.87533558, -64.3057251, 8, "dxfr29mc"},
    {46.76244305, -60.6362915, 8, "f8kfh0y4"},
    {50.79204706, 61.47949219, 8, "v358zn2j"},
    {-82.214234, 114.257834, 9, "n93k21252"},
    {-21.45306863, 137.02148438, 9, "rh1myn84b"},
    {44.87533558, -64.3057251, 7, "dxfr29m"},
    {46.76244305, -60.6362915, 7, "f8kfh0y"},
    {50.79204706, 61.47949219, 7, "v358zn2"},
    {-82.214234, 114.257834, 8, "n93k2125"},
    {-21.45306863, 137.02148438, 8, "rh1myn84"},
    {0.0, 0.0, 0, NULL},
  };

  //random changes in right strings
  static TestEncodeItem_t negTests[] = {
    {44.87533558, -64.3057251, 8, "dxer29mc"},
    {46.76244305, -60.6362915, 8, "f8kgh0y4"},
    {50.79204706, 61.47949219, 8, "v338zn2j"},
    {-82.214234, 114.257834, 9, "n93kgg1252"},
    {-21.45306863, 137.02148438, 9, "rh12myn84b"},
    {44.87533558, -64.3057251, 7, "dxfr2gm"},
    {46.76244305, -60.6362915, 7, "f84fh0y"},
    {50.79204706, 61.47949219, 7, "v318zn2"},
    {-82.214234, 114.257834, 8, "143k2125"},
    {-21.45306863, 137.02148438, 8, "43fmyn84"},
    {0.0, 0.0, 0, NULL},
  };

  TestEncodeItem_t *tmp;

  for (tmp = posTests; tmp->expected; ++tmp) {
    GeohashEncode(tmp->lat, tmp->lon, buff, tmp->precision);
    assert(strcmp(buff, tmp->expected) == 0);
  }

  for (tmp = negTests; tmp->expected; ++tmp) {
    GeohashEncode(tmp->lat, tmp->lon, buff, tmp->precision);
    assert(strcmp(buff, tmp->expected) != 0);
  }
}
//////////////////////////////////////////////////////////////////////////

typedef struct TestDecodeItem {
  double expectedLat;
  double expectedLon;
  const char *geohash;
} TestDecodeItem_t;

static void testDecode() {
  static const double EPS = 1e-03;
  static TestDecodeItem_t posTests[] = {
    {  44.8753 ,  -64.3059 , "dxfr29mc"},
    {  46.7624 ,  -60.6361 , "f8kfh0y4"},
    {  50.7921 ,  61.4797 , "v358zn2j"},
    {  -82.2142 ,  114.258 , "n93k21252"},
    {  -21.4531 ,  137.022 , "rh1myn84b"},
    {  44.8757 ,  -64.3064 , "dxfr29m"},
    {  46.7626 ,  -60.6356 , "f8kfh0y"},
    {  50.7919 ,  61.4802 , "v358zn2"},
    {  -82.2142 ,  114.258 , "n93k2125"},
    {27.988056, 86.925278, "hello, world"},
    {0.0, 0.0, NULL},
  };
  TestDecodeItem_t *tmp;

  for (tmp = posTests; tmp->geohash; ++tmp) {
    double lat, lon;
    double diff;
    GeohashDecode(tmp->geohash, &lon, &lat);
    diff = abs(lat - tmp->expectedLat);
    assert(diff < EPS);
    diff = abs(lon - tmp->expectedLon);
    assert(diff < EPS);
  }
}

void TestGeohash() {
  tesnEncodeU64();
  testEncode();
  testDecode();
}
