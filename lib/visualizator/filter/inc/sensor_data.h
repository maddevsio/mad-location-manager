#ifndef SENSOR_DATA_H
#define SENSOR_DATA_H

#include <cmath>
#include <string>

#include "commons.h"
/// accelerometer_t - raw accelerometer data (without g compensation)
/// @x - axis X
/// @y - axis Y
/// @z - axis Z
struct accelerometer {
  double x, y, z;
  accelerometer() = default;  //: x(0.0), y(0.0), z(0.0) {}
  accelerometer(double x, double y, double z) : x(x), y(y), z(z) {}
};

// gyroscope_t - raw gyroscope data
// @x - around axis X
// @y - around axis Y
// @z - around axis Z
struct gyroscope {
  double x, y, z;

  gyroscope() = default;  // : x(0.0), y(0.0), z(0.0) {}
  gyroscope(double x, double y, double z) : x(x), y(y), z(z) {}
};

/// magnetometer_t - raw magnetometer data
/// @x - axis X
/// @y - axis Y
/// @z - axis Z
struct magnetometer {
  double x, y, z;

  magnetometer() = default;  // : x(0.0), y(0.0), z(0.0) {}
  magnetometer(double x, double y, double z) : x(x), y(y), z(z) {}
};
//////////////////////////////////////////////////////////////

/// abs_accelerometer_t  - data calculated by quaternion x
/// linear_accelerometer_data or roatation matrix * linear_accelerometer_data
/// vector
/// @x - axis X (longitude)
/// @y - axis Y (latitude)
/// @z - axiz Z (altitude) zero for now
struct abs_accelerometer {
  double x, y, z;

  abs_accelerometer() = default;  // : x(0.0), y(0.0), z(0.0) {}
  abs_accelerometer(double x, double y, double z) : x(x), y(y), z(z) {}
  abs_accelerometer(double acc, double azimuth)
  {
    double a_rad = degree_to_rad(azimuth);
    x = acc * cos(a_rad);
    y = acc * sin(a_rad);
    z = 0.0;  // for now
  }

  double cartezian_angle(void) const
  {
    return rad_to_degree(atan2(y, x));
  }
  double acceleration(void) const
  {
    return sqrt(x * x + y * y);
  }
};
//////////////////////////////////////////////////////////////

/// geopoint - geopoint gps_coordinate
/// @latitude - latitude (axis Y) - 0 .. M_PI
/// @longitude - longitude (axis X) - 0 .. 2 * M_PI
/// @altitude - altitude (axis Z)
/// @error - error in meters (distance from real point)
/// location
struct geopoint {
  double latitude;   // 0 .. M_PI
  double longitude;  // 0 .. 2 * M_PI
  double altitude;
  double error;

  geopoint() = default;  // : latitude(0.0), longitude(0.0), altitude(0.0),
                         // accuracy(0.0) {}
  geopoint(double latitude,
           double longitude,
           double altitude = 0.,
           double error = 1e-6)
      : latitude(latitude),
        longitude(longitude),
        altitude(altitude),
        error(error)
  {
  }
};
//////////////////////////////////////////////////////////////

/// gps_speed - speed received from GPS
/// @azimuth - in degrees (HDOP in NMEA)
/// @value - speed in m/s
/// @error - the estimated speed error in meters per second of this
/// location
struct gps_speed {
  double azimuth;
  double value;
  double error;

  gps_speed() = default;  // : azimuth(0.0), value(0.0), accuracy(0.0) {}
  gps_speed(double azimuth, double value, double error)
      : azimuth(azimuth), value(value), error(error)
  {
  }
};
//////////////////////////////////////////////////////////////

/// gps_coordinate - consists of 2 independent parts : location and speed
/// @location - see gps_location_t
/// @speed - see gps_speed_t
struct gps_coordinate {
  geopoint location;
  gps_speed speed;

  gps_coordinate() = default;  //: location(), speed() {}
};
//////////////////////////////////////////////////////////////

enum sensor_data_record_type {
  SD_ACC_ABS_SET = 0,
  SD_ACC_ABS_GENERATED,
  SD_GPS_SET,
  SD_GPS_FILTERED,
  SD_GPS_GENERATED,
  // TODO add accelerometer/gyroscope/magnetometer too
  SD_UNKNOWN
};

/// sd_record_hdr - header for all sensor data output records
struct sd_record_hdr {
  sensor_data_record_type type;
  double timestamp;

  sd_record_hdr() = default;
  sd_record_hdr(sensor_data_record_type type, double ts)
      : type(type), timestamp(ts)
  {
  }
};

struct sd_record {
  sd_record_hdr hdr;
  union {
    abs_accelerometer acc;
    gps_coordinate gps;
  } data;

  sd_record() = default;
  sd_record(sd_record_hdr hdr, abs_accelerometer acc) : hdr(hdr)
  {
    data.acc = acc;
  }
  sd_record(sd_record_hdr hdr, gps_coordinate gps) : hdr(hdr)
  {
    data.gps = gps;
  }
};

std::string sdr_serialize_str(const sd_record &rec);

enum sdr_deserialize_error {
  SDRDE_SUCCESS = 0,
  SDRDE_WRONG_HDR_SEPARATOR = 1,
  SDRDE_UNEXPECTED_FMT = 2,
  SDRDE_UNSUPPORTED = 3,
  SDRDE_UNDEFINED
};
sdr_deserialize_error sdr_deserialize_str(const std::string &str,
                                          sd_record &rec);

#endif  // SENSOR_DATA_H
