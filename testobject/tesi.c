#include <math.h>
#include <string.h>
#include "config.h"

#include <stdlib.h>

#include "libgimpbase/gimpbase.h"
#include "libgimpmath/gimpmath.h"
#include "libgimpcolor/gimpcolor.h"

#include "core-types.h"

#include "base/pixel-processor.h"
#include "base/pixel-region.h"
#include "base/tile.h"
#include "base/tile-manager.h"

#include "paint-funcs/paint-funcs.h"

#include "gimp.h"
#include "gimpchannel.h"
#include "gimpcontext.h"
#include "gimpdrawable-blend.h"
#include "gimpgradient.h"
#include "gimpimage.h"
#include "gimpprogress.h"

#include "gimp-intl.h"

#include "gimps.h"

#define SQR(x) ((x) * (x))

#define M_PI 3.14159265358979323846 /* pi */

typedef int boolean;

#define TRUE 1
#define true TRUE
#define FALSE 0
#define false FALSE

#define RAD_TO_DEG	(180.0 / M_PI)
typedef struct FCOMPLEX {
	float r, i;
} fcomplex;
enum Fisher_mode {
	LESS = 1, GREATER, TWO_SIDED
};

typedef boolean YESorNO;
#define YES TRUE
#define NO FALSE

#define ISBN_DIGIT_VALUE(c)	((((c) == 'X') || ((c) == 'x')) ? 10 : \
					((c) - '0'))
/* correct only if digits are valid; */
/* the code below ensures that */


double hypot(double a, double b) {
	return sqrt(pow(a,2) + pow(b,2));
}

bool clip_to_circle(int *x1, int *y1, int *x2, int *y2, int cx, int cy, int rad) {
	double perplen, a, b, c;
	double tx, ty, dt;
	double dtheta;
	double theta1, theta2, tt, alpha, beta, gamma;
	bool flip = FALSE;
	int i;

	/* Get the angles between the origin and the endpoints. */
	if ((*x1 - cx) || (*y1 - cy))
		theta1 = atan2((double) *y1 - cy, (double) *x1 - cx);
	else
		theta1 = M_PI;
	if ((*x2 - cx) || (*y2 - cy))
		theta2 = atan2((double) *y2 - cy, (double) *x2 - cx);
	else
		theta2 = M_PI;

	if (theta1 < 0.0)
		theta1 = 2 * M_PI + theta1;
	if (theta2 < 0.0)
		theta2 = 2 * M_PI + theta2;

	dtheta = theta2 - theta1;
	if (dtheta > M_PI)
		dtheta = dtheta - 2 * M_PI;
	else if (dtheta < -M_PI)
		dtheta = 2 * M_PI - dtheta;

	/* Make sure that p1 is the first point */
	if (dtheta < 0) {
		tt = theta1;
		theta1 = theta2;
		theta2 = tt;
		i = *x1;
		*x1 = *x2;
		*x2 = i;
		i = *y1;
		*y1 = *y2;
		*y2 = i;
		flip = TRUE;
		dtheta = -dtheta;
	}

	/* Figure out the distances between the points */
	a = hypot(*x1 - cx, *y1 - cy);
	b = hypot(*x2 - cx, *y2 - cy);
	c = hypot(*x1 - *x2, *y1 - *y2);

	/* We have three cases now -- either the midpoint of the line is
	 * closest to the origon, or point 1 or point 2 is.  Actually the
	 * midpoint won't in general be the closest, but if a point besides
	 * one of the endpoints is closest, the midpoint will be closer than
	 * both endpoints.
	 */
	tx = (*x1 + *x2) / 2;
	ty = (*y1 + *y2) / 2;
	dt = hypot(tx - cx, ty - cy);
	if ((dt < a) && (dt < b)) {
		/* This is wierd -- round-off errors I guess. */
		tt = (a * a + c * c - b * b) / (2 * a * c);
		if (tt > 1.0)
			tt = 1.0;
		else if (tt < -1.0)
			tt = -1.0;
		alpha = acos(tt);
		perplen = a * sin(alpha);
	} else if (a < b) {
		perplen = a;
	} else {
		perplen = b;
	}

	/* Now we should see if the line is outside of the circle */
	if (perplen >= rad)
		return (TRUE);

	/* It's at least partially inside */
	if (a > rad) {
		tt = (a * a + c * c - b * b) / (2 * a * c);
		if (tt > 1.0)
			tt = 1.0;
		else if (tt < -1.0)
			tt = -1.0;
		alpha = acos(tt);
		gamma = asin(sin(alpha) * a / rad);
		if (gamma < M_PI / 2)
			gamma = M_PI - gamma;
		beta = M_PI - alpha - gamma;
		*x1 = (int) (cx + rad * cos(theta1 + beta));
		*y1 = (int) (cy + rad * sin(theta1 + beta));
	}

	if (b > rad) {
		tt = (c * c + b * b - a * a) / (2 * b * c);
		if (tt > 1.0)
			tt = 1.0;
		else if (tt < -1.0)
			tt = -1.0;
		alpha = acos(tt);
		gamma = asin(sin(alpha) * b / rad);
		if (gamma < M_PI / 2)
			gamma = M_PI - gamma;
		beta = M_PI - alpha - gamma;
		*x2 = (int) (cx + rad * cos(theta2 - beta));
		*y2 = (int) (cy + rad * sin(theta2 - beta));
	}

	if (flip) {
		i = *x1;
		*x1 = *x2;
		*x2 = i;
		i = *y1;
		*y1 = *y2;
		*y2 = i;
	}

	return (FALSE);
}

