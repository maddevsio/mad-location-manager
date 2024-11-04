#include "main_window.h"

#include <gtk/gtk.h>
#include <sensor_data.h>
#include <shumate/shumate.h>

#include <fstream>
#include <iostream>
#include <vector>

#include "mlm.h"
#include "sd_generator.h"
#include "w_filter_settings.h"
#include "w_generator_settings.h"

enum marker_type {
  MT_GPS_SET = 0,    // set by this tool OR received from GPS as is
  MT_GPS_GENERATED,  // generated (with noise etc.)
  MT_GPS_FILTERED,   // filtered by MLM
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
  w_generator_settings *w_gs;
  w_filter_settings *w_fs;
  std::vector<gmw_marker_layer> marker_layers;

  generator_main_window();
  ~generator_main_window();
};
//////////////////////////////////////////////////////////////

generator_main_window::generator_main_window()
    : window(nullptr),
      simple_map(nullptr),
      map_source_registry(nullptr),
      w_gs(nullptr),
      w_fs(nullptr)
{
}
//////////////////////////////////////////////////////////////

generator_main_window::~generator_main_window()
{
  /* g_clear_object(&map_source_registry); */
  g_object_unref(map_source_registry);
  if (w_gs)
    delete w_gs;
  if (w_fs)
    delete w_fs;
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

static void gmw_btn_save_tracks_clicked(GtkWidget *btn, gpointer ud);

/// gmw_btn_generate_sensor_data - generates sensor data and GPS data with noise
static void gmw_btn_generate_sensor_data_clicked(GtkWidget *btn, gpointer ud);
static void gmw_btn_clear_generated_data_clicked(GtkWidget *btn, gpointer ud);

/// gmw_btn_filter_data_clicked
static void gmw_btn_filter_sensor_data_clicked(GtkWidget *btn, gpointer ud);
static void gmw_btn_clear_filtered_data_cliecked(GtkWidget *btn, gpointer ud);

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
  gmw->w_fs = w_filter_settings_default();
  g_signal_connect(gmw->w_fs->btn_generate,
                   "clicked",
                   G_CALLBACK(gmw_btn_filter_sensor_data_clicked),
                   gmw);

  g_signal_connect(gmw->w_fs->btn_clear,
                   "clicked",
                   G_CALLBACK(gmw_btn_clear_filtered_data_cliecked),
                   gmw);

  return gmw->w_fs->frame;
}
//////////////////////////////////////////////////////////////

static GtkWidget *create_load_track_frame(generator_main_window *gmw)
{
  GtkWidget *btn_clear = gtk_button_new();
  GtkWidget *btn_load = gtk_button_new();
  GtkWidget *btn_save = gtk_button_new();

  gtk_button_set_label(GTK_BUTTON(btn_clear), "Clear ALL");
  g_signal_connect(btn_clear,
                   "clicked",
                   G_CALLBACK(gmw_btn_clear_all_points_clicked),
                   gmw);
  gtk_button_set_label(GTK_BUTTON(btn_load), "Load");
  g_signal_connect(btn_load,
                   "clicked",
                   G_CALLBACK(gmw_btn_load_track_clicked),
                   gmw);
  gtk_button_set_label(GTK_BUTTON(btn_save), "Save");
  g_signal_connect(btn_save,
                   "clicked",
                   G_CALLBACK(gmw_btn_save_tracks_clicked),
                   gmw);

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
  gmw->w_gs = w_generator_settings_default();
  g_signal_connect(gmw->w_gs->btn_generate,
                   "clicked",
                   G_CALLBACK(gmw_btn_generate_sensor_data_clicked),
                   gmw);

  g_signal_connect(gmw->w_gs->btn_clear,
                   "clicked",
                   G_CALLBACK(gmw_btn_clear_generated_data_clicked),
                   gmw);

  return gmw->w_gs->frame;
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
static const int marker_sizes[] = {24, 16, 20};
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
  rec.hdr = sd_record_hdr(SD_GPS_SET, 0.);
  rec.data.gps.location = geopoint(lat, lng);
  rec.data.gps.speed = gps_speed(0., 0., 0.);
  gmw->marker_layers[MT_GPS_SET].lst_sd_records.push_back(rec);
  gmw_add_marker(gmw, MT_GPS_SET, lat, lng);
}
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
  std::ifstream fs_in(fpath, std::ios_base::in);
  if (!fs_in.is_open()) {
    std::cerr << "unable to open file " << fpath << std::endl;
    return;
  }

  std::string line;
  while (std::getline(fs_in, line)) {
    sd_record rec;
    sdr_deserialize_error derr = sdr_deserialize_str(line, rec);

    if (derr != SDRDE_SUCCESS) {
      std::cerr << derr << "->  failed to process line: " << line << std::endl;
      continue;
    }

    // todo array of handlers
    switch (rec.hdr.type) {
      case SD_GPS_SET: {
        gmw->marker_layers[MT_GPS_SET].lst_sd_records.push_back(rec);
        gmw_add_marker(gmw,
                       MT_GPS_SET,
                       rec.data.gps.location.latitude,
                       rec.data.gps.location.longitude);
        break;
      }
      default: {
        break;  // do nothing for now
      }
    }  // switch
  }  // while (getline())
}  // dlg_load_track_cb
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

  // todo add filtered layer here later
  marker_type export_layers[] = {MT_GPS_SET, MT_GPS_GENERATED, MT_COUNT};

  for (int i = 0; export_layers[i] != MT_COUNT; ++i) {
    for (auto rec : gmw->marker_layers[export_layers[i]].lst_sd_records) {
      of << sdr_serialize_str(rec) << std::endl;
    }
  }
  of.close();
}
//////////////////////////////////////////////////////////////

