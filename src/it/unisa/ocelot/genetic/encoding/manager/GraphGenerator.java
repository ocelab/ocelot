package it.unisa.ocelot.genetic.encoding.manager;

import it.unisa.ocelot.c.types.CPointer;
import it.unisa.ocelot.c.types.CPrimitive;
import it.unisa.ocelot.c.types.CStruct;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.encoding.graph.*;

import java.util.ArrayList;
import java.util.Stack;

public class GraphGenerator {
    public static final int MAX_SIZE_ARRAY = 1;
    public static final int STRUCT_DEEP = 0;

    private static GraphManager graphManager;

    public GraphGenerator () {
        this.graphManager = new GraphManager();
    }

    public static Graph generateGraph (CType[] parameters) {
        graphManager = new GraphManager();
        Graph graph = new Graph();

        Stack<Node> backTrackingNodes= new Stack<>();

        //Root Creation
        graph.addNode(new RootNode(0));

        //Add root in the stack
        backTrackingNodes.push(graph.getLastNode());


        for (CType paramenter : parameters) {
            Node parameterNode = null;

            if (paramenter instanceof  CPrimitive) {
                parameterNode = new ScalarNode(graph.getLastId()+1, paramenter);
            }
            else if (paramenter instanceof CStruct) {
                parameterNode = new StructNode(graph.getLastId()+1, paramenter);
            }
            else {
                parameterNode = new PointerNode(graph.getLastId()+1, paramenter);
            }

            graph.addNode(parameterNode);
            graph.addEdge(new Edge(graph.getNode(0), parameterNode));


            Node actualNode = null;
            Node newNode = null;

            //Generation of parameter subtree
            if (!(paramenter instanceof CPrimitive)) {
                backTrackingNodes.push(parameterNode);

                do {
                    actualNode = backTrackingNodes.peek();
                    newNode = null;

                    if (actualNode instanceof ArrayNode) {
                        //I have to create MAX_SIZE_ARRAY elements, of ArrayNode's ctype

                        //Check if I have already created MAX_SIZE_ARRAY elements
                        ArrayList<Node> children = graphManager.children(graph, actualNode);
                        if (children.size() < MAX_SIZE_ARRAY) {
                            CType pointerType = ((CPointer)actualNode.getCType()).getType();

                            if (pointerType instanceof CPrimitive) {
                                newNode = new ScalarNode(graph.getLastId()+1, pointerType);
                            }
                            else if (pointerType instanceof CStruct) {
                                int deepLevel = getDeepLevel(backTrackingNodes, pointerType);

                                if (deepLevel <= STRUCT_DEEP) {
                                    newNode = new StructNode(graph.getLastId()+1, pointerType);
                                } else {
                                    newNode = new StructNode(graph.getLastId()+1, null);
                                }
                            }
                            else {
                                newNode = new PointerNode(graph.getLastId()+1, pointerType);
                            }
                        }

                        //In this case, remove the arrayNode from stack
                        else {
                            //ArrayNode
                            backTrackingNodes.pop();

                            //PointerNode
                            backTrackingNodes.pop();
                        }
                    }

                    else if (actualNode instanceof StructNode) {
                        CStruct structType = (CStruct) actualNode.getCType();
                        CType [] structVariables = structType.getStructVariables();
                        int numberOfChildren = graphManager.children(graph, actualNode).size();

                        if (numberOfChildren < structVariables.length) {
                            CType structVariable = structVariables[numberOfChildren];

                            if (structVariable instanceof CPrimitive) {
                                newNode = new ScalarNode(graph.getLastId()+1, structVariable);
                            }
                            else if (structVariable instanceof CStruct) {
                                CType newStructType = structVariable;

                                //Check if structVariable is the same of parent. In that case, take the last one, because the variables of structvariable will be null
                                if (((CStruct) structVariable).getStructVariables() == null)
                                    newStructType = structType;


                                int deepLevel = getDeepLevel(backTrackingNodes, structVariable);

                                if (deepLevel <= STRUCT_DEEP) {
                                    newNode = new StructNode(graph.getLastId()+1, newStructType);
                                } else {
                                    newNode = new StructNode(graph.getLastId()+1, null);
                                }
                            }
                            else {
                                CType pointerNodeRealType = graphManager.getRealType(structVariable);

                                if (pointerNodeRealType instanceof CStruct) {
                                    CType newStructType = structVariable;

                                    //Check if structVariable is the same of parent. In that case, take the last one, because the variables of structvariable will be null
                                    if (((CStruct) pointerNodeRealType).getStructVariables() == null) {
                                        newStructType = structType;

                                        CType actualType = structVariable;
                                        while (actualType instanceof CPointer) {
                                            newStructType = new CPointer(newStructType);
                                            actualType = ((CPointer) actualType).getType();
                                        }
                                    }


                                    int deepLevel = getDeepLevel(backTrackingNodes, pointerNodeRealType);

                                    if (deepLevel <= STRUCT_DEEP) {
                                        newNode = new PointerNode(graph.getLastId()+1, newStructType);
                                    } else {
                                        newNode = new PointerNode(graph.getLastId()+1, null);
                                    }
                                } else {
                                    newNode = new PointerNode(graph.getLastId()+1, structVariable);
                                }
                            }
                        } else {
                            //StructNode
                            backTrackingNodes.pop();
                        }
                    }

                    else if (actualNode instanceof PointerNode) {
                        newNode = new ArrayNode(graph.getLastId()+1, actualNode.getCType());
                    }

                    if (newNode != null) {
                        //Add node to stack, only if it may have children or it is a valid pointerNode
                        if (!(newNode instanceof ScalarNode) &&
                                !((newNode instanceof PointerNode || newNode instanceof StructNode) && newNode.getCType() == null)) {
                            backTrackingNodes.push(newNode);
                        }

                        graph.addNode(newNode);
                        graph.addEdge(new Edge(actualNode, newNode));
                    }

                } while (backTrackingNodes.peek().getId() != 0);
            }
        }

        return graph;
    }