/* This routine draws an arc and clips it to a circle.  It's hard to figure
 * out how it works without looking at the piece of scratch paaper I have
 * in front of me, so let's hope it doesn't break...
 * Converted to all doubles for CRAYs
 */

void Arc(int a, int b, int c, double d, double e) {

}

double cliparc(cx, cy, rad, start, end, iclipx, iclipy, icliprad, flag)
	double cx, cy, rad;int iclipx, iclipy, icliprad, flag;double start, end; {
	double clipx, clipy, cliprad;
	double x, y, tx, ty, dist;
	double alpha, theta, phi, a1, a2, d, l;
	double sclip, eclip;
	bool in;

	clipx = (double) iclipx;
	clipy = (double) iclipy;
	cliprad = (double) icliprad;
	x = cx - clipx;
	y = cy - clipy;
	dist = sqrt((double) (x * x + y * y));

	if (!rad || !cliprad)
		return (-1);
	if (dist + rad < cliprad) {
		/* The arc is entirely in the boundary. */
		Arc((int) cx, (int) cy, (int) rad, start, end);
		return (flag ? start : end);
	} else if ((dist - rad >= cliprad) || (rad - dist >= cliprad)) {
		/* The arc is outside of the boundary. */
		return (-1);
	}
	/* Now let's figure out the angles at which the arc crosses the
	 * circle. We know dist != 0.
	 */
	if (x)
		phi = atan2((double) y, (double) x);
	else if (y > 0)
		phi = M_PI * 1.5;
	else
		phi = M_PI / 2;
	if (cx > clipx)
		theta = M_PI + phi;
	else
		theta = phi;

	alpha = (double) (dist * dist + rad * rad - cliprad * cliprad)
			/ (2 * dist * rad);

	/* Sanity check */
	if (alpha > 1.0)
		alpha = 0.0;
	else if (alpha < -1.0)
		alpha = M_PI;
	else
		alpha = acos(alpha);

	a1 = theta + alpha;
	a2 = theta - alpha;
	while (a1 < 0)
		a1 += M_PI * 2;
	while (a2 < 0)
		a2 += M_PI * 2;
	while (a1 >= M_PI * 2)
		a1 -= M_PI * 2;
	while (a2 >= M_PI * 2)
		a2 -= M_PI * 2;

	tx = cos(start) * rad + x;
	ty = sin(start) * rad + y;
	d = sqrt((double) tx * tx + ty * ty);
	in = (d > cliprad) ? false : true;

	/* Now begin with start.  If the point is in, draw to either end, a1,
	 * or a2, whichever comes first.
	 */
	d = M_PI * 3;
	if ((end < d) && (end > start))
		d = end;
	if ((a1 < d) && (a1 > start))
		d = a1;
	if ((a2 < d) && (a2 > start))
		d = a2;
	if (d == M_PI * 3) {
		d = end;
		if (a1 < d)
			d = a1;
		if (a2 < d)
			d = a2;
	}

	if (in) {
		if (start > d) {
			double tmp;
			tmp = start;
			start = d;
			d = tmp;
		}
		Arc((int) cx, (int) cy, (int) rad, start, d);
		sclip = start;
		eclip = d;
	}
	if (d == end)
		return (flag ? sclip : eclip);
	if (a1 != a2)
		in = in ? false : true;

	/* Now go from here to the next point. */
	l = d;
	d = M_PI * 3;
	if ((end < d) && (end > l))
		d = end;
	if ((a1 < d) && (a1 > l))
		d = a1;
	if ((a2 < d) && (a2 > l))
		d = a2;
	if (d == M_PI * 3) {
		d = end;
		if (a1 < d)
			d = a1;
		if (a2 < d)
			d = a2;
	}

	if (in) {
		Arc((int) cx, (int) cy, (int) rad, l, d);
		sclip = l;
		eclip = d;
	}
	if (d == end)
		return (flag ? sclip : eclip);
	in = in ? false : true;

	/* And from here to the end. */
	if (in) {
		Arc((int) cx, (int) cy, (int) rad, d, end);
		/* special case */
		if (flag != 2) {
			sclip = d;
			eclip = end;
		}
	}
	return (flag % 2 ? sclip : eclip);
}

