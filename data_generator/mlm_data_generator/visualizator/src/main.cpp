#include <gtk/gtk.h>
#include <iostream>
#include <shumate/shumate.h>
#include <string>

static void btnCount_clicked(GtkWidget *widget, gpointer data);
static void activate(GtkApplication *app, gpointer user_data);

int main(int argc, char **argv) {
  GtkApplication *app;
  int status;

  app = gtk_application_new("org.gtk.example", G_APPLICATION_DEFAULT_FLAGS);
  g_signal_connect(app, "activate", G_CALLBACK(activate), NULL);
  status = g_application_run(G_APPLICATION(app), argc, argv);
  g_object_unref(app);
  return status;
}
//////////////////////////////////////////////////////////////

void btnCount_clicked(GtkWidget *widget, gpointer data) {
  (void)widget;
  static int count = 0;
  GtkWidget *lblCount = static_cast<GtkWidget *>(data);
  gtk_label_set_text(GTK_LABEL(lblCount), std::to_string(++count).c_str());
}
//////////////////////////////////////////////////////////////

void activate(GtkApplication *app, gpointer user_data) {
  (void)user_data;
  GtkWidget *window = gtk_application_window_new(app);
  GtkWidget *grid = gtk_grid_new();
  GtkWidget *btnCount = gtk_button_new();
  GtkWidget *lblCount = gtk_label_new("0");
  ShumateSimpleMap *smap = shumate_simple_map_new();

  // main window
  gtk_window_set_title(GTK_WINDOW(window), "Trajectory generator");
  gtk_window_set_default_size(GTK_WINDOW(window), 800, 600);

  // label
  gtk_label_set_xalign(GTK_LABEL(lblCount), 0.5);

  // button
  gtk_button_set_label(GTK_BUTTON(btnCount), "Counter");
  g_signal_connect(btnCount, "clicked", GCallback(btnCount_clicked), lblCount);

  // map
  ShumateMapSourceRegistry *msr =
      shumate_map_source_registry_new_with_defaults();
  ShumateMapSource *ms = shumate_map_source_registry_get_by_id(
      msr, SHUMATE_MAP_SOURCE_OSM_MAPNIK);
  shumate_simple_map_set_map_source(smap, ms);
  ShumateViewport *vp = shumate_simple_map_get_viewport(smap);
  shumate_viewport_set_zoom_level(vp, 4.0);
  ShumateMap *map = shumate_simple_map_get_map(smap);
  shumate_map_set_animate_zoom(map, false);
  shumate_map_go_to_full(map, 36.5519514, 31.9801362, 14.0);

  ShumateMarkerLayer *marker_layer = shumate_marker_layer_new(vp);
  shumate_simple_map_add_overlay_layer(smap, SHUMATE_LAYER(marker_layer));

  const char *img_path =
      "/home/lezh1k/SRC/work/MDPet/mad-location-manager/data_generator/"
      "visualizator/resources/map-marker.svg";
  // GtkWidget *img = gtk_image_new_from_icon_name(img_path);
  GtkWidget *img = gtk_image_new_from_file(img_path);
  ShumateMarker *marker = shumate_marker_new();
  shumate_location_set_location(SHUMATE_LOCATION(marker), 36.5519514, 31.9801362);
  shumate_marker_set_child(marker, img);
  shumate_marker_layer_add_marker(marker_layer, marker);

  std::cout << "min zoom level: " << shumate_viewport_get_min_zoom_level(vp)
            << "\n";
  std::cout << "max zoom level: " << shumate_viewport_get_max_zoom_level(vp)
            << "\n";

  // grid
  gtk_grid_set_column_spacing(GTK_GRID(grid), 10);
  gtk_grid_set_row_spacing(GTK_GRID(grid), 10);
  gtk_widget_set_hexpand(GTK_WIDGET(grid), true);
  gtk_widget_set_hexpand(GTK_WIDGET(grid), true);

  gtk_grid_attach(GTK_GRID(grid), btnCount, 0, 0, 1, 1);
  gtk_grid_attach(GTK_GRID(grid), lblCount, 1, 0, 1, 1);
  gtk_grid_attach(GTK_GRID(grid), GTK_WIDGET(smap), 0, 1, 2, 2);
  gtk_window_set_child(GTK_WINDOW(window), grid);

  // show window
  gtk_widget_set_visible(window, true);
}
//////////////////////////////////////////////////////////////