    private static int getDeepLevel(Stack<Node> stack, CType type) {
        int deepLevel = 0;

        //Check the number of same struct that i have already created in the subtree
        for (Node stackNode : stack) {
            if (stackNode instanceof StructNode) {
                String stackNodeTypedefName = ((CStruct)stackNode.getCType()).getTypedefName();
                String stackNodeNameOfStruct = ((CStruct)stackNode.getCType()).getNameOfStruct();

                if (stackNodeTypedefName != null && stackNodeTypedefName.equals(((CStruct) type).getTypedefName()) ||
                        stackNodeNameOfStruct.equals(((CStruct) type).getNameOfStruct())) {
                    deepLevel++;
                }
            }
        }

        return deepLevel;
    }

    private static boolean checkIfExistAnotherStruct(Stack<Node> stack, CType type) {
        CType realType = graphManager.getRealType(type);

        //Check the number of same struct that i have already created in the subtree
        for (Node stackNode : stack) {
            if (stackNode instanceof StructNode) {
                String stackNodeTypedefName = ((CStruct)stackNode.getCType()).getTypedefName();
                String stackNodeNameOfStruct = ((CStruct)stackNode.getCType()).getNameOfStruct();

                if (stackNodeTypedefName != null && stackNodeTypedefName.equals(((CStruct) realType).getTypedefName()) ||
                        stackNodeNameOfStruct.equals(((CStruct) realType).getNameOfStruct())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static CType getOriginalStructType (Stack<Node> stack, CType type) {
        CType realType = graphManager.getRealType(type);

        //Check the number of same struct that i have already created in the subtree
        for (Node stackNode : stack) {
            if (stackNode instanceof StructNode) {
                String stackNodeTypedefName = ((CStruct)stackNode.getCType()).getTypedefName();
                String stackNodeNameOfStruct = ((CStruct)stackNode.getCType()).getNameOfStruct();

                if (stackNodeTypedefName != null && stackNodeTypedefName.equals(((CStruct) realType).getTypedefName()) ||
                        stackNodeNameOfStruct.equals(((CStruct) realType).getNameOfStruct())) {
                    if (((CStruct) stackNode.getCType()).getStructVariables() != null)
                        return stackNode.getCType();
                }
            }
        }

        return type;
    }
}
