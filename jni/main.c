#define M_PI 3.1415926
#define RAD_TO_DEG	(180.0 / M_PI)
#include <math.h>
#include "ocelot.h"
static double *lingrid(), *loggrid();
static void polargrid(), smithgrid();
static void drawpolargrid();
static void drawsmithgrid();
static void arcset();
double cliparc();
static void adddeglabel(), addradlabel();
typedef enum { x_axis, y_axis} Axis;
void Arc(int a, int b, int c, double d, double e)
{
}

double cliparc(cx, cy, rad, start, end, iclipx, iclipy, icliprad, flag)
double cx, cy, rad;
int iclipx, iclipy, icliprad, flag;
double start, end;
{
    double clipx, clipy, cliprad;
    double x, y, tx, ty, dist;
    double alpha, theta, phi, a1, a2, d, l;
    double sclip, eclip;
    bool in;
    clipx = (double)iclipx;
    clipy = (double)iclipy;
    cliprad = (double)icliprad;
    x = cx - clipx;
    y = cy - clipy;
    dist = sqrt((double)(x * x + y * y));
    if(_f_ocelot_trace(!rad || !cliprad, _f_ocelot_or(_f_ocelot_isfalse(rad), _f_ocelot_isfalse(cliprad)), _f_ocelot_and(_f_ocelot_istrue(rad), _f_ocelot_istrue(cliprad))))
        return (-1);

    if(_f_ocelot_trace(dist + rad < cliprad, _f_ocelot_gt_numeric(cliprad, dist + rad), _f_ocelot_ge_numeric(dist + rad, cliprad))){
        Arc((int)cx, (int)cy, (int)rad, start, end);
        return (flag ? start : end);
    }else
        if(_f_ocelot_trace((dist - rad >= cliprad) || (rad - dist >= cliprad), _f_ocelot_or(_f_ocelot_ge_numeric(dist - rad, cliprad), _f_ocelot_ge_numeric(rad - dist, cliprad)), _f_ocelot_and(_f_ocelot_gt_numeric(cliprad, dist - rad), _f_ocelot_gt_numeric(cliprad, rad - dist)))){
            return (-1);
        }

    if(_f_ocelot_trace(x, _f_ocelot_istrue(x), _f_ocelot_isfalse(x)))
        phi = atan2((double)y, (double)x);

    else
        if(_f_ocelot_trace(y > 0, _f_ocelot_gt_numeric(y, 0), _f_ocelot_ge_numeric(0, y)))
            phi = 3.1415926 * 1.5;

        else
            phi = 3.1415926 / 2;


    if(_f_ocelot_trace(cx > clipx, _f_ocelot_gt_numeric(cx, clipx), _f_ocelot_ge_numeric(clipx, cx)))
        theta = 3.1415926 + phi;

    else
        theta = phi;

    alpha = (double)(dist * dist + rad * rad - cliprad * cliprad) / (2 * dist * rad);
    if(_f_ocelot_trace(alpha > 1.0, _f_ocelot_gt_numeric(alpha, 1.0), _f_ocelot_ge_numeric(1.0, alpha)))
        alpha = 0.0;

    else
        if(_f_ocelot_trace(alpha < -1.0, _f_ocelot_gt_numeric(1.0, alpha), _f_ocelot_ge_numeric(alpha, 1.0)))
            alpha = 3.1415926;

        else
            alpha = acos(alpha);


    a1 = theta + alpha;
    a2 = theta - alpha;
    while(_f_ocelot_trace(a1 < 0, _f_ocelot_gt_numeric(0, a1), _f_ocelot_ge_numeric(a1, 0)))
        a1 += 3.1415926 * 2;

    while(_f_ocelot_trace(a2 < 0, _f_ocelot_gt_numeric(0, a2), _f_ocelot_ge_numeric(a2, 0)))
        a2 += 3.1415926 * 2;

    while(_f_ocelot_trace(a1 >= 3.1415926 * 2, _f_ocelot_ge_numeric(a1, 3.1415926 * 2), _f_ocelot_gt_numeric(3.1415926 * 2, a1)))
        a1 -= 3.1415926 * 2;

    while(_f_ocelot_trace(a2 >= 3.1415926 * 2, _f_ocelot_ge_numeric(a2, 3.1415926 * 2), _f_ocelot_gt_numeric(3.1415926 * 2, a2)))
        a2 -= 3.1415926 * 2;

    tx = cos(start) * rad + x;
    ty = sin(start) * rad + y;
    d = sqrt((double)tx * tx + ty * ty);
    in = (d > cliprad) ? false : true;
    d = 3.1415926 * 3;
    if(_f_ocelot_trace((end < d) && (end > start), _f_ocelot_and(_f_ocelot_gt_numeric(d, end), _f_ocelot_gt_numeric(end, start)), _f_ocelot_or(_f_ocelot_ge_numeric(d, end), _f_ocelot_ge_numeric(start, end))))
        d = end;

    if(_f_ocelot_trace((a1 < d) && (a1 > start), _f_ocelot_and(_f_ocelot_gt_numeric(d, a1), _f_ocelot_gt_numeric(a1, start)), _f_ocelot_or(_f_ocelot_ge_numeric(d, a1), _f_ocelot_ge_numeric(start, a1))))
        d = a1;

    if(_f_ocelot_trace((a2 < d) && (a2 > start), _f_ocelot_and(_f_ocelot_gt_numeric(d, a2), _f_ocelot_gt_numeric(a2, start)), _f_ocelot_or(_f_ocelot_ge_numeric(d, a2), _f_ocelot_ge_numeric(start, a2))))
        d = a2;

    if(_f_ocelot_trace(d == 3.1415926 * 3, _f_ocelot_eq_numeric(d, 3.1415926 * 3), _f_ocelot_neq_numeric(d, 3.1415926 * 3))){
        d = end;
        if(_f_ocelot_trace(a1 < d, _f_ocelot_gt_numeric(d, a1), _f_ocelot_ge_numeric(a1, d)))
            d = a1;

        if(_f_ocelot_trace(a2 < d, _f_ocelot_gt_numeric(d, a2), _f_ocelot_ge_numeric(a2, d)))
            d = a2;

    }
    if(_f_ocelot_trace(in, _f_ocelot_istrue(in), _f_ocelot_isfalse(in))){
        if(_f_ocelot_trace(start > d, _f_ocelot_gt_numeric(start, d), _f_ocelot_ge_numeric(d, start))){
            double tmp;
            tmp = start;
            start = d;
            d = tmp;
        }
        Arc((int)cx, (int)cy, (int)rad, start, d);
        sclip = start;
        eclip = d;
    }

    if(_f_ocelot_trace(d == end, _f_ocelot_eq_numeric(d, end), _f_ocelot_neq_numeric(d, end)))
        return (flag ? sclip : eclip);

    if(_f_ocelot_trace(a1 != a2, _f_ocelot_neq_numeric(a1, a2), _f_ocelot_eq_numeric(a1, a2)))
        in = in ? false : true;

    l = d;
    d = 3.1415926 * 3;
    if(_f_ocelot_trace((end < d) && (end > l), _f_ocelot_and(_f_ocelot_gt_numeric(d, end), _f_ocelot_gt_numeric(end, l)), _f_ocelot_or(_f_ocelot_ge_numeric(d, end), _f_ocelot_ge_numeric(l, end))))
        d = end;

    if(_f_ocelot_trace((a1 < d) && (a1 > l), _f_ocelot_and(_f_ocelot_gt_numeric(d, a1), _f_ocelot_gt_numeric(a1, l)), _f_ocelot_or(_f_ocelot_ge_numeric(d, a1), _f_ocelot_ge_numeric(l, a1))))
        d = a1;

    if(_f_ocelot_trace((a2 < d) && (a2 > l), _f_ocelot_and(_f_ocelot_gt_numeric(d, a2), _f_ocelot_gt_numeric(a2, l)), _f_ocelot_or(_f_ocelot_ge_numeric(d, a2), _f_ocelot_ge_numeric(l, a2))))
        d = a2;

    if(_f_ocelot_trace(d == 3.1415926 * 3, _f_ocelot_eq_numeric(d, 3.1415926 * 3), _f_ocelot_neq_numeric(d, 3.1415926 * 3))){
        d = end;
        if(_f_ocelot_trace(a1 < d, _f_ocelot_gt_numeric(d, a1), _f_ocelot_ge_numeric(a1, d)))
            d = a1;

        if(_f_ocelot_trace(a2 < d, _f_ocelot_gt_numeric(d, a2), _f_ocelot_ge_numeric(a2, d)))
            d = a2;

    }
    if(_f_ocelot_trace(in, _f_ocelot_istrue(in), _f_ocelot_isfalse(in))){
        Arc((int)cx, (int)cy, (int)rad, l, d);
        sclip = l;
        eclip = d;
    }
    if(_f_ocelot_trace(d == end, _f_ocelot_eq_numeric(d, end), _f_ocelot_neq_numeric(d, end)))
        return (flag ? sclip : eclip);

    in = in ? false : true;
    if(_f_ocelot_trace(in, _f_ocelot_istrue(in), _f_ocelot_isfalse(in))){
        Arc((int)cx, (int)cy, (int)rad, d, end);
        if(_f_ocelot_trace(flag != 2, _f_ocelot_neq_numeric(flag, 2), _f_ocelot_eq_numeric(flag, 2))){
            sclip = d;
            eclip = end;
        }
    }

    return (flag % 2 ? sclip : eclip);
}

