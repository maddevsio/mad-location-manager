#ifndef W_GENERATOR_SETTINGS_H
#define W_GENERATOR_SETTINGS_H

#include <gtk/gtk.h>

struct generator_options {
  double acceleration_time;
  double acc_measurement_period;
  double gps_measurement_period;
  double acc_noise;
  double gps_noise;

  generator_options()
      : acceleration_time(1.0),
        acc_measurement_period(1e-1),
        gps_measurement_period(1.5),
        acc_noise(0.0),
        gps_noise(0.0) {};
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
  GtkWidget *lbl_gps_noise;
  GtkWidget *tb_gps_noise;
  GtkWidget *btn_generate;
};

w_generator_settings *w_generator_settings_default(void);
w_generator_settings *w_generator_settings_new(const generator_options *opts);

#endif
