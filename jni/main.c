#include <stdio.h>
#include <math.h>
#define OCELOT_TESTFUNCTION triangle
#define MAX(a, b) (a > b ? a : b)
#define MIN(a, b) (a < b ? a : b)
#define ROUND(a) (int)a
#include "ocelot.h"
typedef int gint;
typedef double gdouble;
int triangle(int, int, int);
int triangle(int a, int b, int c)
{
    if(_f_ocelot_trace(a == b, _f_ocelot_eq_numeric(a, b), _f_ocelot_neq_numeric(a, b)))
        if(_f_ocelot_trace(b == c, _f_ocelot_eq_numeric(b, c), _f_ocelot_neq_numeric(b, c)))
            return 1;


    if(_f_ocelot_trace(a == b || b == c || c == a, _f_ocelot_or(_f_ocelot_or(_f_ocelot_eq_numeric(a, b), _f_ocelot_eq_numeric(b, c)), _f_ocelot_eq_numeric(c, a)), _f_ocelot_and(_f_ocelot_and(_f_ocelot_neq_numeric(a, b), _f_ocelot_neq_numeric(b, c)), _f_ocelot_neq_numeric(c, a))))
        return 2;

    return 3;
}
