void _f_ocelot_init() {
	_v_ocelot_events = g_new_array(FALSE, FALSE, sizeof(_T_ocelot_event))
}

int _ocelot_trace(int nodeId, int result, double distance) {
	_T_ocelot_event event;

	event.node = nodeId;
	event.choise = result;
	event.distance = distance;

	g_array_append_val(_v_ocelot_events, event);

	return result;
}

double _f_ocelot_eq_int(int exprId, int op1, int op2);
double _f_ocelot_eq_float(int exprId, float op1, float op2);
double _f_ocelot_eq_double(int exprId, double op1, double op2);
double _f_ocelot_eq_pointer(int exprId, void* op1, void* op2);
