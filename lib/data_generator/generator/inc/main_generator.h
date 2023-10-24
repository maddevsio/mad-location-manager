#ifndef _MAIN_GENERATOR_H_
#define _MAIN_GENERATOR_H_

int generator_entry_point(int argc, char *argv[], char **env);

struct generator_options {
  double acceleration_time;
  double acc_measurement_period;
  double gps_measurement_period;
  double acc_noise;
  double gps_noise;
  char *output;

  generator_options()
      : acceleration_time(1.0),
        acc_measurement_period(1e-1),
        gps_measurement_period(1.5),
        acc_noise(0.0),
        gps_noise(0.0),
        output(nullptr)
  {
  }
};

int process_cl_arguments(int argc, char *argv[], generator_options &go);

#endif  // !_MAIN_GENERATOR_H_
