/**********
Copyright 1990 Regents of the University of California.  All rights reserved.
**********/

/*

    Routines to draw the various sorts of grids -- linear, log, polar.
*/

#define M_PI 3.1415926
#define RAD_TO_DEG	(180.0 / M_PI)

#include <math.h>

static double *lingrid(), *loggrid();
static void polargrid(), smithgrid();
static void drawpolargrid( );
static void drawsmithgrid( );

static void arcset();
double cliparc();
static void adddeglabel(), addradlabel();

typedef enum { x_axis, y_axis } Axis;


/* This routine draws an arc and clips it to a circle.  It's hard to figure
 * out how it works without looking at the piece of scratch paaper I have
 * in front of me, so let's hope it doesn't break...
 * Converted to all doubles for CRAYs
 */

void Arc(int a, int b, int c, double d, double e){

}

double
cliparc(cx, cy, rad, start, end, iclipx, iclipy, icliprad, flag)
    double cx, cy, rad;
    int iclipx, iclipy, icliprad, flag;
    double start, end;
{
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
    dist = sqrtf((double) (x * x + y * y));

    if (!rad || !cliprad)
        return(-1);
    if (dist + rad < cliprad) {
        /* The arc is entirely in the boundary. */
        Arc((int)cx, (int)cy, (int)rad, start, end);
        return(flag?start:end);
    } else if ((dist - rad >= cliprad) || (rad - dist >= cliprad)) {
        /* The arc is outside of the boundary. */
        return(-1);
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

    alpha = (double) (dist * dist + rad * rad - cliprad * cliprad) /
            (2 * dist * rad);

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
    ty = sinf(start) * rad + y;
    d = sqrtf((double) tx * tx + ty * ty);
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
        Arc((int)cx, (int)cy, (int)rad, start, d);
	sclip = start;
	eclip = d;
    }
    if (d == end)
        return(flag?sclip:eclip);
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
        Arc((int)cx, (int)cy, (int)rad, l, d);
	sclip = l;
	eclip = d;
    }
    if (d == end)
        return(flag?sclip:eclip);
    in = in ? false : true;

    /* And from here to the end. */
    if (in) {
        Arc((int)cx, (int)cy, (int)rad, d, end);
	/* special case */
	if (flag != 2) {
	  sclip = d;
	  eclip = end;
	}
    }
    return(flag%2?sclip:eclip);
}
