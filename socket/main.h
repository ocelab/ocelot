#include "ocelot.h"
#include <stdio.h>
#include <math.h>
typedef struct _data
{
    int *internal;
    int a, b;
    struct _data *next;
} Data;

#define OCELOT_TESTFUNCTION case2
