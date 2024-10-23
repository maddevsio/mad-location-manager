#ifndef GEOHASH_H
#define GEOHASH_H

#include <cstdint>

#define GEOHASH_MAX_PRECISION 12

uint64_t geohash_encode(double lat, double lon, int prec);

int geohash_cmp(double lon1,
                double lat1,
                double lon2,
                double lat2,
                int precision);

#endif  // GEOHASH_H
