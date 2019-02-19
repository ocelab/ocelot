#include "function.h"

FunctionParameters extractParametersFromGraph(Graph graph) {
    FunctionParameters functionParameters;
    Node* variableNodes = children(graph, graph.nodes[0]);
    
    functionParameters.str = extractParameter_charSS(graph, variableNodes[0]);

    free(variableNodes);
    
    return functionParameters;
}

Event* executeFunction(FunctionParameters functionParameters, int *size) {
    _f_ocelot_init();
    OCELOT_TESTFUNCTION(functionParameters.str);
    events = removeFirstEventElement(events);
    
    int eventSize = sizeEventList(events);
    Event *eventToReturn = malloc(sizeof(Event) * eventSize);
    Event *actualEvent = events;
    
    int i = 0;
    while (actualEvent != NULL) {
        eventToReturn[i].isAnEventCase = actualEvent->isAnEventCase;
        eventToReturn[i].kind = actualEvent->kind;
        eventToReturn[i].choice = actualEvent->choice;
        eventToReturn[i].distanceTrue = actualEvent->distanceTrue;
        eventToReturn[i].distanceFalse = actualEvent->distanceFalse;
        eventToReturn[i].distance = actualEvent->distance;
        eventToReturn[i].chosen = actualEvent->chosen;
        eventToReturn[i].next = NULL;
        
        actualEvent = actualEvent->next;
        i++;
    }
    
    _f_ocelot_end();
    free(actualEvent);
    
    *size = eventSize;
    
    return eventToReturn;
}

char extractParameter_char (Graph graph, Node variableNode) {
    char val = (char)(((int)absValue(variableNode.value) % 58) + 65);
    
    return val;
}

char* extractParameter_charS (Graph graph, Node variableNode) {
    int size = 0;
    int *index = getIndexOfVariableNode(graph, variableNode, &size);
    
    int i = 0;
    
    char *val = malloc(sizeof(char) * ARRAY_LENGTH);
    val[0] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[1] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[2] = extractParameter_char(graph, graph.nodes[index[i++]]);
    
    free(index);
    
    return val;
}

char** extractParameter_charSS (Graph graph, Node variableNode) {
    int size = 0;
    int *index = getIndexOfVariableNode(graph, variableNode, &size);
    
    int i = 0;
    
    char **val = malloc(sizeof(char*) * ARRAY_LENGTH);
    val[0] = malloc(sizeof(char) * ARRAY_LENGTH);
    val[1] = malloc(sizeof(char) * ARRAY_LENGTH);
    val[2] = malloc(sizeof(char) * ARRAY_LENGTH);
    val[0][0] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[0][1] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[0][2] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[1][0] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[1][1] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[1][2] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[2][0] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[2][1] = extractParameter_char(graph, graph.nodes[index[i++]]);
    val[2][2] = extractParameter_char(graph, graph.nodes[index[i++]]);
    
    free(index);
    
    return val;
}


void freeParameters (FunctionParameters functionParameters) {
    free(functionParameters.str);
}

double absValue(double value) {
    if (value < 0)
        return value * (-1);
    
    return value;
}