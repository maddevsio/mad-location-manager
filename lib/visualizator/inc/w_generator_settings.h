#ifndef W_GENERATOR_SETTINGS_H
#define W_GENERATOR_SETTINGS_H

#include <gtk/gtk.h>

/// generator_options stores different options for ideal/noised data generation
/// @acceleration_time - how much time point has acceleration between two points
/// @acc_measurement_period - data from accelerometer comes every
/// acc_measurement_period seconds
/// @gps_measurement_period - data comes every gps_measurement_period seconds
/// @acc_noise - accelerometer error (in meters/s^2)
/// @gps_location_noise - gps error (in meters). radius of circle around real
/// coordinate
/// @gps_speed_noise - gps speed error (in meters/sec)
struct generator_options {
  double acceleration_time;
  double acc_measurement_period;
  double gps_measurement_period;
  double acc_noise;
  double gps_location_noise;
  double gps_speed_noise;

  generator_options()
      : acceleration_time(0.8),
        acc_measurement_period(1e-1),
        gps_measurement_period(1.0),
        acc_noise(3e-1),
        gps_location_noise(15.0),
        gps_speed_noise(1e-2) {};
};
//////////////////////////////////////////////////////////////

struct w_generator_settings {
  generator_options opts;
  GtkWidget *frame;
  GtkWidget *grid;

  GtkWidget *lbl_acc_time;
  GtkWidget *tb_acc_time;
  GtkWidget *lbl_acc_measurement_period;
  GtkWidget *tb_acc_measurement_period;
  GtkWidget *lbl_gps_measurement_period;
  GtkWidget *tb_gps_measurement_period;
  GtkWidget *lbl_acc_noise;
  GtkWidget *tb_acc_noise;
  GtkWidget *lbl_gps_location_noise;
  GtkWidget *tb_gps_location_noise;
  GtkWidget *lbl_gps_speed_noise;
  GtkWidget *tb_gps_speed_noise;

  GtkWidget *btn_generate;
  GtkWidget *btn_clear;
};

w_generator_settings *w_generator_settings_default(void);

#endif
