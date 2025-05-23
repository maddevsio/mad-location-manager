#include "sensor_data.h"

#include <gtest/gtest.h>

TEST(sensor_data, test_serialize_acc_abs)
{
  sd_record sr(sd_record_hdr(SD_ACC_ENU_SET, 4.0),
               enu_accelerometer(1.1, 2.2, 0.0));
  std::string serialized = sdr_serialize_str(sr);
  std::string expected = "0 4:::1.1 2.2 0";
  EXPECT_EQ(serialized, expected);

  sr.hdr.type = SD_ACC_ENU_GENERATED;
  serialized = sdr_serialize_str(sr);
  expected = "1 4:::1.1 2.2 0";
  EXPECT_EQ(serialized, expected);
}
//////////////////////////////////////////////////////////////

TEST(sensor_data, test_serialize_acc_abs_small_values)
{
  sd_record sr(sd_record_hdr(SD_ACC_ENU_SET, 4.0),
               enu_accelerometer(1e-8, 2e-15, 0.0));
  std::string serialized = sdr_serialize_str(sr);
  std::string expected = "0 4:::1e-08 2e-15 0";
  EXPECT_EQ(serialized, expected);

  sd_record deserialized ;
  sdr_deserialize_error err = sdr_deserialize_str(expected, deserialized);
  ASSERT_EQ(SDRDE_SUCCESS, err);
  ASSERT_NEAR(sr.data.acc.x, deserialized.data.acc.x, 1e-10);
}
//////////////////////////////////////////////////////////////

TEST(sensor_data, test_deserialize_acc_abs)
{
  std::string input = "0 4:::1.1 2.2 0";
  sd_record act;
  sdr_deserialize_error dr = sdr_deserialize_str(input, act);
  ASSERT_EQ(dr, SDRDE_SUCCESS);
  ASSERT_EQ(act.hdr.type, SD_ACC_ENU_SET);
  ASSERT_EQ(act.hdr.timestamp, 4.0);
  ASSERT_EQ(act.data.acc.x, 1.1);
  ASSERT_EQ(act.data.acc.y, 2.2);
  ASSERT_EQ(act.data.acc.z, 0.0);
}
//////////////////////////////////////////////////////////////

TEST(sensor_data, test_deserialize_acc_abs_wrong_hdr)
{
  std::string input = "0 4:1.1 2.2 0";
  sd_record act;
  sdr_deserialize_error dr = sdr_deserialize_str(input, act);
  ASSERT_EQ(dr, SDRDE_WRONG_HDR_SEPARATOR);
}
//////////////////////////////////////////////////////////////

TEST(sensor_data, test_deserialize_acc_abs_wrong_fmt)
{
  std::string input = "0 4:::1.1 2";  // not enough fields
  sd_record act;
  sdr_deserialize_error dr = sdr_deserialize_str(input, act);
  ASSERT_EQ(dr, SDRDE_UNEXPECTED_FMT);
}
//////////////////////////////////////////////////////////////

TEST(sensor_data, test_deserialize_record_unsupported_type)
{
  std::string input = "9 4:::1.1 2";  // not enough fields
  sd_record act;
  sdr_deserialize_error dr = sdr_deserialize_str(input, act);
  ASSERT_EQ(dr, SDRDE_UNSUPPORTED);
}
//////////////////////////////////////////////////////////////

TEST(sensor_data, test_serialize_gps_record)
{
  sd_record sr;
  sr.hdr.type = SD_GPS_SET;
  sr.hdr.timestamp = 9.3;
  sr.data.gps.location = geopoint(1.123456789, 2.234567890, 3.3, 4.4);
  sr.data.gps.speed = gps_speed(5.5, 6.6, 7.7);

  std::string serialized = sdr_serialize_str(sr);
  std::string expected = "2 9.3:::1.123456789 2.23456789 3.3 4.4 5.5 6.6 7.7";
  EXPECT_EQ(serialized, expected);
}
//////////////////////////////////////////////////////////////

TEST(sensor_data, test_deserialize_gps_record)
{
  std::string input = "9 4:::1.1 2";  // not enough fields
  sd_record act;
  sdr_deserialize_error dr = sdr_deserialize_str(input, act);
  ASSERT_EQ(dr, SDRDE_UNSUPPORTED);
}
//////////////////////////////////////////////////////////////