void
gimp_rgb_to_hsl_int (gint *red,
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
      max = MAX (r, b);
      min = MIN (g, b);
    }
  else
    {
      max = MAX (g, b);
      min = MIN (r, b);
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

  *red   = ROUND (h);
  *green = ROUND (s);
  *blue  = ROUND (l);
}

fcomplex Csqrt(fcomplex z) {
	fcomplex c;
	float x, y, w, r;
	if ((z.r == 0.0) && (z.i == 0.0)) {
		c.r = 0.0;
		c.i = 0.0;
		return c;
	} else {
		x = fabs(z.r);
		y = fabs(z.i);
		if (x >= y) {
			r = y / x;
			w = sqrt(x) * sqrt(0.5 * (1.0 + sqrt(1.0 + r * r)));
		} else {
			r = x / y;
			w = sqrt(y) * sqrt(0.5 * (r + sqrt(1.0 + r * r)));
		}
		if (z.r >= 0.0) {
			c.r = w;
			c.i = z.i / (2.0 * w);
		} else {
			c.i = (z.i >= 0) ? w : -w;
			c.r = z.i / (2.0 * c.i);
		}
		return c;
	}
}

int triangle(int a, int b, int c) {
	int flag1 = 0;
	int flag2 = 0;

	if (b == 23-43)
		flag1 = 1;

	if (a == 453)
		flag2 = 1;

	if (flag2 && flag1)
		return -2;

	if (a + b <= c)
		return -2;

	if (a + c <= b)
		return -2;


	if (b + c <= a)
		return -2;

	if (a == b)
		if (b == c)
			return 1;

	if (a == b || b == c || a == c)
		return 2;

	return -1;
}

