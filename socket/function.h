#include "graph.h"
#include "main.h"

#define ARRAY_LENGTH 3
#define DEEP_STRUCT 2

typedef struct {
    strbuf_t *s;
    int len;
    char *fmt;
} FunctionParameters;

FunctionParameters extractParametersFromGraph(Graph graph);
void generatePointerMap (FunctionParameters parameters);
Event* executeFunction(FunctionParameters functionParameters, int *size);


strbuf_t extractParameter_strbuf_t (Graph graph, Node variableNode);
strbuf_t* extractParameter_strbuf_tS (Graph graph, Node variableNode);
char extractParameter_char (Graph graph, Node variableNode);
char* extractParameter_charS (Graph graph, Node variableNode);
int extractParameter_int (Graph graph, Node variableNode);

void freeParameters (FunctionParameters functionParameters);

double absValue(double value);