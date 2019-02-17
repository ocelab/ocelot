#include <stdlib.h>
#include <stdio.h>
#include "graph.h"

int size (Graph graph, Node parent) {
    if (parent.index >= graph.sizeNodes)
        return -1;

    int size = 0;
    
    //Search incident
    int index = 0;
    for (int i = 0; i < graph.sizeEdges; i++) {
        if ((graph.edges[i].nodeFrom.index == parent.index &&
                graph.edges[i].nodeTo.index > parent.index) || 
                (graph.edges[i].nodeTo.index == parent.index && 
                graph.edges[i].nodeFrom.index > parent.index)){
            size++;
        }
    }
    
    return size;
}

Node* children(Graph graph, Node parent) {
    //printGraph2(*graph);
    if (parent.index >= graph.sizeNodes) {
        return NULL;
    }
        
    int sizeOfChildren = size(graph, parent);
    
    Node *children = malloc(sizeof (Node) * sizeOfChildren);

    //Search incident
    int index = 0;
    for (int i = 0; i < graph.sizeEdges; i++) {
        if (graph.edges[i].nodeFrom.index == parent.index &&
                graph.edges[i].nodeTo.index > parent.index){
            children[index++] = graph.nodes[graph.edges[i].nodeTo.index];
        }
        
        else if (graph.edges[i].nodeTo.index == parent.index && 
                graph.edges[i].nodeFrom.index > parent.index) {
            children[index++] = graph.nodes[graph.edges[i].nodeFrom.index];
        }
    }
    
    return children;
}

int* getIndexOfVariableNode (Graph graph, Node node, int *numberOfNodes) {
    int *indexOfVariableNodes = malloc(sizeof(int));
    
    if (node.type == POINTER_TYPE) {
        Node *childrenList = children(graph, node);
        int numberOfChildren = size(graph, node);
        
        for (int i = 0; i < numberOfChildren; i++) {
            int pointerNumberOfNodes = 0;
            
            //Get the index of pointer's subtree
            int *pointerIndexNodes = getIndexOfVariableNode(graph, childrenList[i], &pointerNumberOfNodes);
            
            //Realloc array's memory, in order to put new indexes take from actual pointer
            indexOfVariableNodes = realloc(indexOfVariableNodes, (sizeof(int) * (*numberOfNodes + pointerNumberOfNodes)));
            
            for (int i = 0; i < pointerNumberOfNodes; i++) {
                indexOfVariableNodes[*numberOfNodes + i] = pointerIndexNodes[i];
            }
            
            free(pointerIndexNodes);
            *numberOfNodes = *numberOfNodes + pointerNumberOfNodes;
        }
    } else {
        *indexOfVariableNodes = node.index;
        *numberOfNodes = *numberOfNodes + 1;
        
        return indexOfVariableNodes;
    }
    
    return indexOfVariableNodes;
}

void printGraph(Graph graph) {
    printf("\tGraph info\n\n");

    printf("Node List\n\n");
    for (int i = 0; i < graph.sizeNodes; i++) {
        printf("Node %d\n", (graph.nodes[i].index));
        if (graph.nodes[i].value != OUT_OF_RANGE) {
            printf("\tValue: %f\n", graph.nodes[i].value);
        }

        printf("\tType: ");
        switch (graph.nodes[i].type) {
            case INTEGER_TYPE:
                printf("Integer\n");
                break;
            case DOUBLE_TYPE:
                printf("Double\n");
                break;
            case CHAR_TYPE:
                printf("Char\n");
                break;
            case POINTER_TYPE:
                printf("Pointer\n");
                break;
            case STRUCT_TYPE:
                printf("Struct\n");
                break;
            default:
                printf("Undefined\n");
                break;
        }
        printf("\n");
    }

    printf("\nEdge List\n\n");
    for (int i = 0; i < graph.sizeEdges; i++) {
        printf("Edge %d: from %d to %d\n", (i + 1), graph.edges[i].nodeFrom.index, graph.edges[i].nodeTo.index);
    }
}