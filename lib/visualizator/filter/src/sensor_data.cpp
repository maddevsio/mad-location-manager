#include "sensor_data.h"

#include <assert.h>
#include <stdio.h>

#include <iomanip>
#include <iostream>
#include <sstream>

static const char *HDR_END = ":::";
static const int HDR_LEN = 3;
static const int PRECISION = 12;

static std::string sd_gps_serialize_str(const sd_record &rec);
static std::string sd_acc_serialize_str(const sd_record &rec);
static std::string sd_raw_enu_acc_serialize_str(const sd_record &rec);
static std::string sd_hdr_serialize_str(const sd_record_hdr &hdr);

static sdr_deserialize_error sd_gps_deserialize_str(const std::string &str,
                                                    sd_record &rec);
static sdr_deserialize_error sd_acc_deserialize_str(const std::string &str,
                                                    sd_record &rec);
static sdr_deserialize_error sd_raw_enu_acc_deserialize_str(
    const std::string &str,
    sd_record &rec);
static sdr_deserialize_error sd_hdr_deserialize_str(const std::string &str,
                                                    sd_record_hdr &hdr);

std::string sd_gps_serialize_str(const sd_record &rec)
{
  std::ostringstream out;
  out << std::setprecision(PRECISION) << rec.data.gps.location.latitude << " "
      << rec.data.gps.location.longitude << " "
      << rec.data.gps.location.altitude << " " << rec.data.gps.location.error
      << " " << rec.data.gps.speed.value << " " << rec.data.gps.speed.azimuth
      << " " << rec.data.gps.speed.error;
  return out.str();
}
//////////////////////////////////////////////////////////////

std::string sd_acc_serialize_str(const sd_record &rec)
{
  std::ostringstream out;
  out << std::setprecision(PRECISION) << rec.data.acc.x << " " << rec.data.acc.y
      << " " << rec.data.acc.z;
  return out.str();
}
//////////////////////////////////////////////////////////////

std::string sd_raw_enu_acc_serialize_str(const sd_record &rec)
{
  std::ostringstream out;
  out << rec.data.raw_enu_acc.acc.x << " " << rec.data.raw_enu_acc.acc.y << " "
      << rec.data.raw_enu_acc.acc.z << " " << rec.data.raw_enu_acc.rq.w << " "
      << rec.data.raw_enu_acc.rq.x << " " << rec.data.raw_enu_acc.rq.y << " "
      << rec.data.raw_enu_acc.rq.z;
  return out.str();
}
//////////////////////////////////////////////////////////////

std::string sd_hdr_serialize_str(const sd_record_hdr &hdr)
{
  std::ostringstream out;
  out << std::setprecision(PRECISION) << hdr.type << " " << hdr.timestamp
      << HDR_END;
  return out.str();
}
//////////////////////////////////////////////////////////////

sdr_deserialize_error sd_gps_deserialize_str(const std::string &str,
                                             sd_record &rec)
{
  const char *gps_format_in = "%lf %lf %lf %lf %lf %lf %lf";
  int matched = sscanf(str.c_str(),
                       gps_format_in,
                       &rec.data.gps.location.latitude,
                       &rec.data.gps.location.longitude,
                       &rec.data.gps.location.altitude,
                       &rec.data.gps.location.error,
                       &rec.data.gps.speed.value,
                       &rec.data.gps.speed.azimuth,
                       &rec.data.gps.speed.error);
  return matched == 7 ? SDRDE_SUCCESS : SDRDE_UNEXPECTED_FMT;
}
//////////////////////////////////////////////////////////////

sdr_deserialize_error sd_acc_deserialize_str(const std::string &str,
                                             sd_record &rec)
{
  const char *acc_format_in = "%lf %lf %lf";
  int matched = sscanf(str.c_str(),
                       acc_format_in,
                       &rec.data.acc.x,
                       &rec.data.acc.y,
                       &rec.data.acc.z);
  return matched == 3 ? SDRDE_SUCCESS : SDRDE_UNEXPECTED_FMT;
}
//////////////////////////////////////////////////////////////

