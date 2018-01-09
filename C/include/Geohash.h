#ifndef GEOHASH_H
#define GEOHASH_H

#include <stdint.h>

static const int GEOHASH_MAX_PRECISION = 12;
void GeohashEncode(double srcLat,
                   double srcLon,
                   char* geohash,
                   uint8_t precision);

void GeohashDecode(const char* str,
                   double *pLon,
                   double *pLat);

int GeohashComparePoints(double lon1, double lat1,
                         double lon2, double lat2,
                         int precision);

#endif // GEOHASH_H
