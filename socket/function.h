#include "graph.h"
#include "main.h"

#define ARRAY_LENGTH 3
#define DEEP_STRUCT 2

typedef struct {
    char **str;
} FunctionParameters;

FunctionParameters extractParametersFromGraph(Graph graph);
Event* executeFunction(FunctionParameters functionParameters, int *size);


char extractParameter_char (Graph graph, Node variableNode);
char* extractParameter_charS (Graph graph, Node variableNode);
char** extractParameter_charSS (Graph graph, Node variableNode);

void freeParameters (FunctionParameters functionParameters);

double absValue(double value);