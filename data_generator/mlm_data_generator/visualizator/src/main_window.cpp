#include "main_window.h"
#include <iostream>

generator_main_window::generator_main_window()
    : window(nullptr), simple_map(nullptr), map_source_registry(nullptr),
      map_marker_layer(nullptr), map_path_layer_green(nullptr),
      map_path_layer_red(nullptr), map_path_layer_blue(nullptr) {}
//////////////////////////////////////////////////////////////

generator_main_window::~generator_main_window() {
  std::cout << "~generator_main_window\n";
  // g_clear_object (&map_source_registry);
  // g_clear_object (&simple_map);
}
//////////////////////////////////////////////////////////////

void generator_main_window_bind_to_app(GtkApplication *app,
                                       generator_main_window *gmw) {
  gmw->window = gtk_application_window_new(app);
  GtkWidget *grid = gtk_grid_new();
  GtkWidget *btn_count = gtk_button_new();
  GtkWidget *lbl_count = gtk_label_new("0");

  // window
  gtk_window_set_title(GTK_WINDOW(gmw->window), "Trajectory generator");
  gtk_window_set_default_size(GTK_WINDOW(gmw->window), 800, 600);

  // label . do nothing, default xalign = 0.5 that what's we need

  // button
  gtk_button_set_label(GTK_BUTTON(btn_count), "Counter");
  // todo btn_clicked

  gmw->simple_map = shumate_simple_map_new();
  // map
  gmw->map_source_registry = shumate_map_source_registry_new_with_defaults();
  ShumateMapSource *ms = shumate_map_source_registry_get_by_id(
      gmw->map_source_registry, SHUMATE_MAP_SOURCE_OSM_MAPNIK);
  shumate_simple_map_set_map_source(gmw->simple_map, ms);
  ShumateViewport *vp = shumate_simple_map_get_viewport(gmw->simple_map);
  shumate_viewport_set_zoom_level(vp, 16.0);
  ShumateMap *map = shumate_simple_map_get_map(gmw->simple_map);
  shumate_map_center_on(map, 36.5519514, 31.9801362);
  // shumate_map_go_to_full(map, 36.5519514, 31.9801362, 14.0); // with
  // animation

  gmw->map_marker_layer = shumate_marker_layer_new(vp);
  gmw->map_path_layer_green = shumate_path_layer_new(vp);
  gmw->map_path_layer_red = shumate_path_layer_new(vp);
  gmw->map_path_layer_blue = shumate_path_layer_new(vp);

  shumate_simple_map_add_overlay_layer(gmw->simple_map,
                                       SHUMATE_LAYER(gmw->map_marker_layer));
  shumate_simple_map_add_overlay_layer(
      gmw->simple_map, SHUMATE_LAYER(gmw->map_path_layer_green));
  shumate_simple_map_add_overlay_layer(gmw->simple_map,
                                       SHUMATE_LAYER(gmw->map_path_layer_blue));

  // grid
  gtk_grid_set_column_spacing(GTK_GRID(grid), 10);
  gtk_grid_set_row_spacing(GTK_GRID(grid), 10);
  gtk_widget_set_hexpand(GTK_WIDGET(grid), true);
  gtk_widget_set_hexpand(GTK_WIDGET(grid), true);

  gtk_grid_attach(GTK_GRID(grid), btn_count, 0, 0, 1, 1);
  gtk_grid_attach(GTK_GRID(grid), lbl_count, 1, 0, 1, 1);
  gtk_grid_attach(GTK_GRID(grid), GTK_WIDGET(gmw->simple_map), 0, 1, 2, 2);

  // set grid as child of main window
  gtk_window_set_child(GTK_WINDOW(gmw->window), grid);
}
//////////////////////////////////////////////////////////////