sdr_deserialize_error sd_raw_enu_acc_deserialize_str(const std::string &str,
                                                     sd_record &rec)
{
  const char *fmt = "%lf %lf %lf %lf %lf %lf %lf";
  int matched = sscanf(str.c_str(),
                       fmt,
                       &rec.data.raw_enu_acc.acc.x,
                       &rec.data.raw_enu_acc.acc.y,
                       &rec.data.raw_enu_acc.acc.z,
                       &rec.data.raw_enu_acc.rq.w,
                       &rec.data.raw_enu_acc.rq.x,
                       &rec.data.raw_enu_acc.rq.y,
                       &rec.data.raw_enu_acc.rq.z);
  return matched == 7 ? SDRDE_SUCCESS : SDRDE_UNEXPECTED_FMT;
}
//////////////////////////////////////////////////////////////

sdr_deserialize_error sd_hdr_deserialize_str(const std::string &str,
                                             sd_record_hdr &hdr)
{
  const char *hdr_format_in = "%d %lf";
  int matched = sscanf(str.c_str(), hdr_format_in, &hdr.type, &hdr.timestamp);
  return matched == 2 ? SDRDE_SUCCESS : SDRDE_UNEXPECTED_FMT;
}
//////////////////////////////////////////////////////////////

// see sensor_data_type enum in sensor_data.h
static std::string (*pf_serializers[])(const sd_record &) = {
    sd_acc_serialize_str,
    sd_acc_serialize_str,
    sd_gps_serialize_str,
    sd_gps_serialize_str,
    sd_gps_serialize_str,
    sd_raw_enu_acc_serialize_str,
};

static sdr_deserialize_error (*pf_deserializers[])(const std::string &,
                                                   sd_record &) = {
    sd_acc_deserialize_str,
    sd_acc_deserialize_str,
    sd_gps_deserialize_str,
    sd_gps_deserialize_str,
    sd_gps_deserialize_str,
    sd_raw_enu_acc_deserialize_str,
};
//////////////////////////////////////////////////////////////

static const sensor_data_record_type supported_hdrs[] = {SD_ACC_ENU_SET,
                                                         SD_ACC_ENU_GENERATED,
                                                         SD_GPS_SET,
                                                         SD_GPS_FILTERED,
                                                         SD_GPS_GENERATED,
                                                         SD_RAW_ENU_ACC,
                                                         SD_UNKNOWN};

std::string sdr_serialize_str(const sd_record &rec)
{
  std::string res = sd_hdr_serialize_str(rec.hdr);
  for (int i = 0; supported_hdrs[i] != SD_UNKNOWN; ++i) {
    if (supported_hdrs[i] != rec.hdr.type)
      continue;
    res.append(pf_serializers[rec.hdr.type](rec));
    return res;
  }

  std::cerr << "unsupported record type. failed to serialize record with type: "
            << rec.hdr.type << std::endl;
  return "";
}
//////////////////////////////////////////////////////////////

sdr_deserialize_error sdr_deserialize_str(const std::string &str,
                                          sd_record &rec)
{
  sdr_deserialize_error res = SDRDE_UNDEFINED;
  size_t hdr_end_idx = str.find(HDR_END);
  if (hdr_end_idx == std::string::npos) {
    std::cerr << "wrong header separator format\n";
    return SDRDE_WRONG_HDR_SEPARATOR;
  }

  if ((res = sd_hdr_deserialize_str(str, rec.hdr))) {
    std::cerr << "failed to deserialize hdr from: " << str << std::endl;
    return res;
  }

  for (int i = 0; supported_hdrs[i] != SD_UNKNOWN; ++i) {
    if (supported_hdrs[i] != rec.hdr.type)
      continue;
    return pf_deserializers[rec.hdr.type](str.substr(hdr_end_idx + HDR_LEN),
                                          rec);
  }

  std::cerr << "unsupported record type. failed to deserialize record from: "
            << str << std::endl;
  return SDRDE_UNSUPPORTED;
}
//////////////////////////////////////////////////////////////
