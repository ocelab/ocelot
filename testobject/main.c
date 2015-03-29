#define gint int
#define gdouble double
#define ROUND(x) (int)x
void
gimp_hsv_to_rgb_int (gint *hue,
                     gint *saturation,
                     gint *value)
{
  gdouble h, s, v, h_temp;
  gdouble f, p, q, t;
  gint i;

  if (*saturation == 0)
    {
      *hue        = *value;
      *saturation = *value;
      *value      = *value;
    }
  else
    {
      h = *hue;
      s = *saturation / 255.0;
      v = *value      / 255.0;

      if (h == 360)
        h_temp = 0;
      else
        h_temp = h;

      h_temp = h_temp / 60.0;
      i = floor (h_temp);
      f = h_temp - i;
      p = v * (1.0 - s);
      q = v * (1.0 - (s * f));
      t = v * (1.0 - (s * (1.0 - f)));

      switch (i)
        {
        case 0:
          *hue        = ROUND (v * 255.0);
          *saturation = ROUND (t * 255.0);
          *value      = ROUND (p * 255.0);
          break;

        case 1:
          *hue        = ROUND (q * 255.0);
          *saturation = ROUND (v * 255.0);
          *value      = ROUND (p * 255.0);
          break;

        case 2:
          *hue        = ROUND (p * 255.0);
          *saturation = ROUND (v * 255.0);
          *value      = ROUND (t * 255.0);
          break;

        case 3:
          *hue        = ROUND (p * 255.0);
          *saturation = ROUND (q * 255.0);
          *value      = ROUND (v * 255.0);
          break;

        case 4:
          *hue        = ROUND (t * 255.0);
          *saturation = ROUND (p * 255.0);
          *value      = ROUND (v * 255.0);
          break;

        case 5:
          *hue        = ROUND (v * 255.0);
          *saturation = ROUND (p * 255.0);
          *value      = ROUND (q * 255.0);
          break;

        default:
        	break;
        }
    }
}
