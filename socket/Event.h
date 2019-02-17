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

int sizeEventList(Event* events);
int sizeCallList(Call* calls);

void addEvent(Event* events, Event* event);
Event* getEvent(Event* eventList, int index);
Event* removeFirstEventElement(Event *events);

void addCall(Call* calls, Call* call);
Call* getCall(Call* calls, int index);
void removeCall(Call* calls, int index);