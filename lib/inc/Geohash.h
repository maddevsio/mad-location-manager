#ifndef GEOHASH_H
#define GEOHASH_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

#define GEOHASH_MAX_PRECISION 12

uint64_t GeohashEncodeU64(double lat,
                          double lon,
                          int prec);

int GeohashComparePointsU64(double lon1,
                            double lat1,
                            double lon2,
                            double lat2,
                            int precision);


#ifdef __cplusplus
}
#endif // extern "C"
#endif // GEOHASH_H
