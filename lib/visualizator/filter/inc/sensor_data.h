#ifndef SENSOR_DATA_H
#define SENSOR_DATA_H

#include <cmath>
#include <string>

#include "commons.h"

/// accelerometer - raw accelerometer data (without g compensation)
/// @x - axis X
/// @y - axis Y
/// @z - axis Z
struct accelerometer {
  double x, y, z;
  accelerometer() : x(0.0), y(0.0), z(0.0) {}
  accelerometer(double x, double y, double z) : x(x), y(y), z(z) {}
};

// gyroscope - raw gyroscope data
// @x - around axis X
// @y - around axis Y
// @z - around axis Z
struct gyroscope {
  double x, y, z;
  gyroscope() : x(0.0), y(0.0), z(0.0) {}
  gyroscope(double x, double y, double z) : x(x), y(y), z(z) {}
};

/// magnetometer - raw magnetometer data
/// @x - axis X
/// @y - axis Y
/// @z - axis Z
struct magnetometer {
  double x, y, z;
  magnetometer() : x(0.0), y(0.0), z(0.0) {}
  magnetometer(double x, double y, double z) : x(x), y(y), z(z) {}
};
//////////////////////////////////////////////////////////////

/// enu_accelerometer - acceleration in ENU coordinates (east, north, up)
/// @x - axis X (longitude/east)
/// @y - axis Y (latitude/north)
/// @z - axiz Z (altitude/up)
struct enu_accelerometer {
  double x, y, z;
  enu_accelerometer() : x(0.0), y(0.0), z(0.0) {}
  enu_accelerometer(double x, double y, double z) : x(x), y(y), z(z) {}
  enu_accelerometer(double acc, double azimuth)
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

/// linear_accelerometer - acceleration along each device axis, not including
/// gravity. All values have units of m/s^2
struct linear_accelerometer {
  double x, y, z;
  linear_accelerometer() : x(0.), y(0.), z(0.) {};
  linear_accelerometer(double x, double y, double z) : x(x), y(y), z(z) {};
};
//////////////////////////////////////////////////////////////

/// rotation_quaternion - normalized quaternion in form [w, x, y, z] describing
/// device orientation in ENU coordinate system
struct rotation_quaternion {
  double w, x, y, z;
  rotation_quaternion() : w(0.), x(0.), y(0.), z(0.) {};
  rotation_quaternion(double w, double x, double y, double z)
      : w(w), x(x), y(y), z(z) {};
};
//////////////////////////////////////////////////////////////

struct raw_enu_accelerometer {
  linear_accelerometer acc;
  rotation_quaternion rq;
  raw_enu_accelerometer() : acc(), rq() {};
  raw_enu_accelerometer(const linear_accelerometer &acc,
                        const rotation_quaternion &rq)
      : acc(acc), rq(rq) {};
  raw_enu_accelerometer(double acc_x,
                        double acc_y,
                        double acc_z,
                        double q_w,
                        double q_x,
                        double q_y,
                        double q_z)
      : acc(acc_x, acc_y, acc_z), rq(q_w, q_x, q_y, q_z) {};
};
//////////////////////////////////////////////////////////////

/// geopoint - part of gps coordinate representing location
/// @latitude - latitude (axis Y) - 0 .. M_PI
/// @longitude - longitude (axis X) - 0 .. 2 * M_PI
/// @altitude - altitude (axis Z)
/// @error - error in meters (distance from real point)
struct geopoint {
  double latitude;   // 0 .. M_PI
  double longitude;  // 0 .. 2 * M_PI
  double altitude;
  double error;

  geopoint() : latitude(0.), longitude(0.), altitude(0.), error(1e-6) {};
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

/// gps_speed - part of gps coordinate representing speed
/// @azimuth - in degrees (HDOP in NMEA)
/// @value - speed in m/s
/// @error - the estimated speed error in meters per second of this
struct gps_speed {
  double value;
  double azimuth;
  double error;

  gps_speed() : value(0.0), azimuth(0.0), error(0.0) {}
  gps_speed(double value, double azimuth, double error)
      : value(value), azimuth(azimuth), error(error)
  {
  }
};
//////////////////////////////////////////////////////////////

/// gps_coordinate - consists of 2 independent parts : location and speed
/// @location - see @gps_location
/// @speed - see @gps_speed
struct gps_coordinate {
  geopoint location;
  gps_speed speed;

  gps_coordinate() : location(), speed() {}
};
//////////////////////////////////////////////////////////////

enum sensor_data_record_type {
  // ACC_ENU_SET - calculated ENU acceleration based on GPS_SET points
  SD_ACC_ENU_SET = 0,
  // ACC_ENU_GENERATED - with noise OR from real device
  SD_ACC_ENU_GENERATED = 1,
  // GPS_SET manually by user in visualizer
  SD_GPS_SET = 2,
  // GPS_FILTERED coordinates after Kalman filter applied
  SD_GPS_FILTERED = 3,
  // GPS_GENERATED coordinates with noise
  SD_GPS_GENERATED = 4,
  // RAW_ENU_ACC - combination of data from linear accelerometer and rotation
  // vector sensors
  SD_RAW_ENU_ACC = 5,
  SD_UNKNOWN
};

/// sd_record_hdr - header for all sensor data output records
struct sd_record_hdr {
  sensor_data_record_type type;
  double timestamp;

  sd_record_hdr() : type(SD_UNKNOWN), timestamp(0.) {};
  sd_record_hdr(sensor_data_record_type type, double ts)
      : type(type), timestamp(ts)
  {
  }
};
//////////////////////////////////////////////////////////////

struct sd_record {
  sd_record_hdr hdr;
  union data_t {
    data_t() {};
    enu_accelerometer acc;
    gps_coordinate gps;
    raw_enu_accelerometer raw_enu_acc;
  } data;

  sd_record() = default;
  sd_record(sd_record_hdr hdr, enu_accelerometer acc) : hdr(hdr)
  {
    data.acc = acc;
  }
  sd_record(sd_record_hdr hdr, gps_coordinate gps) : hdr(hdr)
  {
    data.gps = gps;
  }
};
//////////////////////////////////////////////////////////////

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
