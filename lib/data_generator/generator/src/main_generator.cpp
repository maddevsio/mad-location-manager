#include "main_generator.h"

#include <assert.h>
#include <getopt.h>
#include <stdio.h>
#include <string.h>

#include <cmath>
#include <functional>
#include <random>

#include "commons.h"
#include "coordinate.h"
#include "sd_generator.h"
#include "sensor_data.h"

static coordinates_vptr m_coord_vptr = coord_vptr_hq();
static bool get_input_coordinate(geopoint &gp);

static geopoint noised_geopoint(const geopoint &src, double gps_noise)
{
  // Will be used to obtain a seed for the random number engine
  std::random_device rd;
  // Standard mersenne_twister_engine seeded with rd()
  std::mt19937 gen(rd());
  std::uniform_real_distribution<> gps_dist(0.0, gps_noise);
  static std::uniform_real_distribution<> az_dist(0.0, 360.0);
  double gps_error = gps_dist(gen);
  double az_rnd = az_dist(gen);
  geopoint n_gps = m_coord_vptr.point_ahead(src, gps_error, az_rnd);
  return n_gps;
}
//////////////////////////////////////////////////////////////

static abs_accelerometer noised_acc(const abs_accelerometer &acc,
                                    double acc_noise)
{
  // Will be used to obtain a seed for the random number engine
  std::random_device rd;
  // Standard mersenne_twister_engine seeded with rd()
  std::mt19937 gen(rd());
  std::uniform_real_distribution<> gps_dist(-acc_noise / 2.0, acc_noise / 2.0);
  abs_accelerometer nacc(acc.x + gps_dist(gen), acc.y + gps_dist(gen), acc.z);
  return nacc;
}
//////////////////////////////////////////////////////////////

static void mlm_gps_out(FILE *stream,
                        const gps_coordinate &gc,
                        SD_RECORD_TYPE type,
                        double ts)
{
  const int buff_len = 128;
  char buff[buff_len] = {0};
  size_t record_len = sd_gps_serialize_str(gc, type, ts, buff, buff_len);
  assert(record_len < buff_len);
  fwrite(static_cast<void *>(buff), record_len, 1, stream);
  fprintf(stream, "\n");
}
//////////////////////////////////////////////////////////////

static void mlm_acc_out(FILE *stream,
                        const abs_accelerometer &acc,
                        SD_RECORD_TYPE rc,
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
    size_t record_len = sd_acc_serialize_str(acc, rc, ts + ats, buff, buff_len);
    assert(record_len < buff_len);
    fwrite(static_cast<void *>(buff), record_len, 1, stream);
    fprintf(stream, "\n");
  }

  abs_accelerometer zacc(0, 0, 0);
  for (double ats = 0.; ats < nat; ats += amp) {
    double nts = ts + at + ats;
    size_t record_len = sd_acc_serialize_str(zacc, rc, nts, buff, buff_len);
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

  fprintf(stderr,
          "next settings are applied:\n"
          "acceleration_time=%lg\n"
          "acc_measurement_period=%lg\n"
          "gps_measurement_period=%lg\n"
          "acc_noise=%lg\n"
          "gps_noise=%lg\n",
          go.acceleration_time,
          go.acc_measurement_period,
          go.gps_measurement_period,
          go.acc_noise,
          go.gps_noise);

  double no_acceleration_time =
      go.gps_measurement_period - go.acceleration_time;
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
  mlm_gps_out(mlm_out, current_coord, SD_GPS_MEASURED, 0.);

  double ts = 0.;
  while (get_input_coordinate(input_point)) {
    current_coord.location = input_point;
    abs_accelerometer acc =
        sd_abs_acc_between_two_geopoints(prev_coord,
                                         current_coord,
                                         go.acceleration_time,
                                         go.gps_measurement_period,
                                         0.);

    abs_accelerometer acc_with_noise = noised_acc(acc, go.acc_noise);
    mlm_acc_out(mlm_out,
                acc,
                SD_ACCELEROMETER_MEASURED,
                ts,
                go.acceleration_time,
                go.acc_measurement_period,
                no_acceleration_time);

    mlm_acc_out(mlm_out,
                acc_with_noise,
                SD_ACCELEROMETER_NOISED,
                ts,
                go.acceleration_time,
                go.acc_measurement_period,
                no_acceleration_time);

    ts += go.gps_measurement_period;
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
    mlm_gps_out(mlm_out, current_coord, SD_GPS_MEASURED, ts);

    gps_coordinate gps_with_noise = gps_coordinate(current_coord);
    gps_with_noise.location =
        noised_geopoint(current_coord.location, go.gps_noise);

    mlm_gps_out(mlm_out, gps_with_noise, SD_GPS_NOISED, ts);
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
  // todo handle comas and spaces
  return scanf(" %lf , %lf", &gp.latitude, &gp.longitude) == 2;
}
//////////////////////////////////////////////////////////////

static void usage()
{
  fprintf(
      stderr,
      "mlm_data_generator %s - reads coordinates from stdin and outputs GPS "
      "coorinates, speed + accelerometer data.\n\n",
      "undefined version");

  fprintf(stderr,
          "Usage: mlm_data_generator [options]\n\n"
          "Options:\n"
          "--acceleration-time\tacceleration time in seconds (floating point "
          "value)"
          "\tdouble\n"
          "--accelerometer-measurement-period\taccelerometer data period "
          "(seconds between "
          "measurements)"
          "\tdouble\n"
          "--gps-measurement-period\tgps data period (seconds between "
          "measurements)"
          "\tdouble\n"
          "--accelerometer-noise\tmax value of accelerometer error in m/sec^2"
          "\tdouble\n"
          "--gps-noise\tmax value of GPS error in meters"
          "\tdouble\n"
          "-o, --output\tpath to output file"
          "\tstring\n"
          "-h, --help\tprint this help.\n");
}
//////////////////////////////////////////////////////////////
/// process_cl_arguments = process command line arguments
int process_cl_arguments(int argc, char *argv[], generator_options &go)
{
  static const struct option lopts[] = {
      {               "acceleration-time", required_argument, NULL,   0},
      {"accelerometer-measurement-period", required_argument, NULL,   1},
      {          "gps-measurement-period", required_argument, NULL,   2},
      {             "accelerometer-noise", required_argument, NULL,   3},
      {                       "gps-noise", required_argument, NULL,   4},
      {                          "output", required_argument, NULL, 'o'},
      {                            "help",       no_argument, NULL, 'h'},
      {                              NULL,                 0, NULL,  -1},
  };

  double *go_doubles[] = {&go.acceleration_time,
                          &go.acc_measurement_period,
                          &go.gps_measurement_period,
                          &go.acc_noise,
                          &go.gps_noise};

  int co;
  size_t opt_len;
  while ((co = getopt_long(argc, argv, "o:h", lopts, NULL)) != -1) {
    switch (co) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
        *go_doubles[co] = atof(optarg);
        break;

      case 'o':
        opt_len = strlen(optarg);
        go.output = new char[opt_len + 1];
        strncpy(go.output, optarg, opt_len);
        go.output[opt_len] = 0;
        break;

      case '?':
        printf("unrecognized option: %d\n", optopt);
        usage();
        break;

      case 'h':
        usage();
        return 1;

      default:
        printf(
            "something bad is happened. option index is out of bounds (%d "
            "%c)\n",
            co,
            static_cast<char>(co));
        usage();
        return 2;
    }
  }

  return 0;
};
//////////////////////////////////////////////////////////////
