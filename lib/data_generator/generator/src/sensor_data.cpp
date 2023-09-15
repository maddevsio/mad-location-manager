#include "sensor_data.h"

#include <stdio.h>

static const char *gps_format = "1: %.9f , %.9f , %.9f , %.9f , %.9f , %9f";
static const char *acc_format = "0: %.9f , %.9f , %.9f , %.9f";

size_t sd_gps_serialize_str(const gps_coordinate &gc,
                            double ts,
                            char buff[],
                            size_t len)
{
  return snprintf(buff,
                  len,
                  gps_format,
                  ts,
                  gc.location.latitude,
                  gc.location.longitude,
                  gc.speed.value,
                  gc.speed.azimuth,
                  gc.speed.accuracy);
}
//////////////////////////////////////////////////////////////

size_t sd_acc_serialize_str(const abs_accelerometer &acc,
                            double ts,
                            char buff[],
                            size_t len)
{
  return snprintf(buff, len, acc_format, ts, acc.x, acc.y, acc.z);
}
//////////////////////////////////////////////////////////////
