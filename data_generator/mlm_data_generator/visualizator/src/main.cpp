#include <gtest/gtest.h>
#include <gtk/gtk.h>

#include <iostream>
#include <memory>
#include <string>

#include "main_window.h"

static void activate(GtkApplication *app, gpointer user_data);
static void shutdown(GtkApplication *app, gpointer user_data);
//////////////////////////////////////////////////////////////

#ifdef _UNIT_TESTS_
int main_tests(int argc, char *argv[]) {
  testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}
#endif
//////////////////////////////////////////////////////////////

int main(int argc, char **argv) {
#ifdef _UNIT_TESTS_
  return main_tests(argc, argv);
#endif
  GtkApplication *app;
  int status;
  generator_main_window *gmw = gmw_create(argv[0]);
  app = gtk_application_new("trajectory.generator.app",
                            G_APPLICATION_DEFAULT_FLAGS);
  g_signal_connect(app, "activate", G_CALLBACK(activate), gmw);
  g_signal_connect(app, "shutdown", G_CALLBACK(shutdown), gmw);
  status = g_application_run(G_APPLICATION(app), argc, argv);
  g_object_unref(app);
  gmw_free(gmw);
  return status;
}
//////////////////////////////////////////////////////////////

void activate(GtkApplication *app, gpointer user_data) {
  generator_main_window *gmw =
      reinterpret_cast<generator_main_window *>(user_data);
  gmw_bind_to_app(app, gmw);
  gmw_show(gmw);
}
//////////////////////////////////////////////////////////////

void shutdown(GtkApplication *app, gpointer user_data) {
  (void)app;
  (void)user_data;
}
//////////////////////////////////////////////////////////////
