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
#include <bitset>
//todo add ifdef bmi2
#include <immintrin.h>

typedef struct TestEncodeItem {
  double lat;
  double lon;
  uint32_t precision;
  const char *expected;
} TestEncodeItem_t;

static const char *geohash_str(uint64_t geohash) ;

static void tesnEncodeU64() {
  double lat = 27.988056;
  double lon = 86.925278;
  uint64_t exp = 0xceb7f254240fd612;
  uint64_t act = GeohashEncodeU64(lat, lon, GEOHASH_MAX_PRECISION);
  assert(exp == act);  
  //tuvz4p141zc1
  std::cout << geohash_str(act) << std::endl;
}
///////////////////////////////////////////////////////

#define BASE32_COUNT 32
static const char base32Table[BASE32_COUNT] = {'0', '1', '2', '3', '4', '5', '6', '7',
                                               '8', '9', 'b', 'c', 'd', 'e', 'f', 'g',
                                               'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
                                               's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

const char *geohash_str(uint64_t geohash) {
  static char buff[GEOHASH_MAX_PRECISION+1] = {0};
  char *str = buff + GEOHASH_MAX_PRECISION + 1;
  *str = 0;
  geohash >>= 4; //cause we don't need last 4 bits. that's strange, I thought we don't need first 4 bits %)
  while (geohash){
    *--str = base32Table[geohash & 0x1f];
    geohash >>= 5;
  }
  return str;
}

static void testEncode() {
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
    uint64_t geohash = GeohashEncodeU64(tmp->lat, tmp->lon, tmp->precision);
    const char *geostr = geohash_str(geohash);
    assert(strcmp(geostr, tmp->expected) == 0);
  }

  for (tmp = negTests; tmp->expected; ++tmp) {
    uint64_t geohash = GeohashEncodeU64(tmp->lat, tmp->lon, tmp->precision);
    const char *geostr = geohash_str(geohash);
    assert(strcmp(geostr, tmp->expected) != 0);
  }
}
//////////////////////////////////////////////////////////////////////////

void TestGeohash() {
  tesnEncodeU64();
  testEncode();
}
