#include "main_window.h"

#include <iostream>

static void gmw_btn_add_next_point_clicked(GtkWidget *btn, gpointer ud);
static void gmw_btn_clear_all_points_clicked(GtkWidget *btn, gpointer ud);
static void gmw_simple_map_gesture_click_released(GtkGestureClick *gesture,
                                                  int n_press, double x,
                                                  double y, gpointer user_data);

generator_main_window::generator_main_window()
    : window(nullptr),
      simple_map(nullptr),
      map_source_registry(nullptr),
      map_marker_layer(nullptr) {}
//////////////////////////////////////////////////////////////

generator_main_window::~generator_main_window() {
  std::cout << "~generator_main_window\n";
  g_clear_object(&map_source_registry);
}
//////////////////////////////////////////////////////////////

void gmw_bind_to_app(GtkApplication *app, generator_main_window *gmw) {
  // window
  gmw->window = gtk_application_window_new(app);
  gtk_window_set_title(GTK_WINDOW(gmw->window), "Trajectory generator");
  gtk_window_set_default_size(GTK_WINDOW(gmw->window), 800, 600);

  // buttons
  GtkWidget *btn_add_next_point = gtk_button_new();
  gtk_button_set_label(GTK_BUTTON(btn_add_next_point), "Generate next point");
  g_signal_connect(btn_add_next_point, "clicked",
                   G_CALLBACK(gmw_btn_add_next_point_clicked), gmw);

  GtkWidget *btn_clear_all_points = gtk_button_new();
  gtk_button_set_label(GTK_BUTTON(btn_clear_all_points), "Clear");
  g_signal_connect(btn_clear_all_points, "clicked",
                   G_CALLBACK(gmw_btn_clear_all_points_clicked), gmw);

  // map
  gmw->simple_map = shumate_simple_map_new();
  gmw->map_source_registry = shumate_map_source_registry_new_with_defaults();
  ShumateMapSource *ms = shumate_map_source_registry_get_by_id(
      gmw->map_source_registry, SHUMATE_MAP_SOURCE_OSM_MAPNIK);
  shumate_simple_map_set_map_source(gmw->simple_map, ms);

  ShumateViewport *vp = shumate_simple_map_get_viewport(gmw->simple_map);
  shumate_viewport_set_zoom_level(vp, 16.0);

  ShumateMap *map = shumate_simple_map_get_map(gmw->simple_map);
  shumate_map_center_on(map, 36.5519514, 31.9801362);
  // shumate_map_go_to_full(map, 36.5519514, 31.9801362, 14.0);

  gmw->map_marker_layer = shumate_marker_layer_new(vp);

  for (size_t i = 0; i < MC_COUNT; ++i) {
    gmw->map_path_layers[i] = shumate_path_layer_new(vp);
    shumate_simple_map_add_overlay_layer(
        gmw->simple_map, SHUMATE_LAYER(gmw->map_path_layers[i]));
  }

  shumate_simple_map_add_overlay_layer(gmw->simple_map,
                                       SHUMATE_LAYER(gmw->map_marker_layer));

  // todo move to gmv and free in destructor to avoid memory leak
  GtkGestureClick *ggc = GTK_GESTURE_CLICK(gtk_gesture_click_new());
  gtk_widget_add_controller(GTK_WIDGET(gmw->simple_map),
                            GTK_EVENT_CONTROLLER(ggc));
  g_signal_connect(ggc, "pressed",
                   G_CALLBACK(gmw_simple_map_gesture_click_released), gmw);

  // grid
  GtkWidget *grid = gtk_grid_new();
  gtk_grid_set_column_spacing(GTK_GRID(grid), 10);
  gtk_grid_set_row_spacing(GTK_GRID(grid), 10);
  gtk_widget_set_hexpand(GTK_WIDGET(grid), true);
  gtk_widget_set_hexpand(GTK_WIDGET(grid), true);

  gtk_grid_attach(GTK_GRID(grid), btn_add_next_point, 0, 0, 1, 1);
  gtk_grid_attach(GTK_GRID(grid), btn_clear_all_points, 1, 0, 1, 1);
  gtk_grid_attach(GTK_GRID(grid), GTK_WIDGET(gmw->simple_map), 0, 1, 2, 2);

  // set grid as child of main window
  gtk_window_set_child(GTK_WINDOW(gmw->window), grid);
}
//////////////////////////////////////////////////////////////