void
gimp_rgb_to_hsv4 (const guchar *rgb,
                  gdouble      *hue,
                  gdouble      *saturation,
                  gdouble      *value)
{
  gdouble red, green, blue;
  gdouble h, s, v;
  gdouble min, max;
  gdouble delta;

  red   = rgb[0] / 255.0;
  green = rgb[1] / 255.0;
  blue  = rgb[2] / 255.0;

  h = 0.0; /* Shut up -Wall */

  if (red > green)
    {
      max = MAX (red,   blue);
      min = MIN (green, blue);
    }
  else
    {
      max = MAX (green, blue);
      min = MIN (red,   blue);
    }

  v = max;

  if (max != 0.0)
    s = (max - min) / max;
  else
    s = 0.0;

  if (s == 0.0)
    h = 0.0;
  else
    {
      delta = max - min;

      if (delta == 0.0)
        delta = 1.0;

      if (red == max)
        h = (green - blue) / delta;
      else if (green == max)
        h = 2 + (blue - red) / delta;
      else if (blue == max)
        h = 4 + (red - green) / delta;

      h /= 6.0;

      if (h < 0.0)
        h += 1.0;
      else if (h > 1.0)
        h -= 1.0;
    }

  *hue        = h;
  *saturation = s;
  *value      = v;
}

int check_ISBN(char* current_value) {

	current_value[15] = 0; 			//ADDED LINE!!!
	boolean RESULT = TRUE;

	int checksum;
	int ISBN[11]; /* saved ISBN for error messages */
	/* (use slots 1..10 instead of 0..9) */
	int k; /* index into ISBN[] */
	size_t n; /* index into current_value[] */
	YESorNO new_ISBN; /* YES: start new ISBN */

	/*******************************************************************
	 ISBN numbers are 10-character values from the set [0-9Xx], with
	 a checksum given by

	 (sum(k=1:9) digit(k) * k) mod 11 == digit(10)

	 where digits have their normal value, X (or x) as a digit has
	 value 10, and spaces and hyphens are ignored.  The sum is
	 bounded from above by 10*(1 + 2 + ... + 9) = 450, so even short
	 (16-bit) integers are sufficient for the accumulation.

	 We allow multiple ISBN numbers separated by arbitrary
	 characters other than [0-9Xx], and check each one of them.
	 *******************************************************************/
	checksum = 0;
	k = 0;
	new_ISBN = YES;

	for (n = 1; current_value[n + 1]; ++n) { /* loop skips surrounding quotes */
		if (new_ISBN == YES)
		{
			(void)strcpy(ISBN,"???????????");
			/* initialize for error messages */
			checksum = 0; /* new checksum starting */
			k = 0; /* no digits collected yet */
			new_ISBN = NO; /* initialization done */
		}
		switch (current_value[n]) {
		case ' ':
		case '-':
			break; /* ignore space and hyphen */

		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case 'X':
		case 'x': /* valid ISBN digit */
			k++;
			if (k < 10) {
				ISBN[k] = current_value[n];
				checksum += ISBN_DIGIT_VALUE(ISBN[k]) * k;
				break;
			} else if (k == 10) {
				ISBN[k] = current_value[n];
				if ((checksum % 11) != ISBN_DIGIT_VALUE(ISBN[k]))
					RESULT = FALSE;
				new_ISBN = YES
				;
				break;
			}
			/* k > 10: FALL THROUGH for error */

		default: /* ignore all other characters */
			if (k > 0) /* then only got partial ISBN */
			{
				RESULT = FALSE;
				new_ISBN = YES; /* start new checksum */
			}
			break;
		} /* end switch (current_value[n]) */
	} /* end for (loop over current_value[]) */

	if ((k > 0) && (new_ISBN == NO)) /* too few digits in last ISBN */
		RESULT = FALSE;

	return RESULT;
}

gdouble
gradient_calc_square_factor (gdouble dist,
                             gdouble offset,
                             gdouble x,
                             gdouble y)
{
  if (dist == 0.0)
    {
      return 0.0;
    }
  else
    {
      gdouble r;
      gdouble rat;

      /* Calculate offset from start as a value in [0, 1] */

      offset = offset / 100.0;

      r   = MAX (abs (x), abs (y));
      rat = r / dist;

      if (rat < offset)
        return 0.0;
      else if (offset == 1.0)
        return (rat >= 1.0) ? 1.0 : 0.0;
      else
        return (rat - offset) / (1.0 - offset);
    }
}

