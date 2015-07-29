#include "ocelot.h"
#include <math.h>

void _f_ocelot_init() {
	_v_ocelot_events = g_array_new(FALSE, FALSE, sizeof(_T_ocelot_event));
	_v_ocelot_fcalls = g_array_new(FALSE, FALSE, sizeof(double));
}

void _f_ocelot_end() {
	g_array_free(_v_ocelot_events, TRUE);
	g_array_free(_v_ocelot_fcalls, TRUE);
}

int _f_ocelot_trace(int result, double distanceTrue, double distanceFalse) {
	_T_ocelot_event event;
	event.kind = OCELOT_KIND_STDEV;
	event.choice = (result == 0 ? 0 : 1);

	//Checks if distanceTrue if NaN
	//if (distanceTrue != distanceTrue)
	//	distanceTrue = INFINITY;

	//Checks if distanceFalse is NaN
	//if (distanceFalse != distanceFalse)
	//	distanceFalse = INFINITY;

	event.distanceTrue = distanceTrue;
	event.distanceFalse = distanceFalse;
	g_array_append_val(_v_ocelot_events, event);

	return result;
}

int _f_ocelot_trace_case(int branch, double distanceTrue, int isChosen) {
	_T_ocelot_event_case event;
	event.kind = OCELOT_KIND_CASEV;
	event.choice = branch;
	event.distance = distanceTrue;
	event.chosen = (double)isChosen;

	g_array_append_val(_v_ocelot_events, event);

	return 0;
}

double _f_ocelot_reg_fcall_numeric(double fcall) {
	printf("Function call numeric");
	g_array_append_val(_v_ocelot_fcalls, fcall);
	return fcall;
}

double _f_ocelot_reg_fcall_pointer(void* fcall) {
	printf("Function call pointer");
	g_array_append_val(_v_ocelot_fcalls, *fcall);

	return (double)*(double*)fcall;
}

double _f_ocelot_get_fcall() {
	if (_v_ocelot_fcalls->len != 0) {
		double element = g_array_index(_v_ocelot_fcalls, double, 0);
		g_array_remove_index(_v_ocelot_fcalls, 0);
		return element;
	} else {
		return 0;
	}
}

double _f_ocelot_eq_numeric(double op1, double op2) {
	double k = ABS(op1 - op2);
	double result;
	if (k == 0.0)
		result = 0.0;
	else
		result = k+OCELOT_K;

	return result;
}

double _f_ocelot_gt_numeric(double op1, double op2) {
	double result;
	if (op2 - op1 < 0.0) {
		result = 0.0;
	} else {
		result = (op2 - op1) + OCELOT_K;
	}
	return result;
}

double _f_ocelot_ge_numeric(double op1, double op2) {
	double result;
	if (op2 - op1 <= 0.0) {
		result = 0.0;
	} else {
		result = (op2 - op1) + OCELOT_K;
	}

	return result;
}

double _f_ocelot_neq_numeric(double op1, double op2) {
	double k = ABS(op1 - op2);
	double result;

	if (k != 0.0)
		result = 0;
	else
		result = OCELOT_K;

	return result;
}

double _f_ocelot_eq_pointer(void* op1, void* op2) {
	return 0;
}
double _f_ocelot_gt_pointer(void* op1, void* op2) {
	return 0;
}
double _f_ocelot_ge_pointer(void* op1, void* op2) {
	return 0;
}
double _f_ocelot_neq_pointer(void* op1, void* op2) {
	return 0;
}

double _f_ocelot_and(double op1, double op2) {
	return op1+op2;
}

double _f_ocelot_or(double op1, double op2) {
	if (op1 < op2)
			return op1;
		else
			return op2;
}

double _f_ocelot_istrue(double flag) {
	if (flag != 0.0)
		return 0;
	else
		return OCELOT_K;
}

double _f_ocelot_isfalse(double flag) {
	double k = ABS(flag);
	if (flag == 0.0)
		return 0;
	else if (flag == 1.0)
		return OCELOT_K;
	else
		return k;
}