// #include <iostream>
//
// MainWindow::MainWindow(GtkApplication *app) {
//   m_window = gtk_application_window_new(app);
//   GtkWidget *grid = gtk_grid_new();
//   GtkWidget *btn_count = gtk_button_new();
//   GtkWidget *lbl_count = gtk_label_new("0");
//   m_simple_map = shumate_simple_map_new();
//
//   // m_window
//   gtk_window_set_title(GTK_WINDOW(m_window), "Trajectory generator");
//   gtk_window_set_default_size(GTK_WINDOW(m_window), 800, 600);
//
//   // label . do nothing, default xalign = 0.5 that what's we need
//
//   // button
//   gtk_button_set_label(GTK_BUTTON(btn_count), "Counter");
//   // todo btn_clicked
//
//   // map
//   ShumateMapSourceRegistry *msr =
//       shumate_map_source_registry_new_with_defaults();
//   ShumateMapSource *ms =
//       shumate_map_source_registry_get_by_id(msr,
//       SHUMATE_MAP_SOURCE_OSM_MAPNIK);
//   shumate_simple_map_set_map_source(m_simple_map, ms);
//   ShumateViewport *vp = shumate_simple_map_get_viewport(m_simple_map);
//   shumate_viewport_set_zoom_level(vp, 16.0);
//   ShumateMap *map = shumate_simple_map_get_map(m_simple_map);
//   shumate_map_center_on(map, 36.5519514, 31.9801362);
//   // shumate_map_go_to_full(map, 36.5519514, 31.9801362, 14.0); // with
//   animation
//
//   m_map_marker_layer = shumate_marker_layer_new(vp);
//   m_map_path_layer_green = shumate_path_layer_new(vp);
//   m_map_path_layer_red = shumate_path_layer_new(vp);
//   m_map_path_layer_blue = shumate_path_layer_new(vp);
//
//   shumate_simple_map_add_overlay_layer(m_simple_map,
//   SHUMATE_LAYER(m_map_marker_layer));
//   shumate_simple_map_add_overlay_layer(m_simple_map,
//   SHUMATE_LAYER(m_map_path_layer_green));
//   shumate_simple_map_add_overlay_layer(m_simple_map,
//   SHUMATE_LAYER(m_map_path_layer_red));
//   shumate_simple_map_add_overlay_layer(m_simple_map,
//   SHUMATE_LAYER(m_map_path_layer_blue));
//
//   // grid
//   gtk_grid_set_column_spacing(GTK_GRID(grid), 10);
//   gtk_grid_set_row_spacing(GTK_GRID(grid), 10);
//   gtk_widget_set_hexpand(GTK_WIDGET(grid), true);
//   gtk_widget_set_hexpand(GTK_WIDGET(grid), true);
//
//   gtk_grid_attach(GTK_GRID(grid), btn_count, 0, 0, 1, 1);
//   gtk_grid_attach(GTK_GRID(grid), lbl_count, 1, 0, 1, 1);
//   gtk_grid_attach(GTK_GRID(grid), GTK_WIDGET(m_simple_map), 0, 1, 2, 2);
//   gtk_window_set_child(GTK_WINDOW(m_window), grid);
//
//   this->add_marker(MC_RED, 36.5519514, 31.9801362);
//   this->add_marker(MC_RED, 36.5519524, 31.9813362);
//   this->add_marker(MC_RED, 36.5519534, 31.9823362);
//   this->add_marker(MC_RED, 36.5519544, 31.9834362);
//
//
//   this->add_marker(MC_BLUE, 36.5519514, 31.9815362);
//   this->add_marker(MC_BLUE, 36.5529524, 31.9815362);
//   this->add_marker(MC_BLUE, 36.5539534, 31.9815362);
//   this->add_marker(MC_BLUE, 36.5549544, 31.9815362);
// }
// //////////////////////////////////////////////////////////////
//
// MainWindow::~MainWindow() {
//   gtk_window_destroy(GTK_WINDOW(m_window));
// }
// //////////////////////////////////////////////////////////////
//
// void MainWindow::add_marker(marker_color color, double lat, double lng) {
//   const char *paths[] = {
//       "/home/lezh1k/SRC/work/MDPet/mad-location-manager/data_generator/"
//       "mlm_data_generator/visualizator/resources/map-marker-green.png",
//
//       "/home/lezh1k/SRC/work/MDPet/mad-location-manager/data_generator/"
//       "mlm_data_generator/visualizator/resources/map-marker-red.png",
//
//       "/home/lezh1k/SRC/work/MDPet/mad-location-manager/data_generator/"
//       "mlm_data_generator/visualizator/resources/map-marker-blue.png",
//   };
//   ShumatePathLayer *layers[] = {m_map_path_layer_green, m_map_path_layer_red,
//                                 m_map_path_layer_blue};
//
//   GtkWidget *img = gtk_image_new_from_file(paths[color]);
//   ShumateMarker *marker = shumate_marker_new();
//   shumate_location_set_location(SHUMATE_LOCATION(marker), lat, lng);
//   shumate_marker_set_child(marker, img);
//   gtk_widget_set_size_request(GTK_WIDGET(marker), 48, 48);
//
//   shumate_marker_layer_add_marker(m_map_marker_layer, marker);
//   shumate_path_layer_add_node(layers[color], SHUMATE_LOCATION(marker));
// }
// //////////////////////////////////////////////////////////////