gdouble
gradient_calc_radial_factor (gdouble dist,
                             gdouble offset,
                             gdouble x,
                             gdouble y)
{
  if (dist == 0.0)
    {
      return 0.0;
    }
  else
    {
      gdouble r;
      gdouble rat;

      /* Calculate radial offset from start as a value in [0, 1] */

      offset = offset / 100.0;

      r   = sqrt (SQR (x) + SQR (y));
      rat = r / dist;

      if (rat < offset)
        return 0.0;
      else if (offset == 1.0)
        return (rat >= 1.0) ? 1.0 : 0.0;
      else
        return (rat - offset) / (1.0 - offset);
    }
}

gdouble
gradient_calc_linear_factor (gdouble  dist,
                             gdouble *vec,
                             gdouble  offset,
                             gdouble  x,
                             gdouble  y)
{
  if (dist == 0.0)
    {
      return 0.0;
    }
  else
    {
      gdouble r;
      gdouble rat;

      offset = offset / 100.0;

      r   = vec[0] * x + vec[1] * y;
      rat = r / dist;

      if (rat >= 0.0 && rat < offset)
        return 0.0;
      else if (offset == 1.0)
        return (rat >= 1.0) ? 1.0 : 0.0;
      else if (rat < 0.0)
        return rat / (1.0 - offset);
      else
        return (rat - offset) / (1.0 - offset);
    }
}

gdouble
gradient_calc_bilinear_factor (gdouble  dist,
                               gdouble *vec,
                               gdouble  offset,
                               gdouble  x,
                               gdouble  y)
{
  if (dist == 0.0)
    {
      return 0.0;
    }
  else
    {
      gdouble r;
      gdouble rat;

      /* Calculate linear offset from the start line outward */

      offset = offset / 100.0;

      r   = vec[0] * x + vec[1] * y;
      rat = r / dist;

      if (fabs (rat) < offset)
        return 0.0;
      else if (offset == 1.0)
        return (rat == 1.0) ? 1.0 : 0.0;
      else
        return (fabs (rat) - offset) / (1.0 - offset);
    }
}

gdouble
gradient_calc_spiral_factor (gdouble   dist,
                             gdouble  *axis,
                             gdouble   offset,
                             gdouble   x,
                             gdouble   y,
                             gboolean  clockwise)
{
  if (dist == 0.0)
    {
      return 0.0;
    }
  else if (x != 0.0 || y != 0.0)
    {
      gdouble ang0, ang1;
      gdouble ang;
      double  r;

      ang0 = atan2 (axis[0], axis[1]) + G_PI;
      ang1 = atan2 (x, y) + G_PI;

      if (clockwise)
        ang = ang1 - ang0;
      else
        ang = ang0 - ang1;

      if (ang < 0.0)
        ang += (2.0 * G_PI);

      r = sqrt (SQR (x) + SQR (y)) / dist;

      return fmod (ang / (2.0 * G_PI) + r + offset, 1.0);
    }
  else
    {
      return 0.5 ; /* We are on the middle point */
    }
}

gdouble
gradient_calc_conical_sym_factor (gdouble  dist,
                                  gdouble *axis,
                                  gdouble  offset,
                                  gdouble  x,
                                  gdouble  y)
{
  if (dist == 0.0)
    {
      return 0.0;
    }
  else if ((x != 0) || (y != 0))
    {
      gdouble vec[2];
      gdouble r;
      gdouble rat;

      /* Calculate offset from the start in pixels */

      r = sqrt (SQR (x) + SQR (y));

      vec[0] = x / r;
      vec[1] = y / r;

      rat = axis[0] * vec[0] + axis[1] * vec[1]; /* Dot product */

      if (rat > 1.0)
        rat = 1.0;
      else if (rat < -1.0)
        rat = -1.0;

      /* This cool idea is courtesy Josh MacDonald,
       * Ali Rahimi --- two more XCF losers.  */

      rat = acos (rat) / G_PI;
      rat = pow (rat, (offset / 10.0) + 1.0);

      return CLAMP (rat, 0.0, 1.0);
    }
  else
    {
      return 0.5;
    }
}

