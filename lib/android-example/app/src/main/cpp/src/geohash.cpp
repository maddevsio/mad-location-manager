#include "geohash.h"

#include <assert.h>

#include <cstdint>

static uint64_t interleave_bits(uint64_t x, uint64_t y);
uint64_t geohash_encode(double lat, double lon, int prec)
{
  union duint64 {
    double dv;
    uint64_t ui;
  };
  duint64 ilat, ilon;
  ilat.dv = lat / 180.0 + 1.5;
  ilon.dv = lon / 360.0 + 1.5;
  ilat.ui >>= 20;
  ilon.ui >>= 20;
  ilat.ui &= 0x00000000ffffffff;
  ilon.ui &= 0x00000000ffffffff;
  return interleave_bits(ilat.ui, ilon.ui) >>
         ((GEOHASH_MAX_PRECISION - prec) * 5);
}
///////////////////////////////////////////////////////

int geohash_cmp(double lon1,
                double lat1,
                double lon2,
                double lat2,
                int precision)
{
  assert(precision >= 1 && precision <= GEOHASH_MAX_PRECISION);
  uint64_t gh1 = geohash_encode(lat1, lon1, precision);
  uint64_t gh2 = geohash_encode(lat2, lon2, precision);
  return (int)(gh1 - gh2);
}
///////////////////////////////////////////////////////

uint64_t interleave_bits(uint64_t x, uint64_t y)
{
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
  //  use pdep instructions if available !!!
  //  return _pdep_u64(x, 0x5555555555555555) | _pdep_u64(y,
  //  0xaaaaaaaaaaaaaaaa);
}
///////////////////////////////////////////////////////
