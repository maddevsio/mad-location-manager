#include "main_window.h"

#include <gtk/gtk.h>
#include <sensor_data.h>
#include <shumate/shumate.h>

#include <fstream>
#include <iostream>
#include <vector>

#include "low_pass.h"
#include "mlm.h"
#include "sd_generator.h"
#include "w_filter_settings.h"
#include "w_generator_settings.h"

enum marker_type {
  MT_GPS_SET = 0,             // set by this tool OR received from GPS as is
  MT_GPS_GENERATED,           // generated (with noise etc.)
  MT_GPS_FILTERED_PREDICTED,  // filtered by MLM, current state after predict
  MT_GPS_FILTERED_UPDATED,    // filtered by MLM, current state after update
  MT_COUNT
};

static const gchar *gmw_css_data =
    ".red-shumate-marker {"
    "   background-color: red;"
    "}"
    ".green-shumate-marker {"
    "   background-color: green;"
    "}"
    ".blue-shumate-marker {"
    "   background-color: blue;"
    "}"
    ".orange-shumate-marker {"
    "   background-color: orange;"
    "}"
    ".label-red {"
    "   color: red;"
    "}"
    ".label-green {"
    "   color: green;"
    "}"
    ".label-blue {"
    "   color: blue;"
    "}"
    ".label-orange {"
    "   color: orange;"
    "}";

static const char *marker_css_classes[] = {"green-shumate-marker",
                                           "red-shumate-marker",
                                           "blue-shumate-marker",
                                           "orange-shumate-marker"};
static const char *label_css_classes[] = {"label-green",
                                          "label-red",
                                          "label-blue",
                                          "label-orange"};
static const int marker_sizes[] = {24, 16, 8, 20};
//////////////////////////////////////////////////////////////

struct gmw_layer {
  ShumateMarkerLayer *marker_layer;
  ShumatePathLayer *path_layer;
  std::vector<sd_record> lst_sd_records;

  size_t last_gps_idx;
  double distance;
  GtkWidget *lbl;

  gmw_layer()
      : marker_layer(nullptr),
        path_layer(nullptr),
        last_gps_idx(-1),
        distance(0.0)
  {
  }
  gmw_layer(ShumateMarkerLayer *marker_layer, ShumatePathLayer *path_layer)
      : marker_layer(marker_layer),
        path_layer(path_layer),
        last_gps_idx(-1),
        distance(0.0)
  {
  }

  void add_record(sd_record rec)
  {
    lst_sd_records.push_back(rec);

    // now we want calculate distance for GPS (todo move to separate method)
    static sensor_data_record_type gps[] = {SD_GPS_SET,
                                            SD_GPS_FILTERED,
                                            SD_GPS_GENERATED,
                                            SD_UNKNOWN};

    bool is_gps = false;
    for (int i = 0; gps[i] != SD_UNKNOWN; ++i) {
      if (rec.hdr.type != gps[i])
        continue;
      is_gps = true;
      break;
    }

    if (!is_gps) {
      return;
    }

    if (last_gps_idx == static_cast<size_t>(-1)) {
      last_gps_idx = lst_sd_records.size() - 1;
      return;
    }

    distance +=
        sd_distance_between_two_points(lst_sd_records[last_gps_idx].data.gps,
                                       rec.data.gps);
    last_gps_idx = lst_sd_records.size() - 1;
  }

  void clear()
  {
    lst_sd_records.clear();
    last_gps_idx = -1;
    distance = 0.;
  }
};
//////////////////////////////////////////////////////////////