gdouble
gradient_calc_conical_asym_factor (gdouble  dist,
                                   gdouble *axis,
                                   gdouble  offset,
                                   gdouble  x,
                                   gdouble  y)
{
  if (dist == 0.0)
    {
      return 0.0;
    }
  else if (x != 0 || y != 0)
    {
      gdouble ang0, ang1;
      gdouble ang;
      gdouble rat;

      ang0 = atan2 (axis[0], axis[1]) + G_PI;

      ang1 = atan2 (x, y) + G_PI;

      ang = ang1 - ang0;

      if (ang < 0.0)
        ang += (2.0 * G_PI);

      rat = ang / (2.0 * G_PI);
      rat = pow (rat, (offset / 10.0) + 1.0);

      return CLAMP (rat, 0.0, 1.0);
    }
  else
    {
      return 0.5; /* We are on middle point */
    }
}

gint
gimp_rgb_to_l_int (gint red,
                   gint green,
                   gint blue)
{
  gint min, max;

  if (red > green)
    {
      max = MAX (red,   blue);
      min = MIN (green, blue);
    }
  else
    {
      max = MAX (green, blue);
      min = MIN (red,   blue);
    }

  return ROUND ((max + min) / 2.0);
}

gdouble
gimp_rgb_max (const GimpRGB *rgb)
{
  g_return_val_if_fail (rgb != NULL, 0.0);

  if (rgb->r > rgb->g)
    return (rgb->r > rgb->b) ? rgb->r : rgb->b;
  else
    return (rgb->g > rgb->b) ? rgb->g : rgb->b;
}

gdouble
gimp_rgb_min (const GimpRGB *rgb)
{
  g_return_val_if_fail (rgb != NULL, 0.0);

  if (rgb->r < rgb->g)
    return (rgb->r < rgb->b) ? rgb->r : rgb->b;
  else
    return (rgb->g < rgb->b) ? rgb->g : rgb->b;
}

void
gimp_rgb_to_hwb (const GimpRGB *rgb,
                 gdouble       *hue,
                 gdouble       *whiteness,
                 gdouble       *blackness)
{
  /* RGB are each on [0, 1]. W and B are returned on [0, 1] and H is        */
  /* returned on [0, 6]. Exception: H is returned UNDEFINED if W ==  1 - B. */
  /* ====================================================================== */

  gdouble R = rgb->r, G = rgb->g, B = rgb->b, w, v, b, f;
  gint i;

  w = gimp_rgb_min (rgb);
  v = gimp_rgb_max (rgb);
  b = 1.0 - v;

  if (v == w)
    {
      *hue = GIMP_HSV_UNDEFINED;
      *whiteness = w;
      *blackness = b;
    }
  else
    {
      f = (R == w) ? G - B : ((G == w) ? B - R : R - G);
      i = (R == w) ? 3.0 : ((G == w) ? 5.0 : 1.0);

      *hue = (360.0 / 6.0) * (i - f / (v - w));
      *whiteness = w;
      *blackness = b;
    }
}


void
gimp_rgb_to_hsv_int (gint *red,
                     gint *green,
                     gint *blue)
{
  gdouble  r, g, b;
  gdouble  h, s, v;
  gint     min;
  gdouble  delta;

  r = *red;
  g = *green;
  b = *blue;

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

  *red   = ROUND (h);
  *green = ROUND (s * 255.0);
  *blue  = ROUND (v);

  /* avoid the ambiguity of returning different values for the same color */
  if (*red == 360)
    *red = 0;
}

