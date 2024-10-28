#ifndef _MAIN_WINDOW_H
#define _MAIN_WINDOW_H

#include <gtk/gtk.h>


struct generator_main_window;
generator_main_window *gmw_create();
void gmw_free(generator_main_window *gmw);

void gmw_bind_to_app(GtkApplication *app, generator_main_window *gmw);
void gmw_show(generator_main_window *gmw);

#endif
