#include <gtest/gtest.h>

#include "main_generator.h"

TEST(generator_options, cli_parser)
{
  generator_options go;
  // cast to char is safe hack, it won't be modified. 
  // unfortunately it's impossible to mark argv as const char **
  char* argv[] = {(char*)"generator_options_cli_parser_test",
                  (char*)"--acceleration-time",
                  (char*)"1.3",
                  (char*)"--accelerometer-measurement-period",
                  (char*)"2.5",
                  (char*)"--gps-measurement-period",
                  (char*)"3.6",
                  (char*)"--accelerometer-noise",
                  (char*)"4.7",
                  (char*)"--gps-noise",
                  (char*)"5.8"};
  int argc = sizeof(argv) / sizeof(char*);
  int pcr = process_cl_arguments(argc, argv, go);
  ASSERT_EQ(0, pcr);
  ASSERT_EQ(1.3, go.acceleration_time);
  ASSERT_EQ(2.5, go.acc_measurement_period);
  ASSERT_EQ(3.6, go.gps_measurement_period);
  ASSERT_EQ(4.7, go.acc_noise);
  ASSERT_EQ(5.8, go.gps_noise);
}
//////////////////////////////////////////////////////////////
