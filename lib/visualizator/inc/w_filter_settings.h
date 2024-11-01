#ifndef W_FILTER_SETTINGS_H
#define W_FILTER_SETTINGS_H

#include <gtk/gtk.h>

/// filter_options stores different options for ideal/noised data generation
/// @acceleration_time - how much time point has acceleration between two points
struct filter_options {
  double dummy;
  filter_options() : dummy(0.0) {};
};
//////////////////////////////////////////////////////////////

struct w_filter_settings {
  filter_options opts;
  GtkWidget *frame;
  GtkWidget *grid;

  GtkWidget *lbl_dummy;
  GtkWidget *tb_dummy;

  GtkWidget *btn_generate;
  GtkWidget *btn_clear;
};

w_filter_settings *w_filter_settings_default(void);

#endif
