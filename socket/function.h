#include "graph.h"
#include "main.h"

#define ARRAY_LENGTH 3
#define DEEP_STRUCT 2

typedef struct {
    Data *d;
    char **str;
} FunctionParameters;

FunctionParameters extractParametersFromGraph(Graph graph);
void generatePointerMap (FunctionParameters parameters);
Event* executeFunction(FunctionParameters functionParameters, int *size);


Data extractParameter_Data (Graph graph, Node variableNode);
Data* extractParameter_DataS (Graph graph, Node variableNode);
int extractParameter_int (Graph graph, Node variableNode);
int* extractParameter_intS (Graph graph, Node variableNode);
char extractParameter_char (Graph graph, Node variableNode);
char* extractParameter_charS (Graph graph, Node variableNode);
char** extractParameter_charSS (Graph graph, Node variableNode);

void freeParameters (FunctionParameters functionParameters);

double absValue(double value);