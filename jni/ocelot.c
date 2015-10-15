#include "ocelot.h"
#include <math.h>

void _f_ocelot_init() {
	_v_ocelot_events = OCLIST_ALLOC(_T_ocelot_event);
	_v_ocelot_fcalls = OCLIST_ALLOC(double);
}

void _f_ocelot_end() {
	OCLIST_FREE(_v_ocelot_events);
	OCLIST_FREE(_v_ocelot_fcalls);

	free(_v_ocelot_pointers);
}

int _f_ocelot_trace(int result, double distanceTrue, double distanceFalse) {
	_T_ocelot_event event;
	event.kind = OCELOT_KIND_STDEV;
	event.choice = (result == 0 ? 0 : 1);

	event.distanceTrue = distanceTrue;
	event.distanceFalse = distanceFalse;
	OCLIST_APPEND(_v_ocelot_events, event);

	return result;
}

int _f_ocelot_trace_case(int branch, double distanceTrue, int isChosen) {
	_T_ocelot_event_case event;
	event.kind = OCELOT_KIND_CASEV;
	event.choice = branch;
	event.distance = distanceTrue;
	event.chosen = (double)isChosen;

	OCLIST_APPEND(_v_ocelot_events, event);

	return 0;
}

double _f_ocelot_reg_fcall_numeric(double fcall) {
	printf("Function call numeric");
	OCLIST_APPEND(_v_ocelot_fcalls, fcall);
	return fcall;
}

double _f_ocelot_reg_fcall_pointer(void* fcall) {
	printf("Function call pointer");
	OCLIST_APPEND(_v_ocelot_fcalls, *(double*)fcall);

	return (double)*(double*)fcall;
}

double _f_ocelot_get_fcall() {
	if (_v_ocelot_fcalls->size != 0) {
		double element = OCLIST_GET(_v_ocelot_fcalls, 0, double);
		OCLIST_SHIFT(_v_ocelot_fcalls);
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

double _f_ocelot_lt_numeric(double op1, double op2) {
	return _f_ocelot_ge_numeric(op2, op1);
}

double _f_ocelot_le_numeric(double op1, double op2) {
	return _f_ocelot_gt_numeric(op2, op1);
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
	int pos1 = _f_ocelot_pointertotab(op1);
	int pos2 = _f_ocelot_pointertotab(op2);
	return _f_ocelot_eq_numeric(pos1, pos2);
}
double _f_ocelot_gt_pointer(void* op1, void* op2) {
	int pos1 = _f_ocelot_pointertotab(op1);
	int pos2 = _f_ocelot_pointertotab(op2);

	return _f_ocelot_gt_numeric(pos1, pos2);
}
double _f_ocelot_ge_pointer(void* op1, void* op2) {
	int pos1 = _f_ocelot_pointertotab(op1);
	int pos2 = _f_ocelot_pointertotab(op2);

	return _f_ocelot_ge_numeric(pos1, pos2);
}
double _f_ocelot_lt_pointer(void* op1, void* op2) {
	return _f_ocelot_ge_pointer(op2, op1);
}
double _f_ocelot_le_pointer(void* op1, void* op2) {
	return _f_ocelot_gt_pointer(op2, op1);
}
double _f_ocelot_neq_pointer(void* op1, void* op2) {
	int pos1 = _f_ocelot_pointertotab(op1);
	int pos2 = _f_ocelot_pointertotab(op2);

	return _f_ocelot_neq_numeric(pos1, pos2);
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

int _f_ocelot_pointertotab(void* ptr) {
	int result = (ptr - (void*)_v_ocelot_pointers) / sizeof(_t_ocelot_array);
	return result;
}
