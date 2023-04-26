#ifndef _MAIN_WINDOW_H
#define _MAIN_WINDOW_H

#include <gtk/gtk.h>
#include <shumate/shumate.h>

enum marker_color { MC_GREEN = 0, MC_RED, MC_BLUE };
//////////////////////////////////////////////////////////////

struct generator_main_window {
  GtkWidget *window;
  ShumateSimpleMap *simple_map;
  ShumateMapSourceRegistry *map_source_registry;
  ShumateMarkerLayer *map_marker_layer;
  ShumatePathLayer *map_path_layer_green;
  ShumatePathLayer *map_path_layer_red;
  ShumatePathLayer *map_path_layer_blue;

  generator_main_window();
  ~generator_main_window();
};
//////////////////////////////////////////////////////////////

void generator_main_window_bind_to_app(GtkApplication *app,
                                       generator_main_window *gmw);

#endif