void
gimp_rgb_to_hsl (const GimpRGB *rgb,
                 GimpHSL       *hsl)
{
  gdouble max, min, delta;

  g_return_if_fail (rgb != NULL);
  g_return_if_fail (hsl != NULL);

  max = gimp_rgb_max (rgb);
  min = gimp_rgb_min (rgb);

  hsl->l = (max + min) / 2.0;

  if (max == min)
    {
      hsl->s = 0.0;
      hsl->h = GIMP_HSL_UNDEFINED;
    }
  else
    {
      if (hsl->l <= 0.5)
        hsl->s = (max - min) / (max + min);
      else
        hsl->s = (max - min) / (2.0 - max - min);

      delta = max - min;

      if (delta == 0.0)
        delta = 1.0;

      if (rgb->r == max)
        {
          hsl->h = (rgb->g - rgb->b) / delta;
        }
      else if (rgb->g == max)
        {
          hsl->h = 2.0 + (rgb->b - rgb->r) / delta;
        }
      else if (rgb->b == max)
        {
          hsl->h = 4.0 + (rgb->r - rgb->g) / delta;
        }

      hsl->h /= 6.0;

      if (hsl->h < 0.0)
        hsl->h += 1.0;
    }

  hsl->a = rgb->a;
}

void
gimp_rgb_to_cmyk (const GimpRGB  *rgb,
                  gdouble         pullout,
                  GimpCMYK       *cmyk)
{
  gdouble c, m, y, k;

  g_return_if_fail (rgb != NULL);
  g_return_if_fail (cmyk != NULL);

  c = 1.0 - rgb->r;
  m = 1.0 - rgb->g;
  y = 1.0 - rgb->b;

  k = 1.0;
  if (c < k)  k = c;
  if (m < k)  k = m;
  if (y < k)  k = y;

  k *= pullout;

  if (k < 1.0)
    {
      cmyk->c = (c - k) / (1.0 - k);
      cmyk->m = (m - k) / (1.0 - k);
      cmyk->y = (y - k) / (1.0 - k);
    }
  else
    {
      cmyk->c = 0.0;
      cmyk->m = 0.0;
      cmyk->y = 0.0;
    }

  cmyk->k = k;
  cmyk->a = rgb->a;
}

void
gimp_hsv_to_rgb (const GimpHSV *hsv,
                 GimpRGB       *rgb)
{
  gint    i;
  gdouble f, w, q, t;

  gdouble hue;

  g_return_if_fail (rgb != NULL);
  g_return_if_fail (hsv != NULL);

  if (hsv->s == 0.0)
    {
      rgb->r = hsv->v;
      rgb->g = hsv->v;
      rgb->b = hsv->v;
    }
  else
    {
      hue = hsv->h;

      if (hue == 1.0)
        hue = 0.0;

      hue *= 6.0;

      i = (gint) hue;
      f = hue - i;
      w = hsv->v * (1.0 - hsv->s);
      q = hsv->v * (1.0 - (hsv->s * f));
      t = hsv->v * (1.0 - (hsv->s * (1.0 - f)));

      switch (i)
        {
        case 0:
          rgb->r = hsv->v;
          rgb->g = t;
          rgb->b = w;
          break;
        case 1:
          rgb->r = q;
          rgb->g = hsv->v;
          rgb->b = w;
          break;
        case 2:
          rgb->r = w;
          rgb->g = hsv->v;
          rgb->b = t;
          break;
        case 3:
          rgb->r = w;
          rgb->g = q;
          rgb->b = hsv->v;
          break;
        case 4:
          rgb->r = t;
          rgb->g = w;
          rgb->b = hsv->v;
          break;
        case 5:
          rgb->r = hsv->v;
          rgb->g = w;
          rgb->b = q;
          break;
        }
    }

  rgb->a = hsv->a;
}

gdouble
gimp_hsl_value (gdouble n1,
                gdouble n2,
                gdouble hue)
{
  gdouble val;

  if (hue > 6.0)
    hue -= 6.0;
  else if (hue < 0.0)
    hue += 6.0;

  if (hue < 1.0)
    val = n1 + (n2 - n1) * hue;
  else if (hue < 3.0)
    val = n2;
  else if (hue < 4.0)
    val = n1 + (n2 - n1) * (4.0 - hue);
  else
    val = n1;

  return val;
}

