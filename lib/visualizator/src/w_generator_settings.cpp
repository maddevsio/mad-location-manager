#include "w_generator_settings.h"

#include <iostream>

#include "vis_commons.h"

static w_generator_settings *w_generator_settings_new(
    const generator_options *opts);

w_generator_settings *w_generator_settings_default(void)
{
  generator_options default_opts;
  return w_generator_settings_new(&default_opts);
}
//////////////////////////////////////////////////////////////

static void insert_only_numbers_slot(GtkEditable *self,
                                     gchar *new_text,
                                     gint new_text_length,
                                     gint *position,
                                     gpointer user_data)
{
  UNUSED(position);
  double *opt = reinterpret_cast<double *>(user_data);
  std::string full_text(gtk_editable_get_text(self));
  for (int i = 0; i < new_text_length; ++i)
    full_text.push_back(new_text[i]);

  if (full_text.empty()) {
    return;  // do nothing
  }

  try {
    size_t idx;
    double val = std::stod(full_text, &idx);
    if (idx != full_text.size()) {
      g_signal_stop_emission_by_name(G_OBJECT(self), "insert-text");
      // TODO some signalization
      return;
    }
    *opt = val;
  } catch (std::exception &exc) {
    std::cerr << exc.what() << std::endl;
    g_signal_stop_emission_by_name(G_OBJECT(self), "insert-text");
  }
}
//////////////////////////////////////////////////////////////

w_generator_settings *w_generator_settings_new(const generator_options *opts)
{
  w_generator_settings *res = new w_generator_settings();
  res->opts = *opts;

  res->btn_generate = gtk_button_new();
  gtk_button_set_label(GTK_BUTTON(res->btn_generate), "Generate");
  res->btn_clear = gtk_button_new();
  gtk_button_set_label(GTK_BUTTON(res->btn_clear), "Clear");

  res->grid = gtk_grid_new();
  gtk_grid_set_column_spacing(GTK_GRID(res->grid), 5);
  gtk_grid_set_row_spacing(GTK_GRID(res->grid), 5);

  // looks little better without row_homogeneous
  /* gtk_grid_set_row_homogeneous(GTK_GRID(grid), true); */
  gtk_grid_set_column_homogeneous(GTK_GRID(res->grid), true);

  GtkWidget **widgets[] = {&res->lbl_acc_time,
                           &res->tb_acc_time,
                           &res->lbl_acc_measurement_period,
                           &res->tb_acc_measurement_period,
                           &res->lbl_gps_measurement_period,
                           &res->tb_gps_measurement_period,
                           &res->lbl_acc_noise,
                           &res->tb_acc_noise,
                           &res->lbl_gps_location_noise,
                           &res->tb_gps_location_noise,
                           &res->lbl_gps_speed_noise,
                           &res->tb_gps_speed_noise,
                           nullptr,
                           nullptr};
  const char *lbl_names[] = {"Acceleration time",
                             "Acceleration measurement period",
                             "GPS measurement period",
                             "Acceleration noise",
                             "GPS location noise",
                             "GPS speed noise",
                             nullptr};
  double *lst_options_pointers[] = {&res->opts.acceleration_time,
                                    &res->opts.acc_measurement_period,
                                    &res->opts.gps_measurement_period,
                                    &res->opts.acc_noise,
                                    &res->opts.gps_location_noise,
                                    &res->opts.gps_speed_noise,
                                    nullptr};

  int r = 0;
  for (; widgets[r * 2]; ++r) {
    *widgets[r * 2] = gtk_label_new(lbl_names[r]);
    *widgets[r * 2 + 1] = gtk_entry_new();

    gtk_editable_set_text(
        GTK_EDITABLE(*widgets[r * 2 + 1]),
        to_string_with_precision<double>(*lst_options_pointers[r], 2).c_str());

    gtk_entry_set_input_purpose(GTK_ENTRY(*widgets[r * 2 + 1]),
                                GTK_INPUT_PURPOSE_NUMBER);

    g_signal_connect(
        gtk_editable_get_delegate(GTK_EDITABLE(*widgets[r * 2 + 1])),
        "insert-text",
        G_CALLBACK(insert_only_numbers_slot),
        lst_options_pointers[r]);

    gtk_grid_attach(GTK_GRID(res->grid), *widgets[r * 2], 0, r, 1, 1);
    gtk_grid_attach(GTK_GRID(res->grid), *widgets[r * 2 + 1], 1, r, 1, 1);
  }

  gtk_grid_attach(GTK_GRID(res->grid), res->btn_generate, 0, r, 1, 2);
  gtk_grid_attach(GTK_GRID(res->grid), res->btn_clear, 1, r, 1, 2);

  res->frame = gtk_frame_new("Generator settings");
  gtk_frame_set_child(GTK_FRAME(res->frame), res->grid);
  gtk_frame_set_label_align(GTK_FRAME(res->frame), 0.5);
  return res;
}
