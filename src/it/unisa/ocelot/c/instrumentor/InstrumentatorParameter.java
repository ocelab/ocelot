package it.unisa.ocelot.c.instrumentor;

import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.c.types.CPointer;
import it.unisa.ocelot.c.types.CPrimitive;
import it.unisa.ocelot.c.types.CStruct;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;
import it.unisa.ocelot.genetic.encoding.manager.GraphGenerator;
import it.unisa.ocelot.genetic.encoding.manager.GraphManager;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.util.ArrayList;

public class InstrumentatorParameter {
    private String testFilename;
    private String testFunction;
    private String[] testIncludes;

    private int tmpIndex;

    public InstrumentatorParameter (String pTestFilename, String pTestFunction, String[] pTestIncludes) {
        this.testFilename = pTestFilename;
        this.testFunction = pTestFunction;
        this.testIncludes = pTestIncludes;

        this.tmpIndex = 0;
    }


    public void instrument () throws Exception {
        IASTTranslationUnit translationUnit = GCC.getTranslationUnit(
                this.testFilename,
                this.testIncludes).copy();

        ExternalReferencesVisitor referencesVisitor = new ExternalReferencesVisitor(this.testFunction);
        translationUnit.accept(referencesVisitor);

        InstrumentorVisitor instrumentor = new InstrumentorVisitor(this.testFunction);
        MacroDefinerVisitor macroDefiner = new MacroDefinerVisitor(this.testFunction, referencesVisitor.getExternalReferences());

        //NOTE: macroDefine MUST preceed instrumentor in visit
        translationUnit.accept(macroDefiner);
        translationUnit.accept(instrumentor);

        CType[] parameterTypes = macroDefiner.getFunctionParametersFromMacroDefinerVisitor(
                macroDefiner.getFunctionParametersMap(), null, null, null);
        Graph graph = GraphGenerator.generateGraph(parameterTypes);

        //Source
        String sourceFunction = getSourceCodeFromGraph(graph);



        String headerFuntion = "";
    }

    private String getSourceCodeFromGraph (Graph graph) {
        GraphManager graphManager = new GraphManager();
        ArrayList<Node> parameterNodes = graphManager.children(graph, graph.getNodes().get(0));

        String source = "#include \"function.h\"\n";

        //Function -> extractParametersFromGraph
        source += "FunctionParameters extractParametersFromGraph(Graph graph) {\n" +
                "    FunctionParameters functionParameters;\n" +
                "\n" +
                "    Node* variableNodes = children(graph, graph.nodes[0]);\n\n";

        for (int i = 0; i < parameterNodes.size(); i++) {
            String variableName = getVariableName(parameterNodes.get(i).getCType());

            source += "    functionParameters." + variableName + " = extractParameter" + (i+1) +
                    "(graph, variableNodes[" + i + "]);\n";
        }

        source += "\nreturn functionParameters;\n}\n\n";


        //Function -> executeFunction
        source += "void executeFunction(FunctionParameters functionParameters) {\n" +
                "    _f_ocelot_init();\n    OCELOT_TESTFUNCTION(";
        for (int i = 0; i < parameterNodes.size(); i++) {
            String variableName = getVariableName(parameterNodes.get(i).getCType());

            source += "functionParameters." + variableName + ", \n";
        }
        source = source.substring(0, source.length()-2);
        source += ");\n    removeEvent(events, 0);\n}\n\n";


        //Function -> extractParameter#
        for (int i = 0; i < parameterNodes.size(); i++) {
            //Set type of function
            String variableType = getVariableName(parameterNodes.get(i).getCType());
            String variablePointer = getStringPointer(parameterNodes.get(i).getCType());

            source += variableType + variablePointer + " extractParameter1(Graph graph, Node variableNode) {\n";
            source += generateBlock(graph, parameterNodes.get(i), 0);

        }
        return source;
    }

    private String getHeaderFromGraph (Graph graph) {
        return null;
    }


    private String extractParameterStruct (Graph graph, Node node, int numberOfFunction) {
        String code = "";
        String variableType = getVariableType(node.getCType());
        int numberOfPointer = getNumberOfPointer(node.getCType());
        String pointer = getStringPointer(node.getCType());

        code += variableType + pointer + " extractParameter" + numberOfFunction + "(Graph graph, Node variableNode) {\n";

        if (numberOfPointer == 0) {
            code += "    " + variableType + " var = *extractInfoFromStructure(graph, variableNode);\n\n";
        } else {
            code += "    " + variableType + " "+ pointer + "var = malloc(sizeof (" + variableType + ") * ARRAY_LENGTH);\n" +
                    "\n" +
                    "    Node *arrayNode = children(graph, variableNode);\n" +
                    "    Node *structNodes = children(graph, arrayNode[0]);\n" +
                    "    \n" +
                    "    for (int i = 0; i < ARRAY_LENGTH; i++) {\n" +
                    "        data[i] = *extractInfoFromStructure(graph, structNodes[i]);\n" +
                    "    }\n" +
                    "\n" +
                    "    return data;\n" +
                    "}";
        }


        return code;
    }