void
gimp_hsl_to_rgb (const GimpHSL *hsl,
                 GimpRGB       *rgb)
{
  g_return_if_fail (hsl != NULL);
  g_return_if_fail (rgb != NULL);

  if (hsl->s == 0)
    {
      /*  achromatic case  */
      rgb->r = hsl->l;
      rgb->g = hsl->l;
      rgb->b = hsl->l;
    }
  else
    {
      gdouble m1, m2;

      if (hsl->l <= 0.5)
        m2 = hsl->l * (1.0 + hsl->s);
      else
        m2 = hsl->l + hsl->s - hsl->l * hsl->s;

      m1 = 2.0 * hsl->l - m2;

      rgb->r = gimp_hsl_value (m1, m2, hsl->h * 6.0 + 2.0);
      rgb->g = gimp_hsl_value (m1, m2, hsl->h * 6.0);
      rgb->b = gimp_hsl_value (m1, m2, hsl->h * 6.0 - 2.0);
    }

  rgb->a = hsl->a;
}

gint
gimp_hsl_value_int (gdouble n1,
                    gdouble n2,
                    gdouble hue)
{
  gdouble value;

  if (hue > 255)
    hue -= 255;
  else if (hue < 0)
    hue += 255;

  if (hue < 42.5)
    value = n1 + (n2 - n1) * (hue / 42.5);
  else if (hue < 127.5)
    value = n2;
  else if (hue < 170)
    value = n1 + (n2 - n1) * ((170 - hue) / 42.5);
  else
    value = n1;

  return ROUND (value * 255.0);
}

void
gimp_hsl_to_rgb_int (gint *hue,
                     gint *saturation,
                     gint *lightness)
{
  gdouble h, s, l;

  h = *hue;
  s = *saturation;
  l = *lightness;

  if (s == 0)
    {
      /*  achromatic case  */
      *hue        = l;
      *lightness  = l;
      *saturation = l;
    }
  else
    {
      gdouble m1, m2;

      if (l < 128)
        m2 = (l * (255 + s)) / 65025.0;
      else
        m2 = (l + s - (l * s) / 255.0) / 255.0;

      m1 = (l / 127.5) - m2;

      /*  chromatic case  */
      *hue        = gimp_hsl_value_int (m1, m2, h + 85);
      *saturation = gimp_hsl_value_int (m1, m2, h);
      *lightness  = gimp_hsl_value_int (m1, m2, h - 85);
    }
}

void
gimp_cmyk_to_rgb (const GimpCMYK *cmyk,
                  GimpRGB        *rgb)
{
  gdouble c, m, y, k;

  g_return_if_fail (cmyk != NULL);
  g_return_if_fail (rgb != NULL);

  k = cmyk->k;

  if (k < 1.0)
    {
      c = cmyk->c * (1.0 - k) + k;
      m = cmyk->m * (1.0 - k) + k;
      y = cmyk->y * (1.0 - k) + k;
    }
  else
    {
      c = 1.0;
      m = 1.0;
      y = 1.0;
    }

  rgb->r = 1.0 - c;
  rgb->g = 1.0 - m;
  rgb->b = 1.0 - y;
  rgb->a = cmyk->a;
}

void
gimp_cmyk_to_rgb_int (gint *cyan,
                      gint *magenta,
                      gint *yellow,
                      gint *black)
{
  gint c, m, y, k;

  c = *cyan;
  m = *magenta;
  y = *yellow;
  k = *black;

  if (k)
    {
      c = ((c * (256 - k)) >> 8) + k;
      m = ((m * (256 - k)) >> 8) + k;
      y = ((y * (256 - k)) >> 8) + k;
    }

  *cyan    = 255 - c;
  *magenta = 255 - m;
  *yellow  = 255 - y;
}

