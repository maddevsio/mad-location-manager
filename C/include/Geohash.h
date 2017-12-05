#ifndef GEOHASH_H
#define GEOHASH_H

#include <stdint.h>

#define GEOHASH_MAX_PRECISION 12
void GeohashEncode(double srcLat,
                   double srcLon,
                   char* geohash,
                   uint8_t precision);

void GeohashDecode(const char* str,
                   double *pLon,
                   double *pLat);

#endif // GEOHASH_H
