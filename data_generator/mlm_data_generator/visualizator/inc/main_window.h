#ifndef _MAIN_WINDOW_H
#define _MAIN_WINDOW_H

#include <gtk/gtk.h>
#include <shumate/shumate.h>

enum marker_color { MC_GREEN = 0, MC_RED, MC_BLUE, MC_COUNT };
//////////////////////////////////////////////////////////////

struct generator_main_window ;
generator_main_window * gmw_create(const char *binary_path);
void gmw_free(generator_main_window *gmw);

void gmw_bind_to_app(GtkApplication *app, generator_main_window *gmw);
void gmw_show(generator_main_window *gmw);

void gmw_add_marker(generator_main_window *gmw, marker_color mc,
                    double latitude, double longitude);
#endif
