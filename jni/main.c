#include <math.h>
#include "ocelot.h"
int test_me(int a, int b, int c)
{
    if(_f_ocelot_trace(a < 0, _f_ocelot_gt_numeric(0, a), _f_ocelot_ge_numeric(a, 0)))
        return 0;

    while(_f_ocelot_trace(b > c, _f_ocelot_gt_numeric(b, c), _f_ocelot_ge_numeric(c, b))){
        if(_f_ocelot_trace(a > c, _f_ocelot_gt_numeric(a, c), _f_ocelot_ge_numeric(c, a))){
            c++;
            b -= c;
        }else{
            b -= a;
        }
    }

    c++;
    return 1;
}

