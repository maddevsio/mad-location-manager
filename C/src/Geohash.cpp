#include <stdint.h>
#include <assert.h>
#include <stdbool.h>
#include <string.h>
#include "Geohash.h"

//where is 'A'???? oh my god, we lost 'I', 'L', 'O' too. :(
#define BASE32_COUNT 32
static const char base32Table[BASE32_COUNT] = {'0', '1', '2', '3', '4', '5', '6', '7',
                                               '8', '9', 'b', 'c', 'd', 'e', 'f', 'g',
                                               'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
                                               's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
static int searchInBase32Table(char c) {
  int f, l, m;
  f = 0;
  l = BASE32_COUNT;
  while (f < l) {
    m = (f + l) >> 1;
    if (c <= base32Table[m])
      l = m;
    else
      f = m+1;
  }
  return l;
}
//////////////////////////////////////////////////////////////////////////

typedef struct interval {
  double min, max;
} interval_t;

void GeohashEncode(double srcLat,
                   double srcLon,
                   char* geohash,
                   uint8_t precision) {
  interval_t lat = {-90.0, 90.0};
  interval_t lon = {-180.0, 180.0};
  interval_t *ci;
  bool isEven = true;
  double mid, cd;
  int32_t idx = 0; // index into base32 map
  int8_t bit = 0; // each char holds 5 bits

  while (precision) {
    if (isEven) {
      ci = &lon;
      cd = srcLon;
    } else {
      ci = &lat;
      cd = srcLat;
    }

    mid = (ci->min + ci->max) / 2.0;
    idx <<= 1; //idx *= 2
    if (cd >= mid) {
      ci->min = mid;
      idx |= 1; //idx += 1
    } else {
      ci->max = mid;
    }

    isEven = !isEven;

    if (++bit == 5) {
      *geohash++ = base32Table[idx];
      idx = bit = 0;
      --precision;
    }
  }
  *geohash++ = 0;
}
//////////////////////////////////////////////////////////////////////////

void GeohashDecode(const char* str,
                   double *pLon,
                   double *pLat) {
  interval_t latInterval = {-90.0, 90.0};
  interval_t lonInterval = {-180.0, 180.0};
  interval_t *ci;
  bool isEven = true;
  int idx ;
  double mid;

  for (; *str; ++str) {
    idx = searchInBase32Table(*str);
    for (int i = 0; i < 5; ++i) {
      ci = isEven ? &lonInterval : &latInterval;
      mid = (ci->max - ci->min) / 2.0;
      if ((idx << i) & 0x10) {
        ci->min += mid;
      } else {
        ci->max -= mid;
      }
      isEven = !isEven;
    }
  } //for c in str
  *pLat = latInterval.max - ((latInterval.max - latInterval.min) / 2.0);
  *pLon = lonInterval.max - ((lonInterval.max - lonInterval.min) / 2.0);
}
//////////////////////////////////////////////////////////////////////////

int GeohashComparePoints(double lon1, double lat1,
                         double lon2, double lat2,
                         int precision) {
  assert(precision >= 1 && precision <= GEOHASH_MAX_PRECISION);
  static char geohash1[GEOHASH_MAX_PRECISION+1] = {0};
  static char geohash2[GEOHASH_MAX_PRECISION+1] = {0};

  GeohashEncode(lat1, lon1, geohash1, precision);
  GeohashEncode(lat2, lon2, geohash2, precision);
  return memcmp(geohash1, geohash2, precision);
}