    private CType getRealType (CType pointerType) {
        CType actualType = pointerType;
        while (actualType instanceof CPointer) {
            actualType = ((CPointer) actualType).getType();
        }

        return actualType;
    }

    private ArrayList<CStruct> getCStructs (Graph graph) {
        ArrayList<CStruct> cstructs = new ArrayList<>();

        for (int i = 0; i < graph.getNodes().size(); i++) {
            if (graph.getNodes().get(i).getCType() != null &&
                    graph.getNodes().get(i).getCType() instanceof CStruct) {
                cstructs.add((CStruct) graph.getNodes().get(i).getCType());
            }
        }

        if (cstructs.size() == 0)
            return null;

        for (int i = 0; i < cstructs.size(); i++) {
            for (int j = 0; j < cstructs.size(); j++) {
                if (cstructs.get(i).getName().equals(cstructs.get(j).getName())) {
                    cstructs.remove(j--);
                }
            }
        }

        return cstructs;

    }

    private String getStringPointer(CType pointerType) {
        String pointer = "";
        int numberOfPointer = getNumberOfPointer(pointerType);
        for (int i = 0; i < numberOfPointer; i++) {
            pointer += "*";
        }

        return pointer;
    }

    private int getNumberOfPointer(CType pointerType) {
        int pointer = 0;
        CType actualType = pointerType;

        while (actualType instanceof CPointer) {
            pointer++;
            actualType = ((CPointer) actualType).getType();
        }

        return pointer;
    }

    private String getVariableName(CType type) {
        CType realVariableType = getRealType(type);
        String variableName = null;
        if (realVariableType instanceof CPrimitive) {
            variableName = ((CPrimitive) realVariableType).getName();
        } else {
            variableName = ((CStruct)realVariableType).getName();
        }

        return variableName;
    }

    private String getVariableType(CType type) {
        CType realVariableType = getRealType(type);
        return realVariableType.toString();
    }

    private String generateBlock(Graph graph, Node node, int indexVariable) {
        String code = "";
        String variableType = getVariableType(node.getCType());
        int numberOfPointer = getNumberOfPointer(node.getCType());
        String pointer = getStringPointer(node.getCType());

        code += variableType + " " + pointer + "variable" + indexVariable++;

        pointer = pointer.substring(0, pointer.length()-1);

        code += " = malloc(sizeof(" + variableType + pointer + ") * ARRAY_LENGTH);\n\n" +
                "Node *arrayNode;\n\n";

        code += "Node *var" + indexVariable++ + " = children(graph, arrayNode[0]);\n";

        for (int i = 0; i < numberOfPointer; i++) {
            if (pointer.length() > 0)
                pointer = pointer.substring(0, pointer.length()-1);
            else
                pointer = "";


            code += "for (int var" + (indexVariable+i) + " = 0; var" + (indexVariable+i) + " < ARRAY_LENGTH; var" + (indexVariable+i) + "++){\n" +
                    "variable";
            for (int j = 0; j < i+1; j++) {
                code += "[var" + (indexVariable+i) + "]";
            }

            if (i < numberOfPointer-1) {
                code += " = malloc(sizeof(" + variableType + pointer + ") * (ARRAY_LENGTH+1));\n" +
                        "arrayNode = children(graph, var" + (indexVariable-1) + "[i]);";
            }
        }




        return null;
    }

    private String generateBlock2(Graph graph, Node node, int indexVariable) {
        String code = "";
        String variableType = getVariableType(node.getCType());
        int numberOfPointer = getNumberOfPointer(node.getCType());
        String pointer = getStringPointer(node.getCType());

        code += variableType + " " + pointer + "variable" + indexVariable++;

        pointer = pointer.substring(0, pointer.length()-1);

        code += " = malloc(sizeof(" + variableType + pointer + ") * ARRAY_LENGTH);\n\n" +
                "Node *arrayNode;\n\n";

        code += "Node *var" + indexVariable++ + " = children(graph, arrayNode[0]);\n";

        for (int i = 0; i < numberOfPointer; i++) {
            if (pointer.length() > 0)
                pointer = pointer.substring(0, pointer.length()-1);
            else
                pointer = "";


            code += "for (int var" + (indexVariable+i) + " = 0; var" + (indexVariable+i) + " < ARRAY_LENGTH; var" + (indexVariable+i) + "++){\n" +
                    "variable";
            for (int j = 0; j < i+1; j++) {
                code += "[var" + (indexVariable+i) + "]";
            }

            if (i < numberOfPointer-1) {
                code += " = malloc(sizeof(" + variableType + pointer + ") * (ARRAY_LENGTH+1));\n" +
                        "arrayNode = children(graph, var" + (indexVariable-1) + "[i]);";
            }
        }




        return null;
    }
}
