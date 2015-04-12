#include "main.h"

#define MAX(a, b) (a > b ? a : b)
#define MIN(a, b) (a < b ? a : b)
#define ROUND(a) (int)a

void
gimp_rgb_to_hsv_int (GimpColor color)
{
  gdouble  r, g, b;
  gdouble  h, s, v;
  gint     min;
  gdouble  delta;

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

  if (v == 0.0)
    s = 0.0;
  else
    s = delta / v;

  if (s == 0.0)
    {
      h = 0.0;
    }
  else
    {
      if (r == v)
        h = 60.0 * (g - b) / delta;
      else if (g == v)
        h = 120 + 60.0 * (b - r) / delta;
      else
        h = 240 + 60.0 * (r - g) / delta;

      if (h < 0.0)
        h += 360.0;

      if (h > 360.0)
        h -= 360.0;
    }

  *color.red   = ROUND (h);
  *color.green = ROUND (s * 255.0);
  *color.blue  = ROUND (v);

  /* avoid the ambiguity of returning different values for the same color */
  if (*color.red == 360)
    *color.red = 0;
}
