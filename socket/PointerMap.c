#include <stdio.h>

#include "PointerMap.h"

void initPointerList (PointerMapList* pointerListMap) {
    pointerListMap = malloc(sizeof(PointerMapList));
    pointerListMap->head = NULL;
}

void addPointerMap (PointerMapList* pointerListMap, void *pointer, int value) {
    PointerMap *pointerToAdd = malloc(sizeof(PointerMap));
    pointerToAdd->pointer = pointer;
    pointerToAdd->value = value;
    pointerToAdd->next = NULL;
    
    if (pointerListMap == NULL) {
        initPointerList(pointerListMap);
    }
    
    if (pointerListMap->head == NULL) {
        pointerListMap->head = pointerToAdd;
        
        return;
    }
    
    PointerMap *actualPointerMap = pointerListMap->head;

    while (actualPointerMap->next != NULL) {
        actualPointerMap = actualPointerMap->next;
    }

    actualPointerMap->next = pointerToAdd;
}

int getValueOfPointer(PointerMapList* pointerListMap, void *pointer) {
    int value = -1;
    
    if (pointerListMap == NULL || pointerListMap->head == NULL) {
        return value;
    }

    PointerMap *actualPointerMap = pointerListMap->head;
    
    while (actualPointerMap->next != NULL && actualPointerMap->pointer != pointer) {
        actualPointerMap = actualPointerMap->next;
    }

    if (actualPointerMap->pointer == pointer) {
        value = actualPointerMap->value;
    }
    
    return value;
}