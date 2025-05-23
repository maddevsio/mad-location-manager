#include "sd_generator.h"

#include <gtest/gtest.h>

#include "GeographicLib/LocalCartesian.hpp"
#include "sensor_data.h"

TEST(sd_generator, test_go_to_point_and_back)
{
  gps_coordinate sc;
  const double latitude = 20.0;
  const double longitude = 30.0;
  sc.location = geopoint(latitude, longitude);
  sc.speed = gps_speed(0.0, 0.0, 1.0);

  const movement_interval intervals[] = {
      { 45.0, 5.0,  5.0},
      {  0.0, 0.0, 15.0},
      {225.0, 5.0, 10.0}, // opposite direction
      {  0.0, 0.0, 15.0},
      { 45.0, 5.0,  5.0},
      {   0.,  0.,  -1.},
  };

  double start_time = 0.0;
  double acc_interval = 0.001;  // 1000 per second

  for (const movement_interval *i = intervals; i->duration != -1.; ++i) {
    double end_time = start_time + i->duration;
    enu_accelerometer acc(i->acceleration, i->cartezian_angle);
    // while (end_time > start_time) ..
    while (fabs(end_time - start_time) > 1e-9) {
      start_time += acc_interval;
      sc = sd_gps_coordinate_in_interval(sc, *i, acc_interval);
    }  // while (end_time > start_time);
  }  // for (const auto &interval : intervals)

  EXPECT_NEAR(0.0, sc.speed.value, 1e-9);
  EXPECT_NEAR(latitude, sc.location.latitude, 1e-6);
  EXPECT_NEAR(longitude, sc.location.longitude, 1e-6);
}
//////////////////////////////////////////////////////////////

TEST(sd_generator, test_speed_generation)
{
  gps_coordinate sc;
  const double latitude = 20.0;
  const double longitude = 30.0;
  sc.location = geopoint(latitude, longitude);
  sc.speed = gps_speed(0.0, 0.0, 1.0);

  const movement_interval intervals[] = {
      { 45., 5.0,  5.0},
      { 0.0, 0.0, 15.0},
      {225., 5.0,  5.0},
      {  0.,  0.,  -1.},
  };

  double start_time = 0.0;
  double acc_interval = 0.001;  // 1000 per second

  for (const movement_interval *i = intervals; i->duration != -1.; ++i) {
    double end_time = start_time + i->duration;
    enu_accelerometer acc(i->acceleration, i->cartezian_angle);
    // while (end_time > start_time) ..
    while (fabs(end_time - start_time) > 1e-9) {
      start_time += acc_interval;
      sc = sd_gps_coordinate_in_interval(sc, *i, acc_interval);
    }
  }

  EXPECT_NEAR(0.0, sc.speed.value, 1e-9);
}
//////////////////////////////////////////////////////////////

TEST(sd_generator, test_abs_acc_generation_1)
{
  gps_coordinate a, b;
  a.location = geopoint(36.556144, 31.976737);
  b.location = geopoint(36.557275, 31.994406);
  gps_coordinate c = a;

  double acceleration_time = 5.0;
  double no_acceleration_time = 10.0;
  double interval_time = acceleration_time + no_acceleration_time;
  enu_accelerometer acc = sd_abs_acc_between_two_geopoints(a,
                                                           b,
                                                           acceleration_time,
                                                           interval_time,
                                                           0.0);
  const movement_interval intervals[] = {
      {acc.cartezian_angle(), acc.acceleration(),    acceleration_time},
      {                   0.,                0.0, no_acceleration_time},
      {                   0.,                 0.,                  -1.},
  };

  for (const movement_interval *i = intervals; i->duration != -1.; ++i) {
    c = sd_gps_coordinate_in_interval(c, *i, i->duration);
  }

  ASSERT_NEAR(c.location.longitude, b.location.longitude, 1e-6);
  ASSERT_NEAR(c.location.latitude, b.location.latitude, 1e-6);
}
//////////////////////////////////////////////////////////////

