#include <stdio.h>

#include "Event.h"

int sizeEventList(Event *events) {
    int size = 0;

    Event *actualEvent = events;

    while (actualEvent != NULL) {
        actualEvent = actualEvent->next;

        size++;
    }

    return size;
}

int sizeCallList(Call* calls) {
    int size = 0;

    Call *actualCall = calls;

    while (actualCall != NULL) {
        actualCall = actualCall->next;
        size++;
    }

    return size;
}

void addEvent(Event *events, Event* event) {
    if (events == NULL) {
        events = event;
        return;
    }

    Event *actualEvent = events;

    while (actualEvent->next != NULL) {
        actualEvent = actualEvent->next;
    }

    actualEvent->next = event;
}

Event* getEvent(Event *events, int index) {
    if (events == NULL) {
        return NULL;
    }

    Event *actualEvent = events;

    int i = 0;
    while (actualEvent != NULL && i++ < index) {
        actualEvent = actualEvent->next;
    }

    return actualEvent;
}

Event* removeFirstEventElement(Event *events) {
    if (events == NULL) {
        return;
    }

    events = events->next;
    return;
}

void addCall(Call *calls, Call * call) {
    if (calls == NULL) {
        calls = call;
        return;
    }

    Call *nextCall = calls->next;

    while (nextCall != NULL) {
        nextCall = nextCall->next;
    }

    nextCall = call;
}

Call* getCall(Call *calls, int index) {
    if (calls == NULL) {
        return NULL;
    }

    Call *callToReturn = calls;
    int i = 0;

    while (callToReturn != NULL && i++ < index) {
        callToReturn = callToReturn->next;
    }

    return callToReturn;
}

void removeCall(Call* calls, int index) {
    if (calls == NULL) {
        return;
    }

    //Remove first element
    if (index == 0) {
        calls = calls->next;
        return;
    }

    Call *actualCall = calls;
    Call *previousCall = NULL;

    int i = 0;
    while (actualCall != NULL && i < index) {
        previousCall = actualCall;
        actualCall = actualCall->next;

        i++;
    }

    if (actualCall == NULL && i < index)
        return;

    previousCall->next = actualCall->next;
}