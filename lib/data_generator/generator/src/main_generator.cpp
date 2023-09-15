#include "main_generator.h"

#include <assert.h>
#include <getopt.h>
#include <stdio.h>
#include <string.h>

#include <cmath>

#include "commons.h"
#include "sd_generator.h"
#include "sensor_data.h"

static bool get_input_coordinate(geopoint &gp);

struct generator_options {
  double acceleration_time;
  double accelerometer_period;
  double gps_period;
  char *output;

  generator_options()
      : acceleration_time(1.0),
        accelerometer_period(1e-1),
        gps_period(1.5),
        output(nullptr)
  {
  }
};
static int process_cl_arguments(int argc, char *argv[], generator_options &go);
//////////////////////////////////////////////////////////////

static void mlm_gps_out(FILE *stream, const gps_coordinate &gc, double ts)
{
  const int buff_len = 128;
  char buff[buff_len] = {0};
  size_t record_len = sd_gps_serialize_str(gc, ts, buff, buff_len);
  assert(record_len < buff_len);
  fwrite(static_cast<void *>(buff), record_len, 1, stream);
  fprintf(stream, "\n");
}
//////////////////////////////////////////////////////////////

static void mlm_acc_out(FILE *stream,
                        const abs_accelerometer &acc,
                        double ts,
                        double acceleration_time,
                        double acc_measurement_period,
                        double no_acceleration_time)
{
  // these temp varialbes are supposed to make one line loops
  double at = acceleration_time;
  double amp = acc_measurement_period;
  double nat = no_acceleration_time;

  const int buff_len = 128;
  char buff[buff_len] = {0};
  for (double ats = 0.; ats < at; ats += amp) {
    size_t record_len = sd_acc_serialize_str(acc, ts + ats, buff, buff_len);
    assert(record_len < buff_len);
    fwrite(static_cast<void *>(buff), record_len, 1, stream);
    fprintf(stream, "\n");
  }

  for (double ats = 0.; ats < nat; ats += amp) {
    double nts = ts + at + ats;
    abs_accelerometer zacc(0, 0, 0);
    size_t record_len = sd_acc_serialize_str(zacc, nts, buff, buff_len);
    assert(record_len < buff_len);
    fwrite(static_cast<void *>(buff), record_len, 1, stream);
    fprintf(stream, "\n");
  }
}
//////////////////////////////////////////////////////////////

int generator_entry_point(int argc, char *argv[], char **env)
{
  UNUSED(env);

  generator_options go;
  int pcr = process_cl_arguments(argc, argv, go);
  if (pcr) {
    return pcr;
  }

  double no_acceleration_time = go.gps_period - go.acceleration_time;
  gps_coordinate prev_coord, current_coord;
  geopoint input_point;

  if (!get_input_coordinate(input_point)) {
    return 0;
  }

  FILE *mlm_out = stdout;
  if (go.output && ((mlm_out = fopen(go.output, "w")) == NULL)) {
    fprintf(stderr, "failed to open %s. using stdout instead", go.output);
    mlm_out = stdout;
  }

  current_coord.location = input_point;
  prev_coord = current_coord;
  mlm_gps_out(mlm_out, current_coord, 0.);

  double ts = 0.;
  while (get_input_coordinate(input_point)) {
    current_coord.location = input_point;
    abs_accelerometer acc =
        sd_abs_acc_between_two_geopoints(prev_coord,
                                         current_coord,
                                         go.acceleration_time,
                                         go.gps_period,
                                         0.);
    mlm_acc_out(mlm_out,
                acc,
                ts,
                go.acceleration_time,
                go.accelerometer_period,
                no_acceleration_time);

    ts += go.gps_period;
    // we have GPS point but have not speed for that point. generating:
    const movement_interval intervals[] = {
        {acc.azimuth(), acc.acceleration(), go.acceleration_time},
        {           0.,                 0., no_acceleration_time},
        {           0.,                 0.,                 -1.0},
    };

    current_coord = prev_coord;
    for (const movement_interval *i = intervals; i->duration != -1.0; ++i) {
      current_coord =
          sd_gps_coordinate_in_interval(current_coord, *i, i->duration);
    }
    // now we have GPS coordinate and speed
    mlm_gps_out(mlm_out, current_coord, ts);
    prev_coord = current_coord;
  }

  if (mlm_out != stdout) {
    fclose(mlm_out);
  }

  return 0;
}
//////////////////////////////////////////////////////////////

bool get_input_coordinate(geopoint &gp)
{
  return scanf("%lf,%lf", &gp.latitude, &gp.longitude) == 2;
}
//////////////////////////////////////////////////////////////

static void usage()
{
  fprintf(
      stderr,
      "mlm_data_generator %s - reads coordinates from stdin and outputs GPS "
      "coorinates, speed + accelerometer data.\n\n",
      "undefined version");

  fprintf(
      stderr,
      "Usage: mlm_data_generator [options]\n\n"
      "Options:\n"
      "-a, --acceleration-time\tacceleration time in seconds (floating point "
      "value)"
      "double\n"
      "-f, --acceleration-period\taccelerometer data period (seconds between "
      "measurements)"
      "double\n"
      "-g, --gps-period\tgps data period (seconds between measurements)"
      "double\n"
      "-o, --output\tpath to output file"
      "string\n"
      "-h, --help\tprint this help.\n");
}
//////////////////////////////////////////////////////////////
/// process_cl_arguments = process command line arguments
int process_cl_arguments(int argc, char *argv[], generator_options &go)
{
  static const struct option lopts[] = {
      {  "acceleration-time", required_argument, NULL, 'a'},
      {"acceleration-period", required_argument, NULL, 'f'},
      {         "gps-period", required_argument, NULL, 'g'},
      {             "output", required_argument, NULL, 'o'},
      {               "help",       no_argument, NULL, 'h'},
      {                 NULL,                 0, NULL,   0},
  };

  int opt_idx;
  size_t opt_len;
  while ((opt_idx = getopt_long(argc, argv, "a:f:g:h", lopts, NULL)) != -1) {
    switch (opt_idx) {
      case 'a':
        go.acceleration_time = atof(optarg);
        break;
      case 'f':
        go.accelerometer_period = atof(optarg);
        break;
      case 'g':
        go.gps_period = atof(optarg);
        break;
      case 'o':
        opt_len = strlen(optarg);
        go.output = new char[opt_len + 1];
        strncpy(go.output, optarg, opt_len);
        go.output[opt_len] = 0;
        break;
      case 'h':
        usage();
        return 1;
      default:
        printf(
            "something bad is happened. option index is out of bounds (%d "
            "%c)\n",
            opt_idx,
            (char)opt_idx);
        return 2;
    }
  }
  return 0;
};
//////////////////////////////////////////////////////////////
