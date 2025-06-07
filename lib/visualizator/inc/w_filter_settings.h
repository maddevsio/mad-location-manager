#ifndef W_FILTER_SETTINGS_H
#define W_FILTER_SETTINGS_H

#include <gtk/gtk.h>

/// filter_options stores different options for ideal/noised data generation
struct filter_options {
  double acc_sigma_2;
  double loc_sigma_2;
  double vel_sigma_2;
  filter_options() : acc_sigma_2(0.3), loc_sigma_2(8.0), vel_sigma_2(0.05) {};
};
//////////////////////////////////////////////////////////////

struct w_filter_settings {
  filter_options opts;
  GtkWidget *frame;
  GtkWidget *grid;

  GtkWidget *lbl_acc_sigma_2;
  GtkWidget *tb_acc_sigma_2;

  GtkWidget *lbl_loc_sigma_2;
  GtkWidget *tb_loc_sigma_2;

  GtkWidget *lbl_vel_sigma_2;
  GtkWidget *tb_vel_sigma_2;

  GtkWidget *btn_generate;
  GtkWidget *btn_clear;
};

w_filter_settings *w_filter_settings_default(void);

#endif
