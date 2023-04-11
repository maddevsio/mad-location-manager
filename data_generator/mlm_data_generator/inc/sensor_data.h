#ifndef SENSOR_DATA_H
#define SENSOR_DATA_H

#include <cstdint>
struct accelerometer_t {
  double x, y, z;
  accelerometer_t() : x(0.0), y(0.0), z(0.0) {}
  accelerometer_t(double x, double y, double z) : x(x), y(y), z(z) {}
};

struct gyroscope_t {
  double x, y, z;
  gyroscope_t() : x(0.0), y(0.0), z(0.0) {}
  gyroscope_t(double x, double y, double z) : x(x), y(y), z(z) {}
};

struct magnetometer_t {
  double x, y, z;
  magnetometer_t() : x(0.0), y(0.0), z(0.0) {}
  magnetometer_t(double x, double y, double z) : x(x), y(y), z(z) {}
};
//////////////////////////////////////////////////////////////

/// abs_accelerometer_t  - data calculated by quaternion x
/// linear_accelerometer_data or roatation matrix x linear_accelerometer_data
struct abs_accelerometer_t {
  // x - along longitude
  // y - along latitude
  // z - always zero for now.
  double x, y, z;
  abs_accelerometer_t() : x(0.0), y(0.0), z(0.0) {}
  abs_accelerometer_t(double x, double y, double z) : x(x), y(y), z(z) {}
};
//////////////////////////////////////////////////////////////

struct gps_location_t {
  double latitude;
  double longitude;

  gps_location_t() : latitude(0.0), longitude(0.0) {}
  gps_location_t(double latitude, double longitude)
      : latitude(latitude), longitude(longitude) {}
};
//////////////////////////////////////////////////////////////

struct gps_speed_t {
  double azimuth;
  double value;
  double accuracy;

  gps_speed_t() : azimuth(0.0), value(0.0), accuracy(0.0) {}
  gps_speed_t(double azimuth, double value, double accuracy)
      : azimuth(azimuth), value(value), accuracy(accuracy) {}
};
//////////////////////////////////////////////////////////////

struct gps_coordinate_t {
  gps_location_t location;
  gps_speed_t speed;
};
//////////////////////////////////////////////////////////////

#endif // SENSOR_DATA_H