TEST(sd_generator, test_abs_acc_generation_2)
{
  gps_coordinate a, b;
  a.location = geopoint(11.11111, 11.11111);
  b.location = geopoint(11.11112, 11.11112);

  double acceleration_time = 1.0;
  double no_acceleration_time = 0.5;
  double interval_time = acceleration_time + no_acceleration_time;
  enu_accelerometer acc = sd_abs_acc_between_two_geopoints(a,
                                                           b,
                                                           acceleration_time,
                                                           interval_time,
                                                           0.0);

  const movement_interval intervals[] = {
      {acc.cartezian_angle(), acc.acceleration(),    acceleration_time},
      {                   0.,                0.0, no_acceleration_time},
      {                   0.,                 0.,                  -1.},
  };

  for (const movement_interval *i = intervals; i->duration != -1.; ++i) {
    a = sd_gps_coordinate_in_interval(a, *i, i->duration);
  }

  ASSERT_NEAR(a.location.longitude, b.location.longitude, 1e-7);
  ASSERT_NEAR(a.location.latitude, b.location.latitude, 1e-7);
}
//////////////////////////////////////////////////////////////

TEST(sd_generator, test_abs_acc_generation_no_movement)
{
  gps_coordinate a, b;
  a.location = geopoint(36.556144, 31.976737);
  b.location = geopoint(36.556144, 31.976737);
  gps_coordinate c = a;

  enu_accelerometer acc =
      sd_abs_acc_between_two_geopoints(a, b, 5.0, 15.0, 0.0);
  const movement_interval intervals[] = {
      {acc.cartezian_angle(), acc.acceleration(),  5.0},
      {                   0.,                0.0, 10.0},
      {                   0.,                 0.,  -1.},
  };

  for (const movement_interval *i = intervals; i->duration != -1.; ++i) {
    c = sd_gps_coordinate_in_interval(c, *i, i->duration);
  }

  ASSERT_NEAR(c.location.longitude, b.location.longitude, 1e-8);
  ASSERT_NEAR(c.location.latitude, b.location.latitude, 1e-8);
  ASSERT_NEAR(acc.acceleration(), 0.0, 1e-9);
  ASSERT_NEAR(c.speed.value, 0.0, 1e-9);
}
//////////////////////////////////////////////////////////////

TEST(sd_generator, test_acc_generation_1D)
{
  double s = 112.5;
  double t1 = 5;
  double t2 = 2;
  double v0 = 0.;
  double acc = sd_acc_between_two_points(s, v0, t1, t2);
  ASSERT_NEAR(acc, 5.0, 1e-6);
}
//////////////////////////////////////////////////////////////

TEST(sd_generator, test_acc_generation_1D_start_speed)
{
  double s = 112.5;
  double t1 = 5;
  double t2 = 2;
  double v0 = -10.;
  double acc = sd_acc_between_two_points(s, v0, t1, t2);
  ASSERT_NEAR(acc, 182.5 / 22.5, 1e-6);
}
//////////////////////////////////////////////////////////////

TEST(sd_generator, test_noised_gps_eq_no_noise)
{
  geopoint expected(10.12345, 12.54321);
  geopoint act = sd_noised_geopoint(expected, 0.);
  ASSERT_NEAR(expected.latitude, act.latitude, 1e-8);
  ASSERT_NEAR(expected.longitude, act.longitude, 1e-8);
}
//////////////////////////////////////////////////////////////

TEST(sd_generator, test_noised_gps_not_eq_with_noise)
{
  geopoint expected(10.12345, 12.54321);
  geopoint act = sd_noised_geopoint(expected, 15);
  ASSERT_NE(expected.latitude, act.latitude);
  ASSERT_NE(expected.longitude, act.longitude);
}
//////////////////////////////////////////////////////////////

TEST(sd_generator, test_acceleration_axis)
{
  geopoint a(45.0, 45.0);
  geopoint b(44.999, 45.0);
  geopoint c(44.999, 44.999);

  gps_coordinate aa, bb, cc;
  aa.location = a;
  bb.location = b;
  cc.location = c;

  enu_accelerometer acc = sd_abs_acc_between_two_geopoints(aa, bb, 1.0, 2.0, 0.8);
  bb = sd_gps_coordinate_in_interval(
      aa,
      movement_interval(acc.cartezian_angle(), acc.acceleration(), 1.0),
      1.0);
  std::cerr << "bb: " << bb.speed.azimuth << " :: " << bb.speed.value << std::endl;

  bb = sd_gps_coordinate_in_interval(
      bb,
      movement_interval(0., 0., 1.0),
      1.0);
  std::cerr << "bb: " << bb.speed.azimuth << " :: " << bb.speed.value << std::endl;
}
