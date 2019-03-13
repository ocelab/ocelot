package it.unisa.ocelot.c.instrumentor;

import it.unisa.ocelot.c.types.*;
import it.unisa.ocelot.genetic.encoding.graph.*;
import it.unisa.ocelot.genetic.encoding.graph.StructNode;
import it.unisa.ocelot.genetic.encoding.manager.GraphManager;

import javax.xml.transform.sax.SAXSource;
import java.util.*;

public class CFunctionGenerator {
    private static int ARRAY_LENGTH = 3;

    private Graph graph;
    private GraphManager graphManager;

    public CFunctionGenerator (Graph pGraph) {
        this.graph = pGraph;
        this.graphManager = new GraphManager();
    }

    public String generateSourceCode () {
        String code = "";

        //Include
        code += "#include \"function.h\"\n\n";

        code += generateExtractParameterFromGraphMethod();
        code += "\n";

        code += generatePointerMap();
        code += "\n";

        code += generateExecuteFunctionMethod();
        code += "\n";

        LinkedHashMap<String, Integer> typeList = graphTypeList();
        for (Map.Entry<String, Integer> entry : graphTypeList().entrySet()) {
            String type = entry.getKey();
            int numberOfPointer = entry.getValue();

            for (int i = 0; i <= numberOfPointer; i++) {
                code += generateExtractParameterTypeMethod(type, i);
                code += "\n";
            }
        }

        code += "\n";

        code += generateFreeParametersMethod();

        code += "\n";

        code += "double absValue(double value) {\n" +
                "    if (value < 0)\n" +
                "        return value * (-1);\n" +
                "    \n" +
                "    return value;\n" +
                "}";


        return code;
    }

    public String generateHeaderFile () {
        String code = "";

        //Include
        code += "#include \"graph.h\"\n" +
                "#include \"main.h\"\n\n";

        //Define
        code += "#define ARRAY_LENGTH 3\n" +
                "#define DEEP_STRUCT 2\n\n";

        //Struct - Parameter
        code += "typedef struct {\n";

        ArrayList<Node> paramterNodes = graphManager.children(graph, graph.getNode(0));
        for (int i = 0; i < paramterNodes.size(); i++) {
            //Variable info
            String variableName = getName(paramterNodes.get(i).getCType());
            String typeName = getTypeName(paramterNodes.get(i).getCType());
            String pointerString = getPointerString(paramterNodes.get(i).getCType());
            String star = pointerString.replaceAll("S", "*");

            code += "    " + typeName + " " + star + variableName + ";\n";
        }

        code += "} FunctionParameters;\n\n";


        //Method
        code += "FunctionParameters extractParametersFromGraph(Graph graph);\n" +
                "void generatePointerMap (FunctionParameters parameters);\n" +
                "Event* executeFunction(FunctionParameters functionParameters, int *size);\n\n\n";

        LinkedHashMap<String, Integer> typeList = graphTypeList();
        for (Map.Entry<String, Integer> entry : graphTypeList().entrySet()) {
            String type = entry.getKey();
            int numberOfPointer = entry.getValue();

            for (int i = 0; i <= numberOfPointer; i++) {
                String pointerString = "";
                for (int j = 0; j < i; j++)
                    pointerString += "S";

                code += type + pointerString.replaceAll("S", "*") + " extractParameter_" + type + pointerString + " (Graph graph, Node variableNode);\n";
            }
        }

        code += "\n";

        code += "void freeParameters (FunctionParameters functionParameters);\n";

        code += "\ndouble absValue(double value);";

        return code;
    }

