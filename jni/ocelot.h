#include <glib.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>

#ifndef _Included_OcelotHeader
#define _Included_OcelotHeader
#ifdef __cplusplus
extern "C" {
#endif

#define OCELOT_K 0.5

typedef struct {
	int choice;
	double distanceTrue;
	double distanceFalse;
} _T_ocelot_event;

void _f_ocelot_init();
/*
 * Traces the branches
 */
int _f_ocelot_trace(int result, double distanceTrue, double distanceFalse);

/*
 * Functions that register in _v_ocelot_fcalls the result of each function call in the original expression.
 */
double _f_ocelot_reg_fcall_numeric(double fcall);
double _f_ocelot_reg_fcall_pointer(void* fcall);

/*
 * Returns one of the
 */
double _f_ocelot_get_fcall();

double _f_ocelot_eq_numeric(double,double);
double _f_ocelot_eq_pointer(void*, void*);

double _f_ocelot_gt_numeric(double,double);
double _f_ocelot_gt_pointer(void*, void*);

double _f_ocelot_ge_numeric(double,double);
double _f_ocelot_ge_pointer(void*, void*);

double _f_ocelot_neq_numeric(double,double);
double _f_ocelot_neq_pointer(void*, void*);

double _f_ocelot_and(double, double);
double _f_ocelot_or(double, double);

double _f_ocelot_istrue(double);

GArray *_v_ocelot_events;
GArray *_v_ocelot_fcalls;

#ifdef __cplusplus
}
#endif
#endif
