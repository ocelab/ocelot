#include "graph.h"
#include "main.h"

#define ARRAY_LENGTH 3
#define DEEP_STRUCT 2

typedef struct {
    charac *p1;
    charac **pp2;
    Node **node_ptr;
} FunctionParameters;

FunctionParameters extractParametersFromGraph(Graph graph);
void generatePointerMap (FunctionParameters parameters);
Event* executeFunction(FunctionParameters functionParameters, int *size);


charac extractParameter_charac (Graph graph, Node variableNode);
charac* extractParameter_characS (Graph graph, Node variableNode);
charac** extractParameter_characSS (Graph graph, Node variableNode);
char extractParameter_char (Graph graph, Node variableNode);
int extractParameter_int (Graph graph, Node variableNode);
Node extractParameter_Node (Graph graph, Node variableNode);
Node* extractParameter_NodeS (Graph graph, Node variableNode);
Node** extractParameter_NodeSS (Graph graph, Node variableNode);
double extractParameter_double (Graph graph, Node variableNode);

void freeParameters (FunctionParameters functionParameters);

double absValue(double value);