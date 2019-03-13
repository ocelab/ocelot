#include "function.h"

FunctionParameters extractParametersFromGraph(Graph graph) {
    FunctionParameters functionParameters;
    Node* variableNodes = children(graph, graph.nodes[0]);
    
    functionParameters.p1 = extractParameter_characS(graph, variableNodes[0]);
    functionParameters.pp2 = extractParameter_characSS(graph, variableNodes[1]);
    functionParameters.node_ptr = extractParameter_NodeSS(graph, variableNodes[2]);

    free(variableNodes);
    
    return functionParameters;
}

void generatePointerMap (FunctionParameters parameters) {
    addPointerMap(pointerList, parameters.p1, 0);
    addPointerMap(pointerList, parameters.p1->PREV, 1);
    addPointerMap(pointerList, parameters.p1->NEXT, 2);
    addPointerMap(pointerList, parameters.pp2, 3);
    addPointerMap(pointerList, parameters.node_ptr, 4);
}

Event* executeFunction(FunctionParameters functionParameters, int *size) {
    _f_ocelot_init();
    OCELOT_TESTFUNCTION(functionParameters.p1, functionParameters.pp2, functionParameters.node_ptr);
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

charac extractParameter_charac (Graph graph, Node variableNode) {
    Node *structVariables = children(graph, variableNode);
    int numberOfVariables = size(graph, variableNode);
    int i = 0;
    
    charac val;
    
    val.info = extractParameter_char(graph, structVariables[i++]);
    val.LINE_NUM = extractParameter_int(graph, structVariables[i++]);
    if (i < numberOfVariables) {
        val.PREV = extractParameter_characS(graph, structVariables[i++]);
    if (i < numberOfVariables) {
        val.NEXT = extractParameter_characS(graph, structVariables[i++]);
    }

    free(structVariables);
    
    return val;
}

charac* extractParameter_characS (Graph graph, Node variableNode) {
    int size = 0;
    int *index = getIndexOfVariableNode(graph, variableNode, &size);
    
    int i = 0;
    
    charac *val = malloc(sizeof(charac) * ARRAY_LENGTH);
    val[0] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[1] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[2] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    
    free(index);
    
    return val;
}

charac** extractParameter_characSS (Graph graph, Node variableNode) {
    int size = 0;
    int *index = getIndexOfVariableNode(graph, variableNode, &size);
    
    int i = 0;
    
    charac **val = malloc(sizeof(charac*) * ARRAY_LENGTH);
    val[0] = malloc(sizeof(charac) * ARRAY_LENGTH);
    val[1] = malloc(sizeof(charac) * ARRAY_LENGTH);
    val[2] = malloc(sizeof(charac) * ARRAY_LENGTH);
    val[0][0] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[0][1] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[0][2] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[1][0] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[1][1] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[1][2] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[2][0] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[2][1] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    val[2][2] = extractParameter_charac(graph, graph.nodes[index[i++]]);
    
    free(index);
    
    return val;
}

char extractParameter_char (Graph graph, Node variableNode) {
    char val = (char)(((int)absValue(variableNode.value) % 58) + 65);
    
    return val;
}

int extractParameter_int (Graph graph, Node variableNode) {
    int val = (int)variableNode.value;
    
    return val;
}

Node extractParameter_Node (Graph graph, Node variableNode) {
    Node *structVariables = children(graph, variableNode);
    int numberOfVariables = size(graph, variableNode);
    int i = 0;
    
    Node val;
    
    val.OMIT_ORIENT = extractParameter_int(graph, structVariables[i++]);
    val.PCOORD = extractParameter_int(graph, structVariables[i++]);
    val.QCOORD = extractParameter_int(graph, structVariables[i++]);
    val.THEA = extractParameter_double(graph, structVariables[i++]);
    val.PHEA = extractParameter_double(graph, structVariables[i++]);
    val.PSEA = extractParameter_double(graph, structVariables[i++]);
    val.ANGLE_UNIT = extractParameter_int(graph, structVariables[i++]);
    if (i < numberOfVariables) {
        val.NEXT = extractParameter_NodeS(graph, structVariables[i++]);
    }

    free(structVariables);
    
    return val;
}

Node* extractParameter_NodeS (Graph graph, Node variableNode) {
    int size = 0;
    int *index = getIndexOfVariableNode(graph, variableNode, &size);
    
    int i = 0;
    
    Node *val = malloc(sizeof(Node) * ARRAY_LENGTH);
    val[0] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[1] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[2] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    
    free(index);
    
    return val;
}

Node** extractParameter_NodeSS (Graph graph, Node variableNode) {
    int size = 0;
    int *index = getIndexOfVariableNode(graph, variableNode, &size);
    
    int i = 0;
    
    Node **val = malloc(sizeof(Node*) * ARRAY_LENGTH);
    val[0] = malloc(sizeof(Node) * ARRAY_LENGTH);
    val[1] = malloc(sizeof(Node) * ARRAY_LENGTH);
    val[2] = malloc(sizeof(Node) * ARRAY_LENGTH);
    val[0][0] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[0][1] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[0][2] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[1][0] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[1][1] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[1][2] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[2][0] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[2][1] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    val[2][2] = extractParameter_Node(graph, graph.nodes[index[i++]]);
    
    free(index);
    
    return val;
}

double extractParameter_double (Graph graph, Node variableNode) {
    double val = variableNode.value;
    
    return val;
}


void freeParameters (FunctionParameters functionParameters) {
    free(functionParameters.p1);
    free(functionParameters.pp2);
    free(functionParameters.node_ptr);
}

double absValue(double value) {
    if (value < 0)
        return value * (-1);
    
    return value;
}