struct generator_main_window {
  GtkWidget *window;
  ShumateSimpleMap *simple_map;
  ShumateMapSourceRegistry *map_source_registry;
  w_generator_settings *w_gs;
  w_filter_settings *w_fs;
  std::vector<gmw_layer> layers;

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

void gmw_show(generator_main_window *gmw)
{
  gtk_widget_set_visible(GTK_WIDGET(gmw->window), true);
}

static void gmw_set_center(generator_main_window *gmw,
                           double latitude,
                           double longitude)
{
  ShumateMap *map = shumate_simple_map_get_map(gmw->simple_map);
  shumate_map_center_on(map, latitude, longitude);
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////

// SLOTS
static void gmw_btn_save_tracks_clicked(GtkWidget *btn, gpointer ud);
static void dlg_save_tracks_cb(GObject *source_object,
                               GAsyncResult *res,
                               gpointer data);

/// gmw_btn_generate_sensor_data - generates sensor data and GPS data with noise
static void gmw_btn_generate_sensor_data_clicked(GtkWidget *btn, gpointer ud);
static void gmw_btn_clear_generated_data_clicked(GtkWidget *btn, gpointer ud);

/// gmw_btn_filter_data_clicked
static void gmw_btn_filter_sensor_data_clicked(GtkWidget *btn, gpointer ud);
static void gmw_btn_clear_filtered_data_clicked(GtkWidget *btn, gpointer ud);

///
static void gmw_btn_clear_all_points_clicked(GtkWidget *btn, gpointer ud);
static void gmw_btn_load_track_clicked(GtkWidget *btn, gpointer ud);
static void dlg_load_track_cb(GObject *source_object,
                              GAsyncResult *res,
                              gpointer data);

/// checkpoints
static void gmw_chk_layer_set_toggled(GtkCheckButton *self, gpointer user_data);
static void gmw_chk_layer_generated_toggled(GtkCheckButton *self,
                                            gpointer user_data);
static void gmw_chk_layer_filtered_gps_toggled(GtkCheckButton *self,
                                               gpointer user_data);
static void gmw_chk_layer_filtered_acc_toggled(GtkCheckButton *self,
                                               gpointer user_data);

static void gmw_simple_map_gesture_click_released(GtkGestureClick *gesture,
                                                  int n_press,
                                                  double x,
                                                  double y,
                                                  gpointer user_data);

//////////////////////////////////////////////////////////////
// private methods
static GtkWidget *create_filter_settings_frame(generator_main_window *gmw);
static GtkWidget *create_load_track_frame(generator_main_window *gmw);
static GtkWidget *create_layers_visibility_and_distance_frame(
    generator_main_window *gmw);
static GtkWidget *create_generator_settings_frame(generator_main_window *gmw);

static void gmw_add_marker(generator_main_window *gmw,
                           marker_type mt,
                           double latitude,
                           double longitude);
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////

GtkWidget *create_filter_settings_frame(generator_main_window *gmw)
{
  gmw->w_fs = w_filter_settings_default();
  g_signal_connect(gmw->w_fs->btn_generate,
                   "clicked",
                   G_CALLBACK(gmw_btn_filter_sensor_data_clicked),
                   gmw);

  g_signal_connect(gmw->w_fs->btn_clear,
                   "clicked",
                   G_CALLBACK(gmw_btn_clear_filtered_data_clicked),
                   gmw);

  return gmw->w_fs->frame;
}
//////////////////////////////////////////////////////////////

GtkWidget *create_load_track_frame(generator_main_window *gmw)
{
  GtkWidget *btn_clear = gtk_button_new_with_label("Clear ALL");
  GtkWidget *btn_load = gtk_button_new_with_label("Load");
  GtkWidget *btn_save = gtk_button_new_with_label("Save");
  void (*btn_clicked_handlers[])(GtkWidget *, gpointer) = {
      gmw_btn_clear_all_points_clicked,
      gmw_btn_load_track_clicked,
      gmw_btn_save_tracks_clicked};
  GtkWidget *btns[] = {btn_clear, btn_load, btn_save, nullptr};

  GtkWidget *grid = gtk_grid_new();
  gtk_grid_set_row_spacing(GTK_GRID(grid), 5);
  gtk_grid_set_column_spacing(GTK_GRID(grid), 5);
  gtk_grid_set_row_homogeneous(GTK_GRID(grid), true);
  gtk_grid_set_column_homogeneous(GTK_GRID(grid), true);

  for (int r = 0; btns[r]; ++r) {
    gtk_grid_attach(GTK_GRID(grid), btns[r], 0, r, 1, 1);
    g_signal_connect(btns[r],
                     "clicked",
                     G_CALLBACK(btn_clicked_handlers[r]),
                     gmw);
  }

  GtkWidget *frame = gtk_frame_new("Current track");
  gtk_frame_set_child(GTK_FRAME(frame), grid);
  gtk_frame_set_label_align(GTK_FRAME(frame), 0.5);
  return frame;
}
//////////////////////////////////////////////////////////////

static void gmw_layer_set_visible(generator_main_window *gmw,
                                  marker_type mt,
                                  bool visible)
{
  gtk_widget_set_visible(GTK_WIDGET(gmw->layers[mt].path_layer), visible);
  gtk_widget_set_visible(GTK_WIDGET(gmw->layers[mt].marker_layer), visible);
}
//////////////////////////////////////////////////////////////

void gmw_chk_layer_set_toggled(GtkCheckButton *self, gpointer user_data)
{
  generator_main_window *gmw =
      reinterpret_cast<generator_main_window *>(user_data);
  bool active = gtk_check_button_get_active(self);
  gmw_layer_set_visible(gmw, MT_GPS_SET, active);
}
//////////////////////////////////////////////////////////////

void gmw_chk_layer_generated_toggled(GtkCheckButton *self, gpointer user_data)
{
  generator_main_window *gmw =
      reinterpret_cast<generator_main_window *>(user_data);
  bool active = gtk_check_button_get_active(self);
  gmw_layer_set_visible(gmw, MT_GPS_GENERATED, active);
}
//////////////////////////////////////////////////////////////

void gmw_chk_layer_filtered_gps_toggled(GtkCheckButton *self,
                                        gpointer user_data)
{
  generator_main_window *gmw =
      reinterpret_cast<generator_main_window *>(user_data);
  bool active = gtk_check_button_get_active(self);
  gmw_layer_set_visible(gmw, MT_GPS_FILTERED_UPDATED, active);
}
//////////////////////////////////////////////////////////////

void gmw_chk_layer_filtered_acc_toggled(GtkCheckButton *self,
                                        gpointer user_data)
{
  generator_main_window *gmw =
      reinterpret_cast<generator_main_window *>(user_data);
  bool active = gtk_check_button_get_active(self);
  gmw_layer_set_visible(gmw, MT_GPS_FILTERED_PREDICTED, active);
}
//////////////////////////////////////////////////////////////

GtkWidget *create_layers_visibility_and_distance_frame(
    generator_main_window *gmw)
{
  // chks
  GtkWidget *chk_set = gtk_check_button_new_with_label("Set manually");
  GtkWidget *chk_generated = gtk_check_button_new_with_label("Generated");
  GtkWidget *chk_filtered_gps = gtk_check_button_new_with_label("Filtered GPS");
  GtkWidget *chk_filtered_acc = gtk_check_button_new_with_label("Filtered ACC");

  // lbls
  GtkWidget *lbl_set = gtk_label_new("0.0");
  const char *lbl_set_css[] = {label_css_classes[MT_GPS_SET], NULL};
  gtk_widget_set_css_classes(lbl_set, lbl_set_css);

  GtkWidget *lbl_generated = gtk_label_new("0.0");
  const char *lbl_generated_css[] = {label_css_classes[MT_GPS_GENERATED], NULL};
  gtk_widget_set_css_classes(lbl_generated, lbl_generated_css);

  GtkWidget *lbl_filtered_gps = gtk_label_new("0.0");
  const char *lbl_filtered_gps_css[] = {
      label_css_classes[MT_GPS_FILTERED_UPDATED],
      NULL};
  gtk_widget_set_css_classes(lbl_filtered_gps, lbl_filtered_gps_css);

  GtkWidget *lbl_filtered_acc = gtk_label_new("0.0");
  const char *lbl_filtered_acc_css[] = {
      label_css_classes[MT_GPS_FILTERED_PREDICTED],
      NULL};
  gtk_widget_set_css_classes(lbl_filtered_acc, lbl_filtered_acc_css);

  gmw->layers[MT_GPS_SET].lbl = lbl_set;
  gmw->layers[MT_GPS_GENERATED].lbl = lbl_generated;
  gmw->layers[MT_GPS_FILTERED_PREDICTED].lbl = lbl_filtered_acc;
  gmw->layers[MT_GPS_FILTERED_UPDATED].lbl = lbl_filtered_gps;

  GtkWidget *grid = gtk_grid_new();
  gtk_grid_set_row_spacing(GTK_GRID(grid), 5);
  gtk_grid_set_row_homogeneous(GTK_GRID(grid), true);
  gtk_grid_set_column_homogeneous(GTK_GRID(grid), true);

  GtkWidget *chks[] = {chk_set,
                       chk_generated,
                       chk_filtered_gps,
                       chk_filtered_acc,
                       nullptr};
  GtkWidget *lbls[] = {lbl_set,
                       lbl_generated,
                       lbl_filtered_gps,
                       lbl_filtered_acc,
                       nullptr};

  void (*chk_toggled_cbs[])(GtkCheckButton *, gpointer) = {
      gmw_chk_layer_set_toggled,
      gmw_chk_layer_generated_toggled,
      gmw_chk_layer_filtered_gps_toggled,
      gmw_chk_layer_filtered_acc_toggled,
  };

  for (int r = 0; chks[r] && lbls[r]; ++r) {
    gtk_check_button_set_active(GTK_CHECK_BUTTON(chks[r]), true);
    gtk_grid_attach(GTK_GRID(grid), chks[r], 0, r, 1, 1);
    gtk_grid_attach(GTK_GRID(grid), lbls[r], 1, r, 1, 1);
    g_signal_connect(GTK_CHECK_BUTTON(chks[r]),
                     "toggled",
                     G_CALLBACK(chk_toggled_cbs[r]),
                     gmw);
  }

  GtkWidget *frame = gtk_frame_new("Visible layers");
  gtk_frame_set_child(GTK_FRAME(frame), grid);
  gtk_frame_set_label_align(GTK_FRAME(frame), 0.5);
  return frame;
}
//////////////////////////////////////////////////////////////

GtkWidget *create_generator_settings_frame(generator_main_window *gmw)
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

void gmw_bind_to_app(GtkApplication *app, generator_main_window *gmw)
{
  // register css styles (for markers)
  GtkCssProvider *css_provider = gtk_css_provider_new();
  gtk_css_provider_load_from_string(css_provider, gmw_css_data);
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
    gmw->layers.push_back(gmw_layer(marker_layer, path_layer));
  }

  gmw_set_center(gmw, 36.5519514, 31.9801362);

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
  GtkWidget *frame_layers_visibility =
      create_layers_visibility_and_distance_frame(gmw);

  // main grid
  GtkWidget *grid_main = gtk_grid_new();
  gtk_grid_set_column_spacing(GTK_GRID(grid_main), 5);
  gtk_grid_set_row_spacing(GTK_GRID(grid_main), 5);

  gtk_grid_set_row_homogeneous(GTK_GRID(grid_main), true);
  gtk_grid_set_column_homogeneous(GTK_GRID(grid_main), true);

  gtk_grid_attach(GTK_GRID(grid_main), GTK_WIDGET(gmw->simple_map), 0, 0, 3, 5);
  gtk_grid_attach(GTK_GRID(grid_main), frame_generator, 3, 0, 1, 2);
  gtk_grid_attach(GTK_GRID(grid_main), frame_filter, 3, 2, 1, 1);
  gtk_grid_attach(GTK_GRID(grid_main), frame_layers_visibility, 3, 3, 1, 1);
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

  gmw->layers[MT_GPS_SET].add_record(rec);
  std::string sd = std::to_string(gmw->layers[MT_GPS_SET].distance);
  gtk_label_set_text(GTK_LABEL(gmw->layers[MT_GPS_SET].lbl), sd.c_str());

  gmw_add_marker(gmw, MT_GPS_SET, lat, lng);
}
//////////////////////////////////////////////////////////////

// case SD_ACC_ENU_SET:
static void load_acc_enu_set(generator_main_window *gmw, const sd_record &rec)
{
  gmw->layers[MT_GPS_SET].add_record(rec);
}

// case SD_GPS_SET:
static void load_gps_set(generator_main_window *gmw, const sd_record &rec)
{
  std::string sd;
  gmw->layers[MT_GPS_SET].add_record(rec);
  sd = std::to_string(gmw->layers[MT_GPS_SET].distance);
  gtk_label_set_text(GTK_LABEL(gmw->layers[MT_GPS_SET].lbl), sd.c_str());
  gmw_add_marker(gmw,
                 MT_GPS_SET,
                 rec.data.gps.location.latitude,
                 rec.data.gps.location.longitude);
}

// case SD_ACC_ENU_GENERATED
static void load_acc_enu_generated(generator_main_window *gmw,
                                   const sd_record &rec)
{
  gmw->layers[MT_GPS_GENERATED].add_record(rec);
}

// case SD_GPS_GENERATED
static void load_gps_generated(generator_main_window *gmw, const sd_record &rec)
{
  std::string sd;
  gmw->layers[MT_GPS_GENERATED].add_record(rec);
  sd = std::to_string(gmw->layers[MT_GPS_GENERATED].distance);
  gtk_label_set_text(GTK_LABEL(gmw->layers[MT_GPS_GENERATED].lbl), sd.c_str());
  gmw_add_marker(gmw,
                 MT_GPS_GENERATED,
                 rec.data.gps.location.latitude,
                 rec.data.gps.location.longitude);
  gmw_set_center(gmw,
                 rec.data.gps.location.latitude,
                 rec.data.gps.location.longitude);
}

// case SD_GPS_FILTERED
static void load_gps_filtered(generator_main_window *gmw, const sd_record &rec)
{
  gmw->layers[MT_GPS_FILTERED_UPDATED].add_record(rec);
  std::string sd =
      std::to_string(gmw->layers[MT_GPS_FILTERED_UPDATED].distance);
  gtk_label_set_text(GTK_LABEL(gmw->layers[MT_GPS_FILTERED_UPDATED].lbl),
                     sd.c_str());
}

// case SD_RAW_ENU_ACC
static void load_raw_enu_acc(generator_main_window *gmw, const sd_record &rec)
{
  gmw->layers[MT_GPS_GENERATED].add_record(rec);
}

static void load_unknown(generator_main_window *gmw, const sd_record &rec)
{
  // do nothing actually
  UNUSED(gmw);
  UNUSED(rec);
}

void dlg_load_track_cb(GObject *source_object, GAsyncResult *res, gpointer data)
{
  typedef void (*pf_load_track_handler)(generator_main_window *,
                                        const sd_record &);
  static std::vector<std::pair<sensor_data_record_type, pf_load_track_handler>>
      handlers = {
          std::make_pair(SD_ACC_ENU_SET, load_acc_enu_set),
          std::make_pair(SD_GPS_SET, load_gps_set),
          std::make_pair(SD_ACC_ENU_GENERATED, load_acc_enu_generated),
          std::make_pair(SD_GPS_GENERATED, load_gps_generated),
          std::make_pair(SD_GPS_FILTERED, load_gps_filtered),
          std::make_pair(SD_RAW_ENU_ACC, load_raw_enu_acc),
          std::make_pair(SD_UNKNOWN, load_unknown),
      };

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
  sd_record rec;
  while (std::getline(fs_in, line)) {
    sdr_deserialize_error derr = sdr_deserialize_str(line, rec);
    if (derr != SDRDE_SUCCESS) {
      std::cerr << derr << "->  failed to process line: " << line << std::endl;
      continue;
    }

    auto h_it = std::find_if(
        handlers.begin(),
        handlers.end(),
        [&rec](const std::pair<sensor_data_record_type, pf_load_track_handler>
                   &p) { return p.first == rec.hdr.type; });

    if (h_it == handlers.end()) {
      std::cerr << "unknown header type: " << rec.hdr.type << "\n";
      continue;
    }

    h_it->second(gmw, rec);
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
    shumate_path_layer_remove_all(gmw->layers[i].path_layer);
    shumate_marker_layer_remove_all(gmw->layers[i].marker_layer);
    gmw->layers[i].clear();
    gtk_label_set_text(GTK_LABEL(gmw->layers[i].lbl), "0.0");
  }
}
//////////////////////////////////////////////////////////////

void dlg_save_tracks_cb(GObject *source_object,
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

  marker_type export_layers[] = {MT_GPS_SET,
                                 MT_GPS_GENERATED,
                                 MT_GPS_FILTERED_UPDATED,
                                 MT_COUNT};
  for (int i = 0; export_layers[i] != MT_COUNT; ++i) {
    for (auto rec : gmw->layers[export_layers[i]].lst_sd_records) {
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
  if (gmw->layers[MT_GPS_SET].lst_sd_records.empty()) {
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
  const std::vector<sd_record> &src = gmw->layers[MT_GPS_SET].lst_sd_records;
  gmw_layer &dst = gmw->layers[MT_GPS_GENERATED];

  if (src.empty()) {
    return;  // do nothing
  }
  generator_options go = gmw->w_gs->opts;

  dst.add_record(
      sd_record(sd_record_hdr(SD_GPS_GENERATED, 0.), src.front().data.gps));
  std::string sd = std::to_string(dst.distance);
  gtk_label_set_text(GTK_LABEL(dst.lbl), sd.c_str());

  gmw_add_marker(gmw,
                 MT_GPS_GENERATED,
                 src.front().data.gps.location.latitude,
                 src.front().data.gps.location.longitude);

  double ts = 0.;  // global ts
  for (size_t i = 1; i < src.size(); ++i) {
    const sd_record &prev_rec =
        dst.lst_sd_records.back();  // WARNING! but should work fine
    const sd_record &curr_rec = src[i];

    const double gps_mp = go.gps_measurement_period;
    const double acc_mp = go.acc_measurement_period;
    // ats = accelerometer ts
    for (double ats = 0.; ats < gps_mp; ats += acc_mp) {
      enu_accelerometer acc =
          sd_abs_acc_between_two_geopoints(prev_rec.data.gps,
                                           curr_rec.data.gps,
                                           go.acceleration_time,
                                           go.gps_measurement_period,
                                           ats);

      acc = sd_noised_acc(acc, go.acc_noise);
      dst.add_record(
          sd_record(sd_record_hdr(SD_ACC_ENU_GENERATED, ts + ats), acc));
    }  // finished generating accelerometer data

    // need to generate ideal coordinate with speed and then noise it
    gps_coordinate cc = prev_rec.data.gps;
    enu_accelerometer acc =
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
    // now we know coordinate and it's speed and can add some location noise
    // there
    cc.location = sd_noised_geopoint(cc.location, go.gps_location_noise);
    ts += go.gps_measurement_period;
    dst.add_record(sd_record(sd_record_hdr(SD_GPS_GENERATED, ts), cc));
    std::string sd = std::to_string(dst.distance);
    gtk_label_set_text(GTK_LABEL(dst.lbl), sd.c_str());
    gmw_add_marker(gmw,
                   MT_GPS_GENERATED,
                   cc.location.latitude,
                   cc.location.longitude);
  }  // for (size_t i = 1; i < src.size(); ++i}
}
//////////////////////////////////////////////////////////////

void gmw_btn_clear_generated_data_clicked(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  int li = MT_GPS_GENERATED;
  shumate_path_layer_remove_all(gmw->layers[li].path_layer);
  shumate_marker_layer_remove_all(gmw->layers[li].marker_layer);
  gmw->layers[li].clear();
  gtk_label_set_text(GTK_LABEL(gmw->layers[li].lbl), "0.0");
}
//////////////////////////////////////////////////////////////

static void filter_acc_enu_generated(generator_main_window *gmw,
                                     MLM &mlm,
                                     const sd_record &rec)
{
  bool pad = mlm.process_acc_data(rec.data.acc, rec.hdr.timestamp);
  if (pad) {
    gps_coordinate pc = mlm.predicted_coordinate();
    // gmw_add_marker(gmw,
    //                MT_GPS_FILTERED_PREDICTED,
    //                pc.location.latitude,
    //                pc.location.longitude);
  }
}

static void filter_gps_generated(generator_main_window *gmw,
                                 MLM &mlm,
                                 const sd_record &rec)
{
  gmw_layer &dst = gmw->layers[MT_GPS_FILTERED_UPDATED];
  mlm.process_gps_data(rec.data.gps, rec.hdr.timestamp);
  gps_coordinate pc = mlm.predicted_coordinate();
  dst.add_record(
      sd_record(sd_record_hdr(SD_GPS_FILTERED, rec.hdr.timestamp), pc));
  std::string sd = std::to_string(dst.distance);
  gtk_label_set_text(GTK_LABEL(dst.lbl), sd.c_str());
  gmw_add_marker(gmw,
                 MT_GPS_FILTERED_UPDATED,
                 pc.location.latitude,
                 pc.location.longitude);
}

static void filter_raw_enu_acc(generator_main_window *gmw,
                               MLM &mlm,
                               const sd_record &rec)
{
  enu_accelerometer acc;
  // quaternion rotate vector
  double ax, ay, az, qw, qx, qy, qz;
  ax = rec.data.raw_enu_acc.acc.x;
  ay = rec.data.raw_enu_acc.acc.y;
  az = rec.data.raw_enu_acc.acc.z;
  qw = rec.data.raw_enu_acc.rq.w;
  qx = rec.data.raw_enu_acc.rq.x;
  qy = rec.data.raw_enu_acc.rq.y;
  qz = rec.data.raw_enu_acc.rq.z;

  // t = 2 * cross(q.xyz, v)
  double t0 = 2.0 * (qy * az - qz * ay);
  double t1 = 2.0 * (qz * ax - qx * az);
  double t2 = 2.0 * (qx * ay - qy * ax);

  acc.x = ax + qw * t0 + (qy * t2 - qz * t1);
  acc.y = ay + qw * t1 + (qz * t0 - qx * t2);
  acc.z = az * qw * t2 + (qx * t1 - qy * t0);

  // static LowPassFilter<double, 3> lp(5);
  // double src[3] = {acc.x, acc.y, acc.z};
  // double *filtered = lp.filter(src, rec.hdr.timestamp);
  //
  // acc.x = filtered[0];
  // acc.y = filtered[1];
  // acc.z = filtered[2];

  bool pad = mlm.process_acc_data(acc, rec.hdr.timestamp);
  if (!pad)
    return;

  gps_coordinate pc = mlm.predicted_coordinate();
  // gmw_add_marker(gmw,
  //                MT_GPS_FILTERED_PREDICTED,
  //                pc.location.latitude,
  //                pc.location.longitude);
}

static void filter_unknown(generator_main_window *gmw,
                           MLM &mlm,
                           const sd_record &rec)
{
  UNUSED(gmw);
  UNUSED(mlm);
  UNUSED(rec);  // do nothing
}

void gmw_btn_filter_sensor_data_clicked(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  const std::vector<sd_record> &src =
      gmw->layers[MT_GPS_GENERATED].lst_sd_records;

  if (src.empty()) {
    return;  // do nothing
  }

  // TODO move generated data into dst.
  MLM mlm(gmw->w_fs->opts.acc_sigma_2,
          gmw->w_fs->opts.loc_sigma_2,
          gmw->w_fs->opts.vel_sigma_2);

  typedef void (
      *pf_filter_handler)(generator_main_window *, MLM &, const sd_record &);
  static std::vector<std::pair<sensor_data_record_type, pf_filter_handler>>
      handlers = {
          std::make_pair(SD_ACC_ENU_GENERATED, filter_acc_enu_generated),
          std::make_pair(SD_GPS_GENERATED, filter_gps_generated),
          std::make_pair(SD_RAW_ENU_ACC, filter_raw_enu_acc),
          std::make_pair(SD_UNKNOWN, filter_unknown),
      };

  for (const sd_record &rec : src) {
    auto h_it = std::find_if(
        handlers.begin(),
        handlers.end(),
        [&rec](const std::pair<sensor_data_record_type, pf_filter_handler> &p) {
          return p.first == rec.hdr.type;
        });

    if (h_it == handlers.end()) {
      std::cerr << "unknown header type: " << rec.hdr.type << "\n";
      continue;
    }

    h_it->second(gmw, mlm, rec);
  }  // for (sd_record &rec : src)
}
//////////////////////////////////////////////////////////////

void gmw_btn_clear_filtered_data_clicked(GtkWidget *btn, gpointer ud)
{
  UNUSED(btn);
  generator_main_window *gmw = reinterpret_cast<generator_main_window *>(ud);
  marker_type lis[] = {MT_GPS_FILTERED_PREDICTED, MT_GPS_FILTERED_UPDATED};
  for (int i = 0; i < 2; ++i) {
    int li = lis[i];
    shumate_path_layer_remove_all(gmw->layers[li].path_layer);
    shumate_marker_layer_remove_all(gmw->layers[li].marker_layer);
    gmw->layers[li].clear();
    gtk_label_set_text(GTK_LABEL(gmw->layers[li].lbl), "0.0");
  }
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

  shumate_marker_layer_add_marker(gmw->layers[mt].marker_layer, marker);
  shumate_path_layer_add_node(gmw->layers[mt].path_layer,
                              SHUMATE_LOCATION(marker));
}
/////////////////////////////////////////////////////////////
