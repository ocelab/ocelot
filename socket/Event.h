#include <stddef.h>
#include <stdlib.h>

typedef struct {
    int isAnEventCase;
    int kind;
    int choice;
    
    //Event variable
    double distanceTrue;
    double distanceFalse;
    
    //EventCase variable
    double distance;
    double chosen;
    
    struct Event *next;
} Event;

typedef struct {
    double value;
    struct Call *next;
} Call;

typedef struct {
    Call *head;
} CallList;

int sizeEventList(Event* events);
int sizeCallList(CallList *list);

void addEvent(Event* events, Event* event);
Event* getEvent(Event* eventList, int index);
Event* removeFirstEventElement(Event *events);

void addCall(CallList *list, Call* call);
Call* getCall(CallList *list, int index);
void removeCall(CallList *list, int index);