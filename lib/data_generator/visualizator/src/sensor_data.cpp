#include "sensor_data.h"

#include <assert.h>
#include <stdio.h>

static const char *gps_format_out =
    "%d: %.9lf , %.9lf , %.9lf , %.9lf , %.9lf , %.9lf";
static const char *gps_format_in = "%d: %lf , %lf , %lf , %lf , %lf , %lf";

static const char *acc_format_out = "%d: %.9lf , %.9lf , %.9lf , %.9lf";
static const char *acc_format_in = "%d: %lf , %lf , %lf , %lf";

size_t sd_gps_serialize_str(const gps_coordinate &gc,
                            SD_RECORD_TYPE rc,
                            double ts,
                            char buff[],
                            size_t len)
{
  assert(rc == SD_GPS_CORRECTED || rc == SD_GPS_NOISED ||
         rc == SD_GPS_MEASURED);

  // "%d: %.9f , %.9f , %.9f , %.9f , %.9f , %9f";
  return snprintf(buff,
                  len,
                  gps_format_out,
                  rc,
                  ts,
                  gc.location.latitude,
                  gc.location.longitude,
                  gc.speed.value,
                  gc.speed.azimuth,
                  gc.speed.accuracy);
}
//////////////////////////////////////////////////////////////

size_t sd_acc_serialize_str(const abs_accelerometer &acc,
                            SD_RECORD_TYPE rc,
                            double ts,
                            char buff[],
                            size_t len)
{
  assert(rc == SD_ACCELEROMETER_MEASURED || rc == SD_ACCELEROMETER_NOISED);

  // "%d: %.9f , %.9f , %.9f , %.9f"
  return snprintf(buff, len, acc_format_out, rc, ts, acc.x, acc.y, acc.z);
}
//////////////////////////////////////////////////////////////

bool sd_gps_deserialize_str(const char *line,
                            sd_record_hdr &hdr,
                            gps_coordinate &gc)
{
  // "%d: %.9f , %.9f , %.9f , %.9f , %.9f , %9f";
  int matched = sscanf(line,
                       gps_format_in,
                       &hdr.type,
                       &hdr.timestamp,
                       &gc.location.latitude,
                       &gc.location.longitude,
                       &gc.speed.value,
                       &gc.speed.azimuth,
                       &gc.speed.accuracy);
  SD_RECORD_TYPE possible_types[] = {SD_GPS_MEASURED,
                                     SD_GPS_NOISED,
                                     SD_GPS_CORRECTED,
                                     SD_UNKNOWN};
  bool is_type_correct = false;
  for (SD_RECORD_TYPE *ppt = possible_types;
       !is_type_correct && *ppt != SD_UNKNOWN;
       ++ppt) {
    is_type_correct = hdr.type == *ppt;
  }
  return matched == 6 && is_type_correct;
}
//////////////////////////////////////////////////////////////

bool sd_acc_deserialize_str(const char *line,
                            sd_record_hdr &hdr,
                            abs_accelerometer &acc)
{
  // "%d: %.9f , %.9f , %.9f , %.9f"
  int matched = sscanf(line,
                       acc_format_in,
                       &hdr.type,
                       &hdr.timestamp,
                       &acc.x,
                       &acc.y,
                       &acc.z);

  SD_RECORD_TYPE possible_types[] = {SD_ACCELEROMETER_NOISED,
                                     SD_ACCELEROMETER_MEASURED,
                                     SD_UNKNOWN};
  bool is_type_correct = false;
  for (SD_RECORD_TYPE *ppt = possible_types;
       !is_type_correct && *ppt != SD_UNKNOWN;
       ++ppt) {
    is_type_correct = hdr.type == *ppt;
  }
  return matched == 4 && is_type_correct;
}
//////////////////////////////////////////////////////////////
