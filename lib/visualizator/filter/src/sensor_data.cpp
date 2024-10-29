#include "sensor_data.h"

#include <assert.h>
#include <stdio.h>

#include <iostream>
#include <sstream>

static const char *HDR_END = ":::";
static const int HDR_LEN = 3;

static std::string sd_gps_serialize_str(const sd_record &rec);
static std::string sd_acc_abs_serialize_str(const sd_record &rec);
static std::string sd_hdr_serialize_str(const sd_record_hdr &hdr);

static int sd_gps_deserialize_str(const std::string &str, sd_record &rec);
static int sd_acc_abs_deserialize_str(const std::string &str, sd_record &rec);
static int sd_hdr_deserialize_str(const std::string &str, sd_record_hdr &hdr);

std::string sd_gps_serialize_str(const sd_record &rec)
{
  std::ostringstream out;
  out << rec.data.gps.location.latitude << " "
      << rec.data.gps.location.longitude << " " << rec.data.gps.speed.value
      << " " << rec.data.gps.speed.azimuth << " "
      << rec.data.gps.speed.accuracy;
  return out.str();
}
//////////////////////////////////////////////////////////////

std::string sd_acc_abs_serialize_str(const sd_record &rec)
{
  std::ostringstream out;
  out << rec.data.acc.x << " " << rec.data.acc.y << " " << rec.data.acc.z;
  return out.str();
}
//////////////////////////////////////////////////////////////

int sd_gps_deserialize_str(const std::string &str, sd_record &rec)
{
  const char *gps_format_in = "%lf %lf %lf %lf %lf";
  int matched = sscanf(str.c_str(),
                       gps_format_in,
                       &rec.data.gps.location.latitude,
                       &rec.data.gps.location.longitude,
                       &rec.data.gps.speed.value,
                       &rec.data.gps.speed.azimuth,
                       &rec.data.gps.speed.accuracy);
  return matched == 5 ? 0 : 1;
}
//////////////////////////////////////////////////////////////

int sd_acc_abs_deserialize_str(const std::string &str, sd_record &rec)
{
  const char *acc_format_in = "%lf %lf %lf";
  int matched = sscanf(str.c_str(),
                       acc_format_in,
                       &rec.data.acc.x,
                       &rec.data.acc.y,
                       &rec.data.acc.z);
  return matched == 3 ? 0 : 1;
}
//////////////////////////////////////////////////////////////

std::string sd_hdr_serialize_str(const sd_record_hdr &hdr)
{
  std::ostringstream out;
  out << hdr.type << " " << hdr.timestamp << HDR_END;
  return out.str();
}
//////////////////////////////////////////////////////////////

int sd_hdr_deserialize_str(const std::string &str, sd_record_hdr &hdr)
{
  const char *hdr_format_in = "%d %lf";
  int matched = sscanf(str.c_str(), hdr_format_in, &hdr.type, &hdr.timestamp);
  return matched == 2 ? 0 : 1;
}
//////////////////////////////////////////////////////////////

// see sensor_data_type enum in sensor_data.h
static std::string (*pf_serializers[])(const sd_record &) = {
    sd_acc_abs_serialize_str,
    sd_acc_abs_serialize_str,
    sd_gps_serialize_str,
    sd_gps_serialize_str,
    sd_gps_serialize_str,
};

static int (*pf_deserializers[])(const std::string &, sd_record &) = {
    sd_acc_abs_deserialize_str,
    sd_acc_abs_deserialize_str,
    sd_gps_deserialize_str,
    sd_gps_deserialize_str,
    sd_gps_deserialize_str,
};
//////////////////////////////////////////////////////////////

std::string sdr_serialize_str(const sd_record &rec)
{
  std::string res = sd_hdr_serialize_str(rec.hdr);
  res.append(pf_serializers[rec.hdr.type](rec));
  return res;
}
//////////////////////////////////////////////////////////////

int sdr_deserialize_str(const std::string &str, sd_record &rec)
{
  size_t hdr_end_idx = str.find(HDR_END);
  if (hdr_end_idx == std::string::npos) {
    std::cerr << "wrong header format\n";
    return 1;
  }

  sd_record_hdr hdr;
  if (sd_hdr_deserialize_str(str, hdr)) {
    std::cerr << "failed to deserialize hdr from: " << str << std::endl;
    return 2;
  }

  rec.hdr = hdr;
  const sensor_data_record_type supported_hdrs[] = {SD_ACC_ABS_MEASURED,
                                                    SD_ACC_ABS_NOISED,
                                                    SD_GPS_MEASURED,
                                                    SD_GPS_CORRECTED,
                                                    SD_GPS_NOISED,
                                                    SD_UNKNOWN};
  for (int i = 0; supported_hdrs[i] != SD_UNKNOWN; ++i) {
    if (supported_hdrs[i] != hdr.type)
      continue;
    return pf_deserializers[hdr.type](str.substr(hdr_end_idx + HDR_LEN), rec);
  }

  std::cerr << "unsupported record type. failed to deserialize record from: "
            << str << std::endl;
  return 0;
}
//////////////////////////////////////////////////////////////
