#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>
#include "Event.h"
#include "PointerMap.h"

#ifndef _Included_OcelotHeader
#define _Included_OcelotHeader
#ifdef __cplusplus
extern "C" {
#endif

#define OCELOT_K 0.5
#define OCELOT_KIND_STDEV 1
#define OCELOT_KIND_CASEV 2

#ifndef ABS
#define ABS(x) (x < 0 ? -x : x)
#endif

#define OCELOD_DEREF(type, variable) ((type)*((double*)variable))


Event *events;
CallList *callList;
PointerMapList *pointerList;

void _f_ocelot_init();
void _f_ocelot_end();

/*
 * Traces the branches
 */
int _f_ocelot_trace(int,double,double);
int _f_ocelot_trace_case(int,double,int);

/*
 * Functions that register in _v_ocelot_fcalls the result of each function call in the original expression.
 */
double _f_ocelot_reg_fcall_numeric(double fcall, int howMany);
double _f_ocelot_reg_fcall_pointer(void* fcall, int howMany);

/*
 * Returns one of the
 */
double _f_ocelot_get_fcall();

int _f_ocelot_pointertotab(void*);

double _f_ocelot_eq_numeric(double,double);
double _f_ocelot_eq_pointer(void*, void*);

double _f_ocelot_gt_numeric(double,double);
double _f_ocelot_gt_pointer(void*, void*);

double _f_ocelot_ge_numeric(double,double);
double _f_ocelot_ge_pointer(void*, void*);

double _f_ocelot_lt_numeric(double,double);
double _f_ocelot_lt_pointer(void*, void*);

double _f_ocelot_le_numeric(double,double);
double _f_ocelot_le_pointer(void*, void*);

double _f_ocelot_neq_numeric(double,double);
double _f_ocelot_neq_pointer(void*, void*);

double _f_ocelot_and(double, double);
double _f_ocelot_or(double, double);

double _f_ocelot_istrue(double);
double _f_ocelot_isfalse(double);


#ifdef __cplusplus
}
#endif
#endif
