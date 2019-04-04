#include "function.h"

FunctionParameters extractParametersFromGraph(Graph graph) {
    FunctionParameters functionParameters;
    Node* variableNodes = children(graph, graph.nodes[0]);
    
    functionParameters.s = extractParameter_strbuf_tS(graph, variableNodes[0]);
    functionParameters.len = extractParameter_int(graph, variableNodes[1]);
    functionParameters.fmt = extractParameter_charS(graph, variableNodes[2]);

    free(variableNodes);
    
    return functionParameters;
}

void generatePointerMap (FunctionParameters parameters) {
    addPointerMap(pointerList, parameters.s, 0);
    addPointerMap(pointerList, parameters.s->buf, 1);
    addPointerMap(pointerList, parameters.fmt, 2);
}

Event* executeFunction(FunctionParameters functionParameters, int *size) {
    _f_ocelot_init();
    OCELOT_TESTFUNCTION(functionParameters.s, functionParameters.len, functionParameters.fmt);
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

strbuf_t extractParameter_strbuf_t (Graph graph, Node variableNode) {
    Node *structVariables = children(graph, variableNode);
    int numberOfVariables = size(graph, variableNode);
    int i = 0;
    
    strbuf_t val;
    
    val.buf = extractParameter_charS(graph, structVariables[i++]);
    val.size = extractParameter_int(graph, structVariables[i++]);
    val.length = extractParameter_int(graph, structVariables[i++]);
    val.increment = extractParameter_int(graph, structVariables[i++]);
    val.dynamic = extractParameter_int(graph, structVariables[i++]);
    val.reallocs = extractParameter_int(graph, structVariables[i++]);
    val.debug = extractParameter_int(graph, structVariables[i++]);
    free(structVariables);
    
    return val;
}

strbuf_t* extractParameter_strbuf_tS (Graph graph, Node variableNode) {
    int size = 0;
    int *index = getIndexOfVariableNode(graph, variableNode, &size);
    
    int i = 0;
    
    strbuf_t *val = malloc(sizeof(strbuf_t) * ARRAY_LENGTH);
    val[0] = extractParameter_strbuf_t(graph, graph.nodes[index[i++]]);
    val[1] = extractParameter_strbuf_t(graph, graph.nodes[index[i++]]);
    val[2] = extractParameter_strbuf_t(graph, graph.nodes[index[i++]]);
    
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

int extractParameter_int (Graph graph, Node variableNode) {
    int val = (int)variableNode.value;
    
    return val;
}


void freeParameters (FunctionParameters functionParameters) {
    free(functionParameters.s);
    free(functionParameters.fmt);
}

double absValue(double value) {
    if (value < 0)
        return value * (-1);
    
    return value;
}