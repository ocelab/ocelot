#include <stdio.h>
#include <math.h>
#define OCELOT_TESTFUNCTION gimp_rgb_to_hsv_int
#define MAX(a, b) (a > b ? a : b)
#define MIN(a, b) (a < b ? a : b)
#define ROUND(a) (int)a
#include "ocelot.h"
typedef int gint;
typedef double gdouble;
typedef struct 
{
    gint* red;
    gint* green;
    gint* blue;
} GimpColor;
void OCELOT_TESTFUNCTION(GimpColor);

void gimp_rgb_to_hsv_int(GimpColor color)
{
    gdouble r, g, b;
    gdouble h, s, v;
    gint min;
    gdouble delta;
    r = *color.red;
    g = *color.green;
    b = *color.blue;
    if (r > g)
    {
      v = MAX (r, b);
      min = MIN (g, b);
    }
  else
    {
      v = MAX (g, b);
      min = MIN (r, b);
    }
    delta = v - min;
    if (_f_ocelot_trace(v == 0.0, _f_ocelot_eq_numeric(v, 0.0), _f_ocelot_neq_numeric(v, 0.0)))
        s = 0.0;
    else
        s = delta / v;

    if (_f_ocelot_trace(s == 0.0, _f_ocelot_eq_numeric(s, 0.0), _f_ocelot_neq_numeric(s, 0.0))){
        h = 0.0;
    }else{
        if (_f_ocelot_trace(r == v, _f_ocelot_eq_numeric(r, v), _f_ocelot_neq_numeric(r, v)))
            h = 60.0 * (g - b) / delta;
        else
            if (_f_ocelot_trace(g == v, _f_ocelot_eq_numeric(g, v), _f_ocelot_neq_numeric(g, v)))
                h = 120 + 60.0 * (b - r) / delta;
            else
                h = 240 + 60.0 * (r - g) / delta;

        if (_f_ocelot_trace(h < 0.0, _f_ocelot_gt_numeric(0.0, h), _f_ocelot_ge_numeric(h, 0.0)))
            h += 360.0;

        if (_f_ocelot_trace(h > 360.0, _f_ocelot_gt_numeric(h, 360.0), _f_ocelot_ge_numeric(360.0, h)))
            h -= 360.0;
    }
    *color.red   = ROUND (h);
    *color.green = ROUND (s * 255.0);
    *color.blue  = ROUND (v);
    if (_f_ocelot_trace(*color.red == 360, _f_ocelot_eq_numeric(*color.red, 360), _f_ocelot_neq_numeric(*color.red, 360)))
        *color.red = 0;
}
