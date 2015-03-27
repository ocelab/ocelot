#include "ocelot.h"

void _f_ocelot_init() {
	_v_ocelot_events = g_array_new(FALSE, FALSE, sizeof(_T_ocelot_event));
	_v_ocelot_fcalls = g_array_new(FALSE, FALSE, sizeof(double));
}

int _f_ocelot_trace(int result, double distanceTrue, double distanceFalse) {
	_T_ocelot_event event;

	event.choice = result;
	event.distanceTrue = distanceTrue;
	event.distanceFalse = distanceFalse;
	g_array_append_val(_v_ocelot_events, event);

	return result;
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
	return op2-op1;
}
double _f_ocelot_eq_pointer(void* op1, void* op2) {
	return op2-op1;
}

double _f_ocelot_gt_numeric(double op1, double op2) {
	printf("%f - %f ", op1, op2);
	return op2-op1;
}
double _f_ocelot_gt_pointer(void* op1, void* op2) {
	return op2-op1;
}

double _f_ocelot_ge_numeric(double op1, double op2) {
	return op2-op1;
}
double _f_ocelot_ge_pointer(void* op1, void* op2) {
	return op2-op1;
}

double _f_ocelot_neq_numeric(double op1, double op2) {
	return op2-op1;
}
double _f_ocelot_neq_pointer(void* op1, void* op2) {
	return op2-op1;
}

double _f_ocelot_and(double op1, double op2) {
	return op2+op1;
}
double _f_ocelot_or(double op1, double op2) {
	return op2+op1;
}
