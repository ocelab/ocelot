#include "ocelot.h"
#include <math.h>

void _f_ocelot_init() {
    events = malloc(sizeof(Event));
    events->next = NULL;
    
    callList = malloc(sizeof(CallList));
    callList->head = NULL;
}

void _f_ocelot_end() {
    free(events);
    events = NULL;
    
    free(callList);
    callList = NULL;
}

int _f_ocelot_trace(int result, double distanceTrue, double distanceFalse) {
    Event *event = malloc(sizeof(Event));
    
    event->isAnEventCase = 0;
    event->kind = OCELOT_KIND_STDEV;
    event->choice = (result == 0 ? 0 : 1);
    
    event->distanceTrue = distanceTrue;
    event->distanceFalse = distanceFalse;
    event->next = NULL;
    
    addEvent(events, event);
    return result;
}

int _f_ocelot_trace_case(int branch, double distanceTrue, int isChosen) {
    Event *eventCase = malloc(sizeof(Event));
    eventCase->isAnEventCase = 1;
    eventCase->kind = OCELOT_KIND_CASEV;
    eventCase->choice = branch;
    
    eventCase->distance = distanceTrue;
    eventCase->chosen = (double) isChosen;
    eventCase->next = NULL;
    addEvent(events, eventCase);

    return 0;
}

double _f_ocelot_reg_fcall_numeric(double fcall, int howMany) {
    int i;
    for (i = 0; i < howMany; i++) {
        Call *call = malloc(sizeof(Call));
        call->value = fcall;
        call->next = NULL;
        
        addCall(callList, call);
    }
    
    //fprintf(stderr, "\n\nAllocated. Size = %d",OCLIST_SIZE(_v_ocelot_fcalls));
    return 1;
}

double _f_ocelot_reg_fcall_pointer(void* fcall, int howMany) {
    int i;
    for (i = 0; i < howMany; i++) {
        Call *call = malloc(sizeof(Call));
        call->value = *(double*)fcall;
        call->next = NULL;
        addCall(callList, call);
    }
    
    return (double) *(double*) fcall;
}

double _f_ocelot_get_fcall() {
    if (sizeCallList(callList) != 0) {
        Call *call = getCall(callList, 0);
        double element = call->value;
        removeCall(callList, 0);
        return element;
    } else {
        fprintf(stderr, "Empty function queue!\n");
        return 0;
    }
}

double _f_ocelot_eq_numeric(double op1, double op2) {
    double k = ABS(op1 - op2);
    double result;
    if (k == 0.0)
        result = 0.0;
    else
        result = k + OCELOT_K;

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
    return op1 + op2;
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
    //int result = (ptr - (void*) _v_ocelot_pointers) / sizeof (_t_ocelot_array);
    return 0;
}