    private String generateExtractParameterFromGraphMethod () {
        String code = "FunctionParameters extractParametersFromGraph(Graph graph) {\n" +
                "    FunctionParameters functionParameters;\n" +
                "    Node* variableNodes = children(graph, graph.nodes[0]);\n" +
                "    \n";

        //Parameter section
        ArrayList<Node> parameterNodes = graphManager.children(graph, graph.getNode(0));
        for (int i = 0; i < parameterNodes.size(); i++) {
            //Variable info
            String variableName = getName(parameterNodes.get(i).getCType());
            String typeName = getTypeName(parameterNodes.get(i).getCType());
            String pointerString = getPointerString(parameterNodes.get(i).getCType());

            code += "    functionParameters." + variableName +
                    " = extractParameter_" + typeName + pointerString + "(graph, variableNodes[" + i + "]);\n";
        }

        code += "\n";

        code += "    free(variableNodes);\n" +
                "    \n" +
                "    return functionParameters;\n" +
                "}\n";

        return code;
    }

    private String generatePointerMap () {
        String code = "void generatePointerMap (FunctionParameters parameters) {\n";

        LinkedHashSet<String> pointerList = getPointerList(graph.getNode(0), "");

        int i = 0;
        for (String s : pointerList) {
            code += "    addPointerMap(pointerList, parameters." + s + ", " + i++ + ");\n";
        }

        code += "}\n";

        return code;
    }

    private String generateExecuteFunctionMethod () {
        String code = "Event* executeFunction(FunctionParameters functionParameters, int *size) {\n" +
                "    _f_ocelot_init();\n" +
                "    OCELOT_TESTFUNCTION(";

        //Parameter section
        ArrayList<Node> paramterNodes = graphManager.children(graph, graph.getNode(0));

        for (int i = 0; i < paramterNodes.size(); i++) {
            String variableName = getName(paramterNodes.get(i).getCType());
            code += "functionParameters." + variableName;

            if (i < (paramterNodes.size() - 1)) {
                code += ", ";
            } else {
                code += ");\n";
            }
        }

        code += "    events = removeFirstEventElement(events);\n" +
                "    \n" +
                "    int eventSize = sizeEventList(events);\n" +
                "    Event *eventToReturn = malloc(sizeof(Event) * eventSize);\n" +
                "    Event *actualEvent = events;\n" +
                "    \n" +
                "    int i = 0;\n" +
                "    while (actualEvent != NULL) {\n" +
                "        eventToReturn[i].isAnEventCase = actualEvent->isAnEventCase;\n" +
                "        eventToReturn[i].kind = actualEvent->kind;\n" +
                "        eventToReturn[i].choice = actualEvent->choice;\n" +
                "        eventToReturn[i].distanceTrue = actualEvent->distanceTrue;\n" +
                "        eventToReturn[i].distanceFalse = actualEvent->distanceFalse;\n" +
                "        eventToReturn[i].distance = actualEvent->distance;\n" +
                "        eventToReturn[i].chosen = actualEvent->chosen;\n" +
                "        eventToReturn[i].next = NULL;\n" +
                "        \n" +
                "        actualEvent = actualEvent->next;\n" +
                "        i++;\n" +
                "    }\n" +
                "    \n" +
                "    _f_ocelot_end();\n" +
                "    free(actualEvent);\n" +
                "    \n" +
                "    *size = eventSize;\n" +
                "    \n" +
                "    return eventToReturn;\n" +
                "}\n";

        return code;
    }

