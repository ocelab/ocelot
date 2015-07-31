#define __GIMP_COLOR_H_INSIDE__

#include "config.h"
typedef int gint;
typedef double gdouble;
typedef unsigned char guchar;

#define ROUND(x) (int)(x)
#define MAX(x, y) ((x) > (y) ? (x) : (y))
#define MIN(x, y) ((x) < (y) ? (x) : (y))

//#include "libgimpmath/gimpmath.h"

/* LIBGIMP - The GIMP Library
 * Copyright (C) 1995-1997 Peter Mattis and Spencer Kimball
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

/* For information look into the C source or the html documentation */


typedef struct _GimpColorManaged GimpColorManaged;  /* dummy typedef */

/*  usually we don't keep the structure definitions in the types file
 *  but GimpRGB appears in too many header files...
 */

/**
 * GimpRGB:
 * @r: the red component
 * @g: the green component
 * @b: the blue component
 * @a: the alpha component
 *
 * Used to keep RGB and RGBA colors. All components are in a range of
 * [0.0..1.0].
 **/
typedef struct
{
  gdouble r, g, b, a;
} GimpRGB;

/**
 * GimpHSV:
 * @h: the hue component
 * @s: the saturation component
 * @v: the value component
 * @a: the alpha component
 *
 * Used to keep HSV and HSVA colors. All components are in a range of
 * [0.0..1.0].
 **/
typedef struct
{
  gdouble h, s, v, a;
} GimpHSV;

/**
 * GimpHSL:
 * @h: the hue component
 * @s: the saturation component
 * @l: the lightness component
 * @a: the alpha component
 *
 * Used to keep HSL and HSLA colors. All components are in a range of
 * [0.0..1.0].
 **/
typedef struct
{
  gdouble h, s, l, a;
} GimpHSL;

/**
 * GimpCMYK:
 * @c: the cyan component
 * @m: the magenta component
 * @y: the yellow component
 * @k: the black component
 * @a: the alpha component
 *
 * Used to keep CMYK and CMYKA colors. All components are in a range
 * of [0.0..1.0]. An alpha value is somewhat useless in the CMYK
 * colorspace, but we keep one around anyway so color conversions
 * going to CMYK and back can preserve alpha.
 **/
typedef struct
{
  gdouble c, m, y, k, a;
} GimpCMYK;



#define GIMP_HSV_UNDEFINED -1.0
#define GIMP_HSL_UNDEFINED -1.0

/*********************************
 *   color conversion routines   *
 *********************************/
