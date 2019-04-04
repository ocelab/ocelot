#include "ocelot.h"
#include <stdio.h>
#include <math.h>
typedef struct 
{
    char *buf;
    int size;
    int length;
    int increment;
    int dynamic;
    int reallocs;
    int debug;
} strbuf_t;

#define OCELOT_TESTFUNCTION strbuf_append_fmt
