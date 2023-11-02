#include <gtest/gtest.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include <cmath>

#include "commons.h"
#include "mlm.h"
#include "sensor_data.h"

#ifdef _UNIT_TESTS_
int main_tests(int argc, char *argv[])
{
  testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}
#endif
//////////////////////////////////////////////////////////////

static bool get_sd_record_hdr(sd_record_hdr &hdr, const char *line);
static bool handle_acc_measured_record(MLM &mlm, const char *line);
static bool handle_acc_noised_record(MLM &mlm, const char *line);
static bool handle_gps_measured_record(MLM &mlm, const char *line);
static bool handle_gps_corrected_record(MLM &mlm, const char *line);
static bool handle_gps_noised_record(MLM &mlm, const char *line);
//////////////////////////////////////////////////////////////

// functions that should return true and do nothing

bool handle_acc_measured_record(MLM &mlm, const char *line)
{
  UNUSED(mlm);
  UNUSED(line);
  return true;
}

bool handle_gps_measured_record(MLM &mlm, const char *line)
{
  UNUSED(mlm);
  UNUSED(line);
  return true;
}

bool handle_gps_corrected_record(MLM &mlm, const char *line)
{
  UNUSED(mlm);
  UNUSED(line);
  return true;
}
//////////////////////////////////////////////////////////////

int main_mlm(int argc, char *argv[], char **env)
{
  UNUSED(argc);
  UNUSED(argv);
  UNUSED(env);

  char *line = NULL;
  size_t len = 0;
  ssize_t nread;
  sd_record_hdr hdr;

  MLM mlm;

  // see the order of SD_RECORD_TYPE enum
  bool (*record_handlers[])(MLM &, const char *) = {
      handle_acc_measured_record,
      handle_acc_noised_record,
      handle_gps_measured_record,
      handle_gps_corrected_record,
      handle_gps_noised_record,
  };

  while ((nread = getline(&line, &len, stdin)) != -1) {
    if (!get_sd_record_hdr(hdr, line)) {
      perror("it's not hdr\n");
      break;
    }

    int record_type = line[0] - '0';
    if (record_type < 0 || record_type >= SD_UNKNOWN) {
      perror("unknown record type\n");
      continue;
    }

    bool handled = record_handlers[record_type](mlm, line);
    if (handled)
      continue;  // do nothing

    fprintf(stderr, "failed to handle '%s' record", line);
  }

  free(line);
  return 0;
}
//////////////////////////////////////////////////////////////

bool handle_acc_noised_record(MLM &mlm, const char *line)
{
  sd_record_hdr hdr;
  abs_accelerometer acc;

  bool parsed = sd_acc_deserialize_str(line, hdr, acc);
  if (!parsed)
    return false;

  mlm.process_acc_data(acc, hdr.timestamp);
  return true;
}
//////////////////////////////////////////////////////////////

bool handle_gps_noised_record(MLM &mlm, const char *line)
{
  sd_record_hdr hdr;
  gps_coordinate gps;

  bool parsed = sd_gps_deserialize_str(line, hdr, gps);
  if (!parsed)
    return false;

  // WARNING!!!! TODO!!!! get velocity and position deviations
  // maybe not necessary in generated data.

  mlm.process_gps_data(gps, 1e-3, 1e-3);
  gps_coordinate pgps = mlm.predicted_coordinate();
  char buff[128] = {0};
  sd_gps_serialize_str(pgps,
                       SD_GPS_CORRECTED,
                       hdr.timestamp,
                       buff,
                       sizeof(buff));
  printf("%s\n", buff);
  return true;
}
//////////////////////////////////////////////////////////////

bool get_sd_record_hdr(sd_record_hdr &hdr, const char *line)
{
  return sscanf(line, "%d: %lf", &hdr.type, &hdr.timestamp) == 2;
}
//////////////////////////////////////////////////////////////

int main(int argc, char *argv[], char **env)
{
#ifdef _UNIT_TESTS_
  UNUSED(env);
  return main_tests(argc, argv);
#else
  return main_mlm(argc, argv, env);
#endif
}
/////////////////////////////////////////////////////////////
