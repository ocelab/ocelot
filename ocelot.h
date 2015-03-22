#include <glib.h>

typedef struct {
	int node;
	int choise;
	double distance;
} _T_ocelot_event;

/**
 * Traces the branches
 */
void _f_ocelot_init();
int _f_ocelot_trace(int nodeId, int result, double distance);

double _f_ocelot_eq_int(int exprId, int op1, int op2);
double _f_ocelot_eq_float(int exprId, float op1, float op2);
double _f_ocelot_eq_double(int exprId, double op1, double op2);
double _f_ocelot_eq_pointer(int exprId, void* op1, void* op2);

double _f_ocelot_gt_int(int exprId, int op1, int op2);
double _f_ocelot_gt_float(int exprId, float op1, float op2);
double _f_ocelot_gt_double(int exprId, double op1, double op2);
double _f_ocelot_gt_pointer(int exprId, void* op1, void* op2);

double _f_ocelot_ge_int(int exprId, int op1, int op2);
double _f_ocelot_ge_float(int exprId, float op1, float op2);
double _f_ocelot_ge_double(int exprId, double op1, double op2);
double _f_ocelot_ge_pointer(int exprId, void* op1, void* op2);

double _f_ocelot_gt_int(int exprId, int op1, int op2);
double _f_ocelot_gt_float(int exprId, float op1, float op2);
double _f_ocelot_gt_double(int exprId, double op1, double op2);
double _f_ocelot_gt_pointer(int exprId, void* op1, void* op2);

GArray *_v_ocelot_events