void gmw_simple_map_gesture_click_released(GtkGestureClick *gesture,
                                           int n_press, double x, double y,
                                           gpointer user_data) {
  generator_main_window *gmw = reinterpret_cast<generator_main_window*>(user_data);
  ShumateViewport *vp = shumate_simple_map_get_viewport(gmw->simple_map);
  double lat, lng;
  shumate_viewport_widget_coords_to_location(vp, GTK_WIDGET(gmw->simple_map), x, y, &lat, &lng);
  gmw_add_marker(gmw, MC_RED, lat, lng);
}
//////////////////////////////////////////////////////////////

void gmw_btn_clear_all_points_clicked(GtkWidget *btn, gpointer ud) {
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  for (int i = 0; i < MC_COUNT; ++i) {
    shumate_path_layer_remove_all(gmw->map_path_layers[i]);
    shumate_marker_layer_remove_all(gmw->map_marker_layer);
  }
}
//////////////////////////////////////////////////////////////

void gmw_btn_add_next_point_clicked(GtkWidget *btn, gpointer ud) {
  (void)btn;
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  struct tst {
    marker_color color;
    double latitude;
    double longitude;
  };
  // clang-format off
  static const tst markers[] = {
    {MC_RED, 36.5519514, 31.9801362},
    {MC_RED, 36.5519524, 31.9813362},
    {MC_RED, 36.5519534, 31.9823362},
    {MC_RED, 36.5519544, 31.9834362},

    {MC_BLUE, 36.5519514, 31.9815362},
    {MC_BLUE, 36.5529524, 31.9815362},
    {MC_BLUE, 36.5539534, 31.9815362},
    {MC_BLUE, 36.5549544, 31.9815362},

    {MC_GREEN, 36.5559514, 31.9815362},
    {MC_GREEN, 36.5569524, 31.9815362},
    {MC_GREEN, 36.5579534, 31.9815362},
    {MC_GREEN, 36.5589544, 31.9815362},

    {MC_COUNT, 0.0, 0.0},
  };
  // clang-format on
  static const tst *m = markers;
  gmw_add_marker(gmw, m->color, m->latitude, m->longitude);
  if ((++m)->color == MC_COUNT) {
    m = markers;
  }
}
//////////////////////////////////////////////////////////////

void gmw_add_marker(generator_main_window *gmw, marker_color mc,
                    double latitude, double longitude) {
  const char *paths[] = {
      "/home/lezh1k/SRC/work/MDPet/mad-location-manager/data_generator/"
      "mlm_data_generator/visualizator/resources/map-marker-green.png",

      "/home/lezh1k/SRC/work/MDPet/mad-location-manager/data_generator/"
      "mlm_data_generator/visualizator/resources/map-marker-red.png",

      "/home/lezh1k/SRC/work/MDPet/mad-location-manager/data_generator/"
      "mlm_data_generator/visualizator/resources/map-marker-blue.png",
  };
  GtkWidget *img = gtk_image_new_from_file(paths[mc]);
  ShumateMarker *marker = shumate_marker_new();
  shumate_location_set_location(SHUMATE_LOCATION(marker), latitude, longitude);
  shumate_marker_set_child(marker, img);
  gtk_widget_set_size_request(GTK_WIDGET(marker), 48, 48);

  shumate_marker_layer_add_marker(gmw->map_marker_layer, marker);
  shumate_path_layer_add_node(gmw->map_path_layers[mc],
                              SHUMATE_LOCATION(marker));
}
//////////////////////////////////////////////////////////////
