#include "main_window.h"

#include <gtk/gtk.h>
#include <sensor_data.h>
#include <shumate/shumate.h>

#include <fstream>
#include <iostream>
#include <vector>

#include "w_generator_settings.h"

enum marker_type {
  MT_GPS_SET = 0,    // set by this tool OR received from GPS as is
  MT_GPS_FILTERED,   // filtered by MLM
  MT_GPS_GENERATED,  // generated (with noise etc.)
  MT_COUNT
};
//////////////////////////////////////////////////////////////

struct gmw_marker_layer {
  ShumateMarkerLayer *marker_layer;
  ShumatePathLayer *path_layer;
  std::vector<sd_record> lst_sd_records;

  gmw_marker_layer() : marker_layer(nullptr), path_layer(nullptr) {}
  gmw_marker_layer(ShumateMarkerLayer *marker_layer,
                   ShumatePathLayer *path_layer)
      : marker_layer(marker_layer), path_layer(path_layer)
  {
  }
};
//////////////////////////////////////////////////////////////

struct generator_main_window {
  GtkWidget *window;
  ShumateSimpleMap *simple_map;
  ShumateMapSourceRegistry *map_source_registry;
  std::vector<gmw_marker_layer> marker_layers;

  generator_main_window();
  ~generator_main_window();
};
//////////////////////////////////////////////////////////////

generator_main_window::generator_main_window()
    : window(nullptr), simple_map(nullptr), map_source_registry(nullptr)
{
}
//////////////////////////////////////////////////////////////

generator_main_window::~generator_main_window()
{
  /* g_clear_object(&map_source_registry); */
  g_object_unref(map_source_registry);
}
//////////////////////////////////////////////////////////////

generator_main_window *gmw_create()
{
  return new generator_main_window();
}
//////////////////////////////////////////////////////////////

void gmw_free(generator_main_window *gmw)
{
  delete gmw;
}
//////////////////////////////////////////////////////////////

static void gmw_btn_save_tracks(GtkWidget *btn, gpointer ud);

static void gmw_btn_generate_sensor_data(GtkWidget *btn, gpointer ud);
static void gmw_btn_clear_generated_data(GtkWidget *btn, gpointer ud);

static void gmw_btn_clear_all_points_clicked(GtkWidget *btn, gpointer ud);
static void gmw_btn_load_track_clicked(GtkWidget *btn, gpointer ud);
static void gmw_simple_map_gesture_click_released(GtkGestureClick *gesture,
                                                  int n_press,
                                                  double x,
                                                  double y,
                                                  gpointer user_data);

static void gmw_add_marker(generator_main_window *gmw,
                           marker_type mt,
                           double latitude,
                           double longitude);
//////////////////////////////////////////////////////////////

void gmw_show(generator_main_window *gmw)
{
  gtk_widget_set_visible(GTK_WIDGET(gmw->window), true);
}
//////////////////////////////////////////////////////////////

static GtkWidget *create_filter_settings_frame(generator_main_window *gmw)
{
  // todo implement
  UNUSED(gmw);  // for now
  GtkWidget *frame = gtk_frame_new("Filter settings");
  gtk_frame_set_label_align(GTK_FRAME(frame), 0.5);
  return frame;
}
//////////////////////////////////////////////////////////////

static GtkWidget *create_load_track_frame(generator_main_window *gmw)
{
  GtkWidget *btn_load = gtk_button_new();
  gtk_button_set_label(GTK_BUTTON(btn_load), "Load");
  g_signal_connect(btn_load,
                   "clicked",
                   G_CALLBACK(gmw_btn_load_track_clicked),
                   gmw);

  GtkWidget *btn_clear = gtk_button_new();
  gtk_button_set_label(GTK_BUTTON(btn_clear), "Clear ALL");
  g_signal_connect(btn_clear,
                   "clicked",
                   G_CALLBACK(gmw_btn_clear_all_points_clicked),
                   gmw);

  GtkWidget *btn_save = gtk_button_new();
  gtk_button_set_label(GTK_BUTTON(btn_save), "Save");
  g_signal_connect(btn_save, "clicked", G_CALLBACK(gmw_btn_save_tracks), gmw);

  GtkWidget *grid = gtk_grid_new();
  gtk_grid_set_column_spacing(GTK_GRID(grid), 10);
  gtk_grid_set_row_spacing(GTK_GRID(grid), 10);

  gtk_grid_set_row_homogeneous(GTK_GRID(grid), true);
  gtk_grid_set_column_homogeneous(GTK_GRID(grid), true);

  gtk_grid_attach(GTK_GRID(grid), btn_clear, 0, 0, 1, 1);
  gtk_grid_attach(GTK_GRID(grid), btn_load, 0, 1, 1, 1);
  gtk_grid_attach(GTK_GRID(grid), btn_save, 0, 2, 1, 1);

  GtkWidget *frame = gtk_frame_new("Current track");
  gtk_frame_set_child(GTK_FRAME(frame), grid);
  gtk_frame_set_label_align(GTK_FRAME(frame), 0.5);
  return frame;
}
//////////////////////////////////////////////////////////////

