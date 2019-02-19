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

int sizeCallList(CallList *list) {
    int size = 0;

    Call *actualCall = list->head;

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

void addCall(CallList *list, Call *call) {
    if (list->head == NULL) {
        list->head = call;
        return;
    }
    
    Call *currentCall = list->head;

    while (currentCall->next != NULL) {
        currentCall = currentCall->next;
    }

    currentCall->next = call;
}

Call* getCall(CallList *list, int index) {
    if (list == NULL) {
        return NULL;
    }

    Call *callToReturn = list->head;;
    int i = 0;

    while (callToReturn != NULL && i++ < index) {
        callToReturn = callToReturn->next;
    }

    return callToReturn;
}

void removeCall(CallList *list, int index) {
    if (list == NULL || list->head == NULL) {
        return;
    }

    //Remove first element
    if (index == 0) {
        list->head = list->head->next;
        
        return;
    }

    Call *actualCall = list->head->next;
    Call *previousCall = list->head;

    int i = 0;
    while (actualCall != NULL && i < index) {
        previousCall = actualCall;
        actualCall = actualCall->next;
        i++;
    }

    if (actualCall == NULL && i < index)
        return;

    previousCall->next = actualCall->next;
    
    printf("Size after remove: %d\n", sizeCallList(list));
}