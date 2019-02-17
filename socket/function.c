#include "function.h"

FunctionParameters extractParametersFromGraph(Graph graph) {
    FunctionParameters functionParameters;
    Node* variableNodes = children(graph, graph.nodes[0]);
    
    functionParameters.d = extractParameter_DataS(graph, variableNodes[0]);
    functionParameters.str = extractParameter_charSS(graph, variableNodes[1]);

    free(variableNodes);
    
    return functionParameters;
}

Event* executeFunction(FunctionParameters functionParameters, int *size) {
    _f_ocelot_init();
    OCELOT_TESTFUNCTION(functionParameters.d, functionParameters.str);
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

Data extractParameter_Data (Graph graph, Node variableNode) {
    Node *structVariables = children(graph, variableNode);
    int numberOfVariables = size(graph, variableNode);
    int i = 0;
    
    Data val;
    
    val.internal = extractParameter_intS(graph, structVariables[i++]);
    val.a = extractParameter_int(graph, structVariables[i++]);
    val.b = extractParameter_int(graph, structVariables[i++]);
    if (i < numberOfVariables) {
        val.next = extractParameter_DataS(graph, structVariables[i++]);
    }

    free(structVariables);
    
    return val;
}

Data* extractParameter_DataS (Graph graph, Node variableNode) {
    int size = 0;
    int *index = getIndexOfVariableNode(graph, variableNode, &size);
    
    int i = 0;
    
    Data *val = malloc(sizeof(Data) * ARRAY_LENGTH);
    val[0] = extractParameter_Data(graph, graph.nodes[index[i++]]);
    val[1] = extractParameter_Data(graph, graph.nodes[index[i++]]);
    val[2] = extractParameter_Data(graph, graph.nodes[index[i++]]);
    
    free(index);
    
    return val;
}

int extractParameter_int (Graph graph, Node variableNode) {
    int val = (int)variableNode.value;
    
    return val;
}

int* extractParameter_intS (Graph graph, Node variableNode) {
    int size = 0;
    int *index = getIndexOfVariableNode(graph, variableNode, &size);
    
    int i = 0;
    
    int *val = malloc(sizeof(int) * ARRAY_LENGTH);
    val[0] = extractParameter_int(graph, graph.nodes[index[i++]]);
    val[1] = extractParameter_int(graph, graph.nodes[index[i++]]);
    val[2] = extractParameter_int(graph, graph.nodes[index[i++]]);
    
    free(index);
    
    return val;
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

double absValue(double value) {
    if (value < 0)
        return value * (-1);
    
    return value;
}