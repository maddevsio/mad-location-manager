#include "sensor_data.h"

#include <stdio.h>

size_t sd_gps_serialize_str(const gps_coordinate &gc,
                            double ts,
                            char buff[],
                            size_t len)
{
  return snprintf(buff,
                  len,
                  "1: %.9f , %.9f , %.9f , %.9f , %.9f , %9f\n",
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
  return snprintf(buff,
                  len,
                  "0: %.9f , %.9f , %.9f , %.9f\n",
                  ts,
                  acc.x,
                  acc.y,
                  acc.z);
}
//////////////////////////////////////////////////////////////
