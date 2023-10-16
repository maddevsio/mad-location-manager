#ifndef SENSOR_DATA_H
#define SENSOR_DATA_H

#include <cmath>
#include <cstdint>

#include "commons.h"
/// accelerometer_t - raw accelerometer data (without g compensation)
/// @x - axis X
/// @y - axis Y
/// @z - axis Z
struct accelerometer {
  double x, y, z;
  accelerometer() : x(0.0), y(0.0), z(0.0) {}
  accelerometer(double x, double y, double z) : x(x), y(y), z(z) {}
};

// gyroscope_t - raw gyroscope data
// @x - around axis X
// @y - around axis Y
// @z - around axis Z
struct gyroscope {
  double x, y, z;

  gyroscope() : x(0.0), y(0.0), z(0.0) {}
  gyroscope(double x, double y, double z) : x(x), y(y), z(z) {}
};

/// magnetometer_t - raw magnetometer data
/// @x - axis X
/// @y - axis Y
/// @z - axis Z
struct magnetometer {
  double x, y, z;

  magnetometer() : x(0.0), y(0.0), z(0.0) {}
  magnetometer(double x, double y, double z) : x(x), y(y), z(z) {}
};
//////////////////////////////////////////////////////////////

/// abs_accelerometer_t  - data calculated by quaternion x
/// linear_accelerometer_data or roatation matrix * linear_accelerometer_data
/// vector
/// @x - axis X (longitude)
/// @y - axis Y (latitude)
/// @z - axiz Z (aptitude) zero for now
struct abs_accelerometer {
  double x, y, z;

  abs_accelerometer() : x(0.0), y(0.0), z(0.0) {}
  abs_accelerometer(double x, double y, double z) : x(x), y(y), z(z) {}
  abs_accelerometer(double acc, double azimuth)
  {
    double a_rad = degree_to_rad(azimuth);
    x = acc * cos(a_rad);
    y = acc * sin(a_rad);
    z = 0.0;  // for now
  }

  double azimuth(void) const
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
/// @latitude - latitude (axis Y)
/// @longitude - longitude (axis X)
struct geopoint {
  double latitude;   // 0 .. M_PI
  double longitude;  // 0 .. 2 * M_PI
  double altitude;
  double accuracy;

  geopoint() : latitude(0.0), longitude(0.0), altitude(0.0), accuracy(0.0) {}
  geopoint(double latitude, double longitude, double altitude = 0.)
      : latitude(latitude), longitude(longitude), altitude(altitude)
  {
  }
};
//////////////////////////////////////////////////////////////

/// gps_speed - speed received from GPS
/// @azimuth - in degrees (HDOP in NMEA)
/// @value - speed in m/s
/// @accuracy - ???
struct gps_speed {
  double azimuth;
  double value;
  double accuracy;

  gps_speed() : azimuth(0.0), value(0.0), accuracy(0.0) {}
  gps_speed(double azimuth, double value, double accuracy)
      : azimuth(azimuth), value(value), accuracy(accuracy)
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

  gps_coordinate() : location(), speed() {}
};
//////////////////////////////////////////////////////////////

enum SD_RECORD_TYPE {
  SD_ACCELEROMETER_MEASURED = 0,
  SD_GPS_MEASURED,
  SD_GPS_PREDICTED,  // after kalman.predict() operation
  SD_GPS_CORRECTED,  // after kalman.correct() operation (and received new gps
                     // coordinate)
  SD_UNKNOWN
};
/// sd_record_hdr - header for all sensor data output records
struct sd_record_hdr {
  SD_RECORD_TYPE type;
  double timestamp;
};

size_t sd_gps_serialize_str(const gps_coordinate &gc,
                            SD_RECORD_TYPE rc,
                            double ts,
                            char buff[],
                            size_t len);

size_t sd_acc_serialize_str(const abs_accelerometer &acc,
                            double ts,
                            char buff[],
                            size_t len);

bool sd_gps_deserialize_str(const char *line,
                            sd_record_hdr &hdr,
                            gps_coordinate &gc);

bool sd_acc_deserialize_str(const char *line,
                            sd_record_hdr &hdr,
                            abs_accelerometer &acc);

#endif  // SENSOR_DATA_H
