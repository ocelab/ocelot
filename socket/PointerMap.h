
#include <stdlib.h>

typedef struct {
    void *pointer;
    int value;
    struct PointerMap *next;
} PointerMap;

typedef struct {
    PointerMap *head;
} PointerMapList;

void initPointerList (PointerMapList* pointerListMap);
void addPointerMap (PointerMapList* pointerListMap, void *pointer, int value);
int getValueOfPointer(PointerMapList* pointerListMap, void *pointer);

