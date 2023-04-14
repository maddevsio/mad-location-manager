#ifndef SENSOR_DATA_H
#define SENSOR_DATA_H

#include <cstdint>
/// accelerometer_t - raw accelerometer data (without g compensation)
/// @x - axis X
/// @y - axis Y
/// @z - axis Z
struct accelerometer_t {
  double x, y, z;
  accelerometer_t() : x(0.0), y(0.0), z(0.0) {}
  accelerometer_t(double x, double y, double z) : x(x), y(y), z(z) {}
};

// gyroscope_t - raw gyroscope data 
// @x - around axis X
// @y - around axis Y
// @z - around axis Z
struct gyroscope_t {
  double x, y, z;
  gyroscope_t() : x(0.0), y(0.0), z(0.0) {}
  gyroscope_t(double x, double y, double z) : x(x), y(y), z(z) {}
};

/// magnetometer_t - raw magnetometer data
/// @x - axis X
/// @y - axis Y
/// @z - axis Z
struct magnetometer_t {
  double x, y, z;
  magnetometer_t() : x(0.0), y(0.0), z(0.0) {}
  magnetometer_t(double x, double y, double z) : x(x), y(y), z(z) {}
};
//////////////////////////////////////////////////////////////

/// abs_accelerometer_t  - data calculated by quaternion x
/// linear_accelerometer_data or roatation matrix x linear_accelerometer_data
/// @x - axis X (longitude)
/// @y - axis Y (latitude)
/// @z - axiz Z (aptitude) zero for now
struct abs_accelerometer_t {
  double x, y, z;
  abs_accelerometer_t() : x(0.0), y(0.0), z(0.0) {}
  abs_accelerometer_t(double x, double y, double z) : x(x), y(y), z(z) {}
};
//////////////////////////////////////////////////////////////

/// geopoint - geopoint gps_coordinate
/// @latitude - latitude (axis Y)
/// @longitude - longitude (axis X)
struct geopoint {
  double latitude;
  double longitude;

  geopoint() : latitude(0.0), longitude(0.0) {}
  geopoint(double latitude, double longitude)
      : latitude(latitude), longitude(longitude) {}
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
      : azimuth(azimuth), value(value), accuracy(accuracy) {}
};
//////////////////////////////////////////////////////////////

/// gps_coordinate - consists of 2 independent parts : location and speed
/// @location - see gps_location_t
/// @speed - see gps_speed_t
struct gps_coordinate {
  geopoint location;
  gps_speed speed;
};
//////////////////////////////////////////////////////////////

#endif // SENSOR_DATA_H
