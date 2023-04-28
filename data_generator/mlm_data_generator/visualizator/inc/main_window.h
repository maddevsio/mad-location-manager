#ifndef _MAIN_WINDOW_H
#define _MAIN_WINDOW_H

#include <gtk/gtk.h>
#include <shumate/shumate.h>

enum marker_color { MC_GREEN = 0, MC_RED, MC_BLUE, MC_COUNT };
//////////////////////////////////////////////////////////////

struct generator_main_window {
  GtkWidget *window;
  ShumateSimpleMap *simple_map;
  ShumateMapSourceRegistry *map_source_registry;
  ShumateMarkerLayer *map_marker_layer;
  ShumatePathLayer *map_path_layers[MC_COUNT];  // see marker color

  generator_main_window();
  ~generator_main_window();
};
//////////////////////////////////////////////////////////////

void gmw_bind_to_app(GtkApplication *app, generator_main_window *gmw);

void gmw_add_marker(generator_main_window *gmw, marker_color mc,
                    double latitude, double longitude);

#endif