static GtkWidget *create_generator_settings_frame(generator_main_window *gmw)
{
  // TODO move w_gs to gmw fields and free in destructor
  w_generator_settings *w_gs = w_generator_settings_default();
  g_signal_connect(w_gs->btn_generate,
                   "clicked",
                   G_CALLBACK(gmw_btn_generate_sensor_data),
                   gmw);

  g_signal_connect(w_gs->btn_clear,
                   "clicked",
                   G_CALLBACK(gmw_btn_clear_generated_data),
                   gmw);

  return w_gs->frame;
}
//////////////////////////////////////////////////////////////

// todo move somewhere
static const gchar *marker_css_data =
    ".red-shumate-marker {"
    "   background-color: red;"
    "}"
    ".green-shumate-marker {"
    "   background-color: green;"
    "}"
    ".blue-shumate-marker {"
    "   background-color: blue;"
    "}";
static const char *marker_css_classes[] = {
    "green-shumate-marker",
    "red-shumate-marker",
    "blue-shumate-marker",
};
//////////////////////////////////////////////////////////////

void gmw_bind_to_app(GtkApplication *app, generator_main_window *gmw)
{
  // register css styles (for markers)
  GtkCssProvider *css_provider = gtk_css_provider_new();
  gtk_css_provider_load_from_string(css_provider, marker_css_data);
  gtk_style_context_add_provider_for_display(gdk_display_get_default(),
                                             GTK_STYLE_PROVIDER(css_provider),
                                             GTK_STYLE_PROVIDER_PRIORITY_USER);
  g_object_unref(css_provider);

  // window
  gmw->window = gtk_application_window_new(app);
  gtk_window_set_title(GTK_WINDOW(gmw->window),
                       "MLM filter test and visualizer tool");
  gtk_window_set_default_size(GTK_WINDOW(gmw->window), 800, 600);

  // map
  gmw->simple_map = shumate_simple_map_new();
  gmw->map_source_registry = shumate_map_source_registry_new_with_defaults();
  ShumateMapSource *ms =
      shumate_map_source_registry_get_by_id(gmw->map_source_registry,
                                            SHUMATE_MAP_SOURCE_OSM_MAPNIK);

  shumate_simple_map_set_map_source(gmw->simple_map, ms);

  ShumateViewport *vp = shumate_simple_map_get_viewport(gmw->simple_map);
  shumate_viewport_set_zoom_level(vp, 16.0);

  for (size_t i = 0; i < MT_COUNT; ++i) {
    ShumateMarkerLayer *marker_layer = shumate_marker_layer_new(vp);
    ShumatePathLayer *path_layer = shumate_path_layer_new(vp);

    shumate_simple_map_add_overlay_layer(gmw->simple_map,
                                         SHUMATE_LAYER(path_layer));
    shumate_simple_map_add_overlay_layer(gmw->simple_map,
                                         SHUMATE_LAYER(marker_layer));
    gmw->marker_layers.push_back(gmw_marker_layer(marker_layer, path_layer));
  }

  ShumateMap *map = shumate_simple_map_get_map(gmw->simple_map);
  shumate_map_center_on(map, 36.5519514, 31.9801362);

  GtkGestureClick *ggc = GTK_GESTURE_CLICK(gtk_gesture_click_new());
  gtk_widget_add_controller(GTK_WIDGET(gmw->simple_map),
                            GTK_EVENT_CONTROLLER(ggc));
  g_signal_connect(ggc,
                   "released",
                   G_CALLBACK(gmw_simple_map_gesture_click_released),
                   gmw);

  // control frames
  GtkWidget *frame_generator = create_generator_settings_frame(gmw);
  GtkWidget *frame_filter = create_filter_settings_frame(gmw);
  GtkWidget *frame_load_track = create_load_track_frame(gmw);

  // main grid
  GtkWidget *grid_main = gtk_grid_new();
  gtk_grid_set_column_spacing(GTK_GRID(grid_main), 10);
  gtk_grid_set_row_spacing(GTK_GRID(grid_main), 10);

  gtk_grid_set_row_homogeneous(GTK_GRID(grid_main), true);
  gtk_grid_set_column_homogeneous(GTK_GRID(grid_main), true);

  gtk_grid_attach(GTK_GRID(grid_main), GTK_WIDGET(gmw->simple_map), 0, 0, 3, 5);
  gtk_grid_attach(GTK_GRID(grid_main), frame_generator, 3, 0, 1, 2);
  gtk_grid_attach(GTK_GRID(grid_main), frame_filter, 3, 2, 1, 2);
  gtk_grid_attach(GTK_GRID(grid_main), frame_load_track, 3, 4, 1, 1);

  // set grid as child of main window
  gtk_window_set_child(GTK_WINDOW(gmw->window), grid_main);
}
//////////////////////////////////////////////////////////////

void gmw_simple_map_gesture_click_released(GtkGestureClick *gesture,
                                           int n_press,
                                           double x,
                                           double y,
                                           gpointer user_data)
{
  if (n_press > 1)
    return;  // do nothing
  GdkModifierType mt = gtk_event_controller_get_current_event_state(
      GTK_EVENT_CONTROLLER(gesture));
  if (!(mt & GDK_CONTROL_MASK))
    return;  // add marker only with ctrl
  generator_main_window *gmw =
      reinterpret_cast<generator_main_window *>(user_data);
  ShumateViewport *vp = shumate_simple_map_get_viewport(gmw->simple_map);
  double lat, lng;
  shumate_viewport_widget_coords_to_location(vp,
                                             GTK_WIDGET(gmw->simple_map),
                                             x,
                                             y,
                                             &lat,
                                             &lng);

  sd_record rec;
  rec.hdr = sd_record_hdr(SD_GPS_MEASURED, 0.);
  rec.data.gps.location = geopoint(lat, lng);
  rec.data.gps.speed = gps_speed(0., 0., 0.);
  gmw->marker_layers[MT_GPS_SET].lst_sd_records.push_back(rec);
  gmw_add_marker(gmw, MT_GPS_SET, lat, lng);
}
//////////////////////////////////////////////////////////////

static bool get_sd_record_hdr(sd_record_hdr &hdr, const char *line)
{
  return sscanf(line, "%d: %lf", &hdr.type, &hdr.timestamp) == 2;
}
//////////////////////////////////////////////////////////////

static bool acc_record_handler(generator_main_window *gmw,
                               sd_record_hdr &hdr,
                               const char *line)
{
  UNUSED(gmw);
  UNUSED(hdr);
  UNUSED(line);
  return true;
};
//////////////////////////////////////////////////////////////

static bool gps_record_handler(generator_main_window *gmw,
                               sd_record_hdr &hdr,
                               const char *line)
{
  return false;
  /* gps_coordinate gps; */
  /* bool parsed = sd_gps_deserialize_str(line, hdr, gps); */
  /* if (!parsed) */
  /*   return false; */
  /*  */
  /* marker_type mt = MT_GPS_MEASURED; */
  /* if (hdr.type == SD_GPS_CORRECTED) */
  /*   mt = MT_GPS_CORRECTED; */
  /* else if (hdr.type == SD_GPS_MEASURED) */
  /*   mt = MT_GPS_MEASURED; */
  /* else if (hdr.type == SD_GPS_NOISED) */
  /*   mt = MT_GPS_NOISED; */
  /*  */
  /* gmw_add_marker(gmw, mt, gps.location.latitude, gps.location.longitude); */
  /* return true; */
};
//////////////////////////////////////////////////////////////

static void dlg_load_track_cb(GObject *source_object,
                              GAsyncResult *res,
                              gpointer data)
{
  GtkFileDialog *dlg = reinterpret_cast<GtkFileDialog *>(source_object);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(data);
  GFile *g_file = gtk_file_dialog_open_finish(dlg, res, NULL);
  if (!g_file) {
    // cancel button pressed
    return;
  }

  char *fpath = g_file_get_path(g_file);
  FILE *f_track = fopen(fpath, "r");
  if (!f_track) {
    // todo log
    return;
  }

  sd_record_hdr hdr;
  char *line = NULL;
  size_t len = 0;
  ssize_t nread;

  // SEE SD_RECORD_TYPE enum
  bool (*record_handlers[])(generator_main_window *,
                            sd_record_hdr &,
                            const char *){
      acc_record_handler,
      acc_record_handler,
      gps_record_handler,
      gps_record_handler,
      gps_record_handler,
  };

  while ((nread = getline(&line, &len, f_track)) != -1) {
    if (!get_sd_record_hdr(hdr, line)) {
      std::cerr << line << "\nis not a header\n";
      break;
    }

    int record_type = line[0] - '0';
    if (record_type < 0 || record_type >= SD_UNKNOWN) {
      std::cerr << "unknown record type\n";
      continue;
    }

    bool handled = record_handlers[record_type](gmw, hdr, line);
    if (handled)
      continue;  // do nothing

    std::cerr << "failed to handle record: " << line << std::endl;
  }

  free(line);
  fclose(f_track);
}
//////////////////////////////////////////////////////////////

void gmw_btn_load_track_clicked(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  GtkFileDialog *dlg = gtk_file_dialog_new();
  gtk_file_dialog_open(dlg,
                       GTK_WINDOW(gmw->window),
                       NULL,
                       dlg_load_track_cb,
                       gmw);
  g_object_unref(dlg);
}
//////////////////////////////////////////////////////////////

void gmw_btn_clear_all_points_clicked(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);

  for (int i = 0; i < MT_COUNT; ++i) {
    shumate_path_layer_remove_all(gmw->marker_layers[i].path_layer);
    shumate_marker_layer_remove_all(gmw->marker_layers[i].marker_layer);
    gmw->marker_layers[i].lst_sd_records.clear();
  }
}
//////////////////////////////////////////////////////////////

static void dlg_save_tracks_cb(GObject *source_object,
                               GAsyncResult *res,
                               gpointer data)
{
  GtkFileDialog *dlg = reinterpret_cast<GtkFileDialog *>(source_object);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(data);
  GFile *file = gtk_file_dialog_save_finish(dlg, res, NULL);
  if (!file) {
    // cancel button pressed
    return;
  }

  char *fpath = g_file_get_path(file);
  std::ofstream of(fpath);
  if (!of.is_open()) {
    std::cerr << "error opening file\n" << fpath << std::endl;
    return;
  }

  for (auto rec : gmw->marker_layers[MT_GPS_SET].lst_sd_records) {
    of << sdr_serialize_str(rec) << std::endl;
  }
  of.close();
}
//////////////////////////////////////////////////////////////

void gmw_btn_save_tracks(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  if (gmw->marker_layers[MT_GPS_SET].lst_sd_records.empty()) {
    return;  // do nothing
  }

  GtkFileDialog *dlg = gtk_file_dialog_new();
  gtk_file_dialog_save(dlg,
                       GTK_WINDOW(gmw->window),
                       NULL,
                       dlg_save_tracks_cb,
                       gmw);
  /* g_clear_object(&dlg); */
  g_object_unref(dlg);
}
//////////////////////////////////////////////////////////////

void gmw_btn_generate_sensor_data(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  if (gmw->marker_layers[MT_GPS_SET].lst_sd_records.empty()) {
    return;  // do nothing
  }
  // TODO generate data + update some model
  std::cout << "gmw_btn_generate_sensor_data\n";
}
//////////////////////////////////////////////////////////////

void gmw_btn_clear_generated_data(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  int li = MT_GPS_GENERATED;
  shumate_path_layer_remove_all(gmw->marker_layers[li].path_layer);
  shumate_marker_layer_remove_all(gmw->marker_layers[li].marker_layer);
  gmw->marker_layers[li].lst_sd_records.clear();
}
//////////////////////////////////////////////////////////////

void gmw_add_marker(generator_main_window *gmw,
                    marker_type mt,
                    double latitude,
                    double longitude)
{
  ShumateMarker *marker = shumate_point_new();
  shumate_location_set_location(SHUMATE_LOCATION(marker), latitude, longitude);

  const char *marker_classes[] = {marker_css_classes[mt], NULL};
  gtk_widget_set_css_classes(GTK_WIDGET(marker), marker_classes);
  gtk_widget_set_size_request(GTK_WIDGET(marker), 18, 18);

  shumate_marker_layer_add_marker(gmw->marker_layers[mt].marker_layer, marker);
  shumate_path_layer_add_node(gmw->marker_layers[mt].path_layer,
                              SHUMATE_LOCATION(marker));
}
/////////////////////////////////////////////////////////////
