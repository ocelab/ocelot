#include "ocelot.h"
//#include <glib-object.h>

#ifndef _Included_MAINTEST
#define _Included_MAINTEST
#ifdef __cplusplus
extern "C" {
#endif

#define gint int
#define gdouble double

#define OCELOT_MAX(a, b) (a > b ? a : b)
#define OCELOT_MIN(a, b) (a < b ? a : b)
#define OCELOT_ROUND(h) (int)h

#define OCELOT_TESTFUNCTION _f_ocelot_testfunction

void OCELOT_TESTFUNCTION (gint *red, gint *green, gint *blue);

#ifdef __cplusplus
}
#endif
#endif
