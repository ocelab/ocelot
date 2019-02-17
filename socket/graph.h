#include <stddef.h>

#define INTEGER_TYPE 0
#define DOUBLE_TYPE 1
#define CHAR_TYPE 2
#define POINTER_TYPE 3
#define STRUCT_TYPE 4

#define OUT_OF_RANGE -100001

typedef struct Node{
    int index;
    double value;
    int type;
} Node;

typedef struct Edge{
    Node nodeFrom;
    Node nodeTo;
} Edge;

typedef struct Graph{
    Node *nodes;
    int sizeNodes;
    Edge *edges;
    int sizeEdges;
} Graph;


int size (Graph graph, Node parent);
Node* children (Graph graph, Node parent);
int* getIndexOfVariableNode (Graph graph, Node node, int *numberOfNode);
void printGraph(Graph graph);