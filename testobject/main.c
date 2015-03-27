//#include <glib-object.h>
#include "main.h"

void OCELOT_TESTFUNCTION (gint *red,
                     gint *green,
                     gint *blue)
{
  gint    r, g, b;
  gdouble h, s, l;
  gint    min, max;
  gint    delta;

  r = *red;
  g = *green;
  b = *blue;

  if (r > g)
    {
      max = OCELOT_MAX (r, b);
      min = OCELOT_MIN (g, b);
    }
  else
    {
      max = OCELOT_MAX (g, b);
      min = OCELOT_MIN (r, b);
    }

  l = (max + min) / 2.0;

  if (max == min)
    {
      s = 0.0;
      h = 0.0;
    }
  else
    {
      delta = (max - min);

      if (l < 128)
        s = 255 * (gdouble) delta / (gdouble) (max + min);
      else
        s = 255 * (gdouble) delta / (gdouble) (511 - max - min);

      if (r == max)
        h = (g - b) / (gdouble) delta;
      else if (g == max)
        h = 2 + (b - r) / (gdouble) delta;
      else
        h = 4 + (r - g) / (gdouble) delta;

      h = h * 42.5;

      if (h < 0)
        h += 255;
      else if (h > 255)
        h -= 255;
    }

  *red   = OCELOT_ROUND (h);
  *green = OCELOT_ROUND (s);
  *blue  = OCELOT_ROUND (l);
}
