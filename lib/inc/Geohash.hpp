#ifndef GEOHASH_H
#define GEOHASH_H

#include <stdint.h>

#define GEOHASH_MAX_PRECISION 12

uint64_t geohash_encode(double lat,
                        double lon,
                        int prec);

int geohash_cmp(double lon1,
                double lat1,
                double lon2,
                double lat2,
                int precision);

int geohash_to_str(uint16_t hash,
                   char **dst);

#endif // GEOHASH_H
