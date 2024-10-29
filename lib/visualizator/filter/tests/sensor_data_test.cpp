#include "sensor_data.h"

#include <gtest/gtest.h>

TEST(sensor_data, test_serialize_acc_abs)
{
  sd_record sr;
  sr.hdr.type = SD_ACC_ABS_MEASURED;
  sr.hdr.timestamp = 4.0;
  sr.data.acc.x = 1.1;
  sr.data.acc.y = 2.2;
  sr.data.acc.z = 0.0;

  std::string serialized = sdr_serialize_str(sr);
  std::string expected = "0 4:::1.1 2.2 0";
  EXPECT_EQ(serialized, expected);

  sr.hdr.type = SD_ACC_ABS_NOISED;
  serialized = sdr_serialize_str(sr);
  expected = "1 4:::1.1 2.2 0";
  EXPECT_EQ(serialized, expected);
}
//////////////////////////////////////////////////////////////

TEST(sensor_data, test_deserialize_acc_abs)
{
  std::string input = "0 4:::1.1 2.2 0";
  sd_record act;
  int dr = sdr_deserialize_str(input, act);
  ASSERT_EQ(dr, 0);
  ASSERT_EQ(act.hdr.type, SD_ACC_ABS_MEASURED);
  ASSERT_EQ(act.hdr.timestamp, 4.0);
  ASSERT_EQ(act.data.acc.x, 1.1);
  ASSERT_EQ(act.data.acc.y, 2.2);
  ASSERT_EQ(act.data.acc.z, 0.0);
}
//////////////////////////////////////////////////////////////
