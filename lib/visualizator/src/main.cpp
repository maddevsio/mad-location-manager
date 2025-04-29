#include <gtest/gtest.h>
#include <gtk/gtk.h>

#include "main_window.h"

static void activate(GtkApplication *app, gpointer user_data);
//////////////////////////////////////////////////////////////

#ifdef _UNIT_TESTS_
int main_tests(int argc, char *argv[])
{
  testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}
#endif
//////////////////////////////////////////////////////////////

int f(int a, int b)
{
  // return -1 if a < b
  // return 1 if a > b
  // return 0 if a == b
  return (a > b) - (a < b);
}

int main(int argc, char **argv)
{
#ifdef _UNIT_TESTS_
  return main_tests(argc, argv);
#endif
  GtkApplication *app;
  int status;
  generator_main_window *gmw = gmw_create();
  app = gtk_application_new("trajectory.generator.app",
                            G_APPLICATION_DEFAULT_FLAGS);
  g_signal_connect(app, "activate", G_CALLBACK(activate), gmw);
  status = g_application_run(G_APPLICATION(app), argc, argv);
  g_object_unref(app);
  gmw_free(gmw);
  return status;
}
//////////////////////////////////////////////////////////////

void activate(GtkApplication *app, gpointer user_data)
{
  (void)(app);
  (void)(user_data);
  generator_main_window *gmw =
      reinterpret_cast<generator_main_window *>(user_data);
  gmw_bind_to_app(app, gmw);
  gmw_show(gmw);

  // static const char *ui_path = "ui/mlm.ui";
  // GtkBuilder *builder = gtk_builder_new_from_file(ui_path);
  // GtkWidget *window =
  //     GTK_WIDGET(gtk_builder_get_object(builder, "ui_main_window"));
  // gtk_window_set_application(GTK_WINDOW(window), app);
  // gtk_widget_set_visible(window, TRUE);
  // g_object_unref(builder);
}
//////////////////////////////////////////////////////////////
