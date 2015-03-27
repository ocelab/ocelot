#include <glib.h>
#include <stdbool.h>

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

double _f_ocelot_eq_numeric(double op1, double op2);
double _f_ocelot_eq_pointer(void* op1, void* op2);

double _f_ocelot_gt_numeric(double op1, double op2);
double _f_ocelot_gt_pointer(void* op1, void* op2);

double _f_ocelot_ge_numeric(double op1, double op2);
double _f_ocelot_ge_pointer(void* op1, void* op2);

double _f_ocelot_neq_numeric(double op1, double op2);
double _f_ocelot_neq_pointer(void* op1, void* op2);

double _f_ocelot_and(double op1, double op2);
double _f_ocelot_or(double op1, double op2);

double _f_ocelot_istrue(double op);

GArray *_v_ocelot_events;
GArray *_v_ocelot_fcalls;