    private String generateExtractParameterTypeMethod (String type, int numberOfPointer) {
        String code = "";

        if (numberOfPointer == 0) {
            if (type.equals("int")) {
                code += "int extractParameter_int (Graph graph, Node variableNode) {\n" +
                        "    int val = (int)variableNode.value;\n" +
                        "    \n" +
                        "    return val;\n" +
                        "}\n";
            }

            else if (type.equals("double")) {
                code += "double extractParameter_double (Graph graph, Node variableNode) {\n" +
                        "    double val = variableNode.value;\n" +
                        "    \n" +
                        "    return val;\n" +
                        "}\n";
            } else if (type.equals("float")) {
                code += "float extractParameter_float (Graph graph, Node variableNode) {\n" +
                        "    float val = variableNode.value;\n" +
                        "    \n" +
                        "    return val;\n" +
                        "}\n";
            } else if (type.equals("char")) {
                code += "char extractParameter_char (Graph graph, Node variableNode) {\n" +
                        "    char val = (char)(((int)absValue(variableNode.value) % 58) + 65);\n" +
                        "    \n" +
                        "    return val;\n" +
                        "}\n";
            }

            else {
                //Take struct node
                CStruct struct;
                CType [] variableTypes = null;
                for (Node node : graph.getNodes()) {
                    if (node instanceof StructNode) {
                        CStruct structType = (CStruct) node.getCType();
                        if (structType.getNameOfStruct().equals(type) ||
                                (structType.getTypedefName() != null &&
                                        structType.getTypedefName().equals(type))) {
                            struct = structType;
                            variableTypes = structType.getStructVariables();
                            break;
                        }
                    }
                }

                code += type + " extractParameter_" + type + " (Graph graph, Node variableNode) {\n" +
                        "    Node *structVariables = children(graph, variableNode);\n" +
                        "    int numberOfVariables = size(graph, variableNode);\n" +
                        "    int i = 0;\n" +
                        "    \n" +
                        "    " + type + " val;\n" +
                        "    \n";

                boolean structPart = false;
                boolean ifStatement = true;
                for (CType variableType : variableTypes) {
                    String name = getName(variableType);
                    String structVariableType = getTypeName(variableType);
                    String pointerString = getPointerString(variableType);

                    CType realType = variableType;
                    while (realType instanceof CPointer)
                        realType = ((CPointer) realType).getType();

                    if (realType instanceof CStruct)
                        structPart = true;

                    if (!structPart) {
                        code += "    val." + name + " = extractParameter_" + structVariableType +
                                pointerString + "(graph, structVariables[i++]);\n";
                    } else {
                        if (ifStatement) {
                            code += "    if (i < numberOfVariables) {\n";
                        }

                        code += "        val." + name + " = extractParameter_" + structVariableType +
                                pointerString + "(graph, structVariables[i++]);\n";
                    }
                }

                if (ifStatement) {
                    code += "    }\n\n";
                }


                code += "    free(structVariables);\n" +
                        "    \n" +
                        "    return val;\n" +
                        "}\n";
            }
        } else {
            String star = "";
            String pointerString = "";
            for (int i = 0; i < numberOfPointer; i++) {
                star += "*";
                pointerString += "S";
            }

            code += type + star + " extractParameter_" + type + pointerString + " (Graph graph, Node variableNode) {\n" +
                    "    int size = 0;\n" +
                    "    int *index = getIndexOfVariableNode(graph, variableNode, &size);\n" +
                    "    \n" +
                    "    int i = 0;\n" +
                    "    \n";

            for (int i = 0; i <= numberOfPointer; i++) {
                int numberOfAllocation = (int) Math.pow(ARRAY_LENGTH, i);
                if (i == 0) {
                    code += "    " + type + " " + star + "val = malloc(sizeof(" + type +
                            star.substring(0, star.length() - i - 1) + ") * ARRAY_LENGTH);\n";
                } else {
                    for (int j = 0; j < numberOfAllocation; j++) {
                        code += "    val";
                        String squareIndexes = Integer.toString(j, ARRAY_LENGTH);
                        while (squareIndexes.length() < i) {
                            squareIndexes = "0" + squareIndexes;
                        }

                        char [] tmpSquare = squareIndexes.toCharArray();
                        squareIndexes = "";
                        for (int k = 0; k < tmpSquare.length; k++) {
                            squareIndexes += "[" + tmpSquare[k] + "]";
                        }

                        if (i == numberOfPointer) {
                            code += squareIndexes + " = extractParameter_" + type + "(graph, graph.nodes[index[i++]]);\n";
                        } else {
                            code += squareIndexes + " = malloc(sizeof(" + type +
                                    star.substring(0, star.length() - i - 1) + ") * ARRAY_LENGTH);\n";
                        }
                    }
                }
            }

            code += "    \n" +
                    "    free(index);\n" +
                    "    \n" +
                    "    return val;\n" +
                    "}\n";
        }

        return code;
    }

