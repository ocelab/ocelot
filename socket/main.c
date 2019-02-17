#include "ocelot.h"

typedef struct _data
{
    int *internal;
    int a, b;
    struct _data *next;
} Data;

void case2(Data *d, char **str)
{
    if(_f_ocelot_trace(d == 0 || d->internal == 0, _f_ocelot_or(_f_ocelot_eq_pointer(d, 0), _f_ocelot_eq_pointer(d->internal, 0)), _f_ocelot_and(_f_ocelot_neq_pointer(d, 0), _f_ocelot_neq_pointer(d->internal, 0))))
        return;

    if(_f_ocelot_trace(d->a == 1 && d->b == 2, _f_ocelot_and(_f_ocelot_eq_numeric(d->a, 1), _f_ocelot_eq_numeric(d->b, 2)), _f_ocelot_or(_f_ocelot_neq_numeric(d->a, 1), _f_ocelot_neq_numeric(d->b, 2)))){
        return;
    }else{
        return;
    }
}