void gmw_btn_save_tracks_clicked(GtkWidget *btn, gpointer ud)
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

void gmw_btn_generate_sensor_data_clicked(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  const std::vector<sd_record> &src =
      gmw->marker_layers[MT_GPS_SET].lst_sd_records;
  std::vector<sd_record> &dst =
      gmw->marker_layers[MT_GPS_GENERATED].lst_sd_records;

  if (src.empty()) {
    return;  // do nothing
  }
  generator_options go = gmw->w_gs->opts;

  dst.push_back(
      sd_record(sd_record_hdr(SD_GPS_GENERATED, 0.), src.front().data.gps));

  gmw_add_marker(gmw,
                 MT_GPS_GENERATED,
                 src.front().data.gps.location.latitude,
                 src.front().data.gps.location.longitude);

  double ts = 0.;
  for (size_t i = 1; i < src.size(); ++i) {
    const sd_record &prev_rec = src[i - 1];
    const sd_record &curr_rec = src[i];

    for (double ats = 0.; ats < go.gps_measurement_period;
         ats += go.acc_measurement_period) {
      abs_accelerometer acc =
          sd_abs_acc_between_two_geopoints(prev_rec.data.gps,
                                           curr_rec.data.gps,
                                           go.acceleration_time,
                                           go.gps_measurement_period,
                                           ats);
      acc = sd_noised_acc(acc, go.acc_noise);
      dst.push_back(
          sd_record(sd_record_hdr(SD_ACC_ABS_GENERATED, ts + ats), acc));
    }  // finished generating accelerometer data

    // need to generate ideal coordinate and then noise it
    gps_coordinate cc = prev_rec.data.gps;
    abs_accelerometer acc =
        sd_abs_acc_between_two_geopoints(prev_rec.data.gps,
                                         curr_rec.data.gps,
                                         go.acceleration_time,
                                         go.gps_measurement_period,
                                         0.);

    double no_acc_time = go.gps_measurement_period - go.acceleration_time;
    movement_interval acc_interval(acc.cartezian_angle(),
                                   acc.acceleration(),
                                   go.acceleration_time);
    movement_interval no_acc_interval(0., 0., no_acc_time);

    cc = sd_gps_coordinate_in_interval(cc, acc_interval, go.acceleration_time);
    cc = sd_gps_coordinate_in_interval(cc, no_acc_interval, no_acc_time);
    cc.location = sd_noised_geopoint(cc.location, go.gps_location_noise);
    ts += go.gps_measurement_period;
    dst.push_back(sd_record(sd_record_hdr(SD_GPS_GENERATED, ts), cc));
    gmw_add_marker(gmw,
                   MT_GPS_GENERATED,
                   cc.location.latitude,
                   cc.location.longitude);
  }
}
//////////////////////////////////////////////////////////////

void gmw_btn_clear_generated_data_clicked(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  int li = MT_GPS_GENERATED;
  shumate_path_layer_remove_all(gmw->marker_layers[li].path_layer);
  shumate_marker_layer_remove_all(gmw->marker_layers[li].marker_layer);
  gmw->marker_layers[li].lst_sd_records.clear();
}
//////////////////////////////////////////////////////////////

void gmw_btn_filter_sensor_data_clicked(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  const std::vector<sd_record> &src =
      gmw->marker_layers[MT_GPS_GENERATED].lst_sd_records;
  std::vector<sd_record> &dst =
      gmw->marker_layers[MT_GPS_FILTERED].lst_sd_records;

  if (src.empty()) {
    return;  // do nothing
  }

  MLM mlm;
  for (const sd_record &rec : src) {
    switch (rec.hdr.type) {
      case SD_ACC_ABS_GENERATED: {
        mlm.process_acc_data(rec.data.acc, rec.hdr.timestamp);
        gps_coordinate pc = mlm.predicted_coordinate();
        gmw_add_marker(gmw,
                       MT_GPS_FILTERED,
                       pc.location.latitude,
                       pc.location.longitude);
        break;
      }
      case SD_GPS_GENERATED: {
        mlm.process_gps_data(rec.data.gps,
                             rec.data.gps.location.error,
                             1e-6);  // small artifitial noise
        /* gps_coordinate pc = mlm.predicted_coordinate(); */
        /* gmw_add_marker(gmw, */
        /*                MT_GPS_FILTERED, */
        /*                pc.location.latitude, */
        /*                pc.location.longitude); */
        break;
      }
      default: {
        // do nothing
        break;
      }
    }
  }

  /* std::cout << "AZAZA MLM FILTER\n"; */
}
//////////////////////////////////////////////////////////////

void gmw_btn_clear_filtered_data_cliecked(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  int li = MT_GPS_FILTERED;
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
  int ms = marker_sizes[mt];
  gtk_widget_set_size_request(GTK_WIDGET(marker), ms, ms);

  shumate_marker_layer_add_marker(gmw->marker_layers[mt].marker_layer, marker);
  shumate_path_layer_add_node(gmw->marker_layers[mt].path_layer,
                              SHUMATE_LOCATION(marker));
}
/////////////////////////////////////////////////////////////