    private String generateFreeParametersMethod () {
        String code = "void freeParameters (FunctionParameters functionParameters) {\n";

        //Parameter section
        ArrayList<Node> parameterNodes = graphManager.children(graph, graph.getNode(0));
        for (int i = 0; i < parameterNodes.size(); i++) {
            //Variable info
            String variableName = getName(parameterNodes.get(i).getCType());
            CType variableType = parameterNodes.get(i).getCType();

            //Do the free only on pointers
            if (variableType instanceof CPointer) {
                code += "    free(functionParameters." + variableName + ");\n";
            }

        }

        code += "}\n";

        return code;
    }



    private String getName (CType type) {
        String name = null;

        CType actualType = type;
        while (actualType instanceof CPointer) {
            actualType = ((CPointer) actualType).getType();
        }

        if (actualType instanceof CPrimitive) {
            name = ((CPrimitive) actualType).getName();
        } else {
            name = ((CStruct)actualType).getName();
        }

        return name;
    }

    private String getTypeName (CType type) {
        String typeName = null;

        CType actualType = type;
        while (actualType instanceof CPointer) {
            actualType = ((CPointer) actualType).getType();
        }

        if (actualType instanceof CInteger) {
            typeName = "int";
        } else if (actualType instanceof CDouble) {
            typeName = "double";
        } else if (actualType instanceof CChar) {
            typeName = "char";
        } else if (actualType instanceof  CFloat) {
            typeName = "float";
        } else {
            String typeDef = ((CStruct)actualType).getTypedefName();
            if (typeDef == null) {
                typeName = ((CStruct)actualType).getNameOfStruct();
            } else {
                typeName = typeDef;
            }
        }

        return typeName;
    }

    private String getPointerString (CType type) {
        String pointerString = "";

        int numberOfPointer = getNumberOfPointer(type);
        for (int i = 0; i < numberOfPointer; i++) {
            pointerString += "S";
        }

        return pointerString;
    }

    private int getNumberOfPointer (CType type) {
        int numberOfPointer = 0;

        CType actualType = type;
        while (actualType instanceof CPointer) {
            numberOfPointer++;
            actualType = ((CPointer) actualType).getType();
        }

        return numberOfPointer;
    }

    private LinkedHashMap<String, Integer> graphTypeList () {
        LinkedHashMap<String, Integer> typeList = new LinkedHashMap<>();

        for (Node node : graph.getNodes()) {
            if (node.getCType() != null) {
                String type = getTypeName(node.getCType());
                int numberOfPointer = getNumberOfPointer(node.getCType());

                if (!typeList.containsKey(type) || typeList.get(type) < numberOfPointer) {
                    typeList.put(type, numberOfPointer);
                }
            }
        }

        return typeList;
    }

    //To test with all possible cases
    private LinkedHashSet<String> getPointerList (Node root, String variablePrefix) {
        LinkedHashSet<String> variableNames = new LinkedHashSet<>();

        ArrayList<Node> children = graphManager.children(graph, root);

        for (Node node : children) {
            if (node instanceof PointerNode) {
                String variableName = variablePrefix + getName(node.getCType());
                variableNames.add(variableName);

                if (graphManager.getRealType(node.getCType()) instanceof CStruct) {
                    Node arrayNode = graphManager.children(graph, node).get(0);

                    //Take the fisrt struct node, inside the relative arrayNode
                    Node structNode = graphManager.children(graph, arrayNode).get(0);

                    LinkedHashSet<String> variableNamesOfSubGraph = getPointerList(structNode, variableName + "->");
                    variableNames.addAll(variableNamesOfSubGraph);
                }
            }

            else if (node instanceof StructNode) {
                String variableName = variablePrefix + getName(node.getCType());

                LinkedHashSet<String> variableNamesOfSubGraph = getPointerList(node, variableName + ".");
                variableNames.addAll(variableNamesOfSubGraph);
            }
        }

        return variableNames;
    }
}
