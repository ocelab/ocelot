package it.unisa.ocelot.genetic.encoding.manager;

import it.unisa.ocelot.c.types.CPointer;
import it.unisa.ocelot.c.types.CPrimitive;
import it.unisa.ocelot.c.types.CStruct;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.encoding.graph.*;

import java.util.ArrayList;

public class GraphGenerator {
    public static final int MAX_SIZE_ARRAY = 3;
    public static final int STRUCT_DEEP = 1;

    private static GraphManager graphManager;

    public GraphGenerator () {
        this.graphManager = new GraphManager();
    }

    public static Graph generateGraphFromFunction (CType [] parameters) {
        Graph graph = new Graph();

        //Root Creation
        graph.addNode(new RootNode(0));

        for (CType paramenter : parameters) {
            //No pointer
            if (!(paramenter instanceof CPointer)) {
                //Scalar/primitive type
                if (paramenter instanceof CPrimitive) {
                    graph.addNode(new ScalarNode(graph.getLastId()+1, paramenter));
                    graph.addEdge(new Edge(graph.getNode(0), graph.getLastNode()));
                }

                //Struct type
                else if (paramenter instanceof CStruct) {
                    graph.addNode(new StructNode(graph.getLastId()+1, paramenter, 0));
                    graph.addEdge(new Edge(graph.getNode(0), graph.getLastNode()));

                    CStruct structVariable = (CStruct) paramenter;
                    generateStructNodeTree((StructNode) graph.getLastNode(),
                            structVariable.getStructVariables(), graph);
                }
            } else {
                //Create pointer
                graph.addNode(new PointerNode(graph.getLastId()+1, paramenter));
                graph.addEdge(new Edge(graph.getNode(0), graph.getLastNode()));

                generateArrayNodeTree((PointerNode) graph.getLastNode(), graph);
            }
        }

        return graph;
    }

    public Graph generateGraphFromArrayNodes (ArrayList<Node> nodes, Graph modelGraph) {
        ArrayList<Node> newNodes = new ArrayList<>(nodes);
        ArrayList<Edge> edges = modelGraph.getEdges();
        ArrayList<Edge> newEdges = new ArrayList<>();

        for (Edge edge : edges) {
            Node nodeFrom = edge.getNodeFrom();
            Node nodeTo = edge.getNodeTo();
            Node newNodeFrom = null;
            Node newNodeTo = null;

            int i = 0;
            while (i < nodes.size() && (newNodeFrom == null || newNodeTo == null)) {
                if (nodes.get(i).equals(nodeFrom))
                    newNodeFrom = nodes.get(i);

                else if (nodes.get(i).equals(nodeTo))
                    newNodeTo = nodes.get(i);

                i++;
            }

            Edge newEdge = new Edge(newNodeFrom, newNodeTo);
            newEdges.add(newEdge);
        }

        return new Graph(newNodes, newEdges);
    }

    private static void generateArrayNodeTree(PointerNode pointerNode, Graph graph) {
        //Create arrayNode
        graph.addNode(new ArrayNode(graph.getLastId()+1, pointerNode.getCType()));
        graph.addEdge(new Edge(pointerNode, graph.getLastNode()));

        ArrayNode arrayNode = (ArrayNode) graph.getLastNode();
        ArrayList<Node> arrayNodes = null;

        CPointer arrayNodePointerType = (CPointer) arrayNode.getCType();

        if (arrayNodePointerType.getType() instanceof CPointer) {
            int i = 0;
            while (arrayNodePointerType.getType() instanceof CPointer && i < MAX_SIZE_ARRAY) {
                arrayNodes = new ArrayList<>();
                for (i = 0; i < MAX_SIZE_ARRAY; i++) {
                    arrayNodes.add(new PointerNode(graph.getLastId()+1,
                            (((CPointer) arrayNode.getCType()).getType())));

                    graph.addNode(arrayNodes.get(arrayNodes.size()-1));
                    graph.addEdge(new Edge(arrayNode, graph.getLastNode()));

                    generateArrayNodeTree((PointerNode) graph.getLastNode(), graph);
                }
                ((ArrayNode)graph.getNode(arrayNode.getId())).setArrayNodes(arrayNodes);
                //arrayNodes.add(new PointerNode(++actualId, pointerNode.getVariable()));
            }
        } else {    //End of the tree
            arrayNodes = new ArrayList<>();
            for (int i = 0; i < MAX_SIZE_ARRAY; i++) {
                if (arrayNodePointerType.getType() instanceof CPrimitive) {
                    arrayNodes.add(new ScalarNode(graph.getLastId()+1, arrayNodePointerType.getType()));

                    graph.addNode(arrayNodes.get(arrayNodes.size()-1));
                    graph.addEdge(new Edge(arrayNode, graph.getLastNode()));
                } else if (arrayNodePointerType.getType() instanceof CStruct) {
                    //Search deepLevel of struct parent, located into parent's pointer
                    //See if parent's pointer is a structure or a root
                    Node pointerParent = graphManager.getNodeParent(pointerNode, graph);
                    CStruct structVariable = null;

                    if (pointerParent instanceof RootNode) {
                        structVariable = (CStruct) arrayNodePointerType.getType();
                        arrayNodes.add(new StructNode(graph.getLastId()+1,
                                structVariable, 0));

                        graph.addNode(arrayNodes.get(arrayNodes.size()-1));
                        graph.addEdge(new Edge(arrayNode, graph.getLastNode()));
                    } else {
                        pointerParent = (StructNode) graphManager.getNodeParent(pointerNode, graph);
                        structVariable = (CStruct) pointerParent.getCType();
                        arrayNodes.add(new StructNode(graph.getLastId()+1, pointerParent.getCType(),
                                (((StructNode) pointerParent).getDeepStructLevel() + 1)));

                        graph.addNode(arrayNodes.get(arrayNodes.size()-1));
                        graph.addEdge(new Edge(arrayNode, graph.getLastNode()));
                    }

                    generateStructNodeTree((StructNode) graph.getLastNode(), structVariable.getStructVariables(), graph);
                }
            }
            ((ArrayNode)graph.getNode(arrayNode.getId())).setArrayNodes(arrayNodes);
        }
    }

    private static void generateStructNodeTree(StructNode parentNode, CType[] parameters, Graph pGraph) {
        int actualDeepLevel = parentNode.getDeepStructLevel();
        CStruct actualStructType = null;

        for (CType parameter : parameters) {
            if (!(parameter instanceof CPointer)) {
                if (parameter instanceof CPrimitive) {
                    pGraph.addNode(new ScalarNode(pGraph.getLastId()+1, parameter));
                    pGraph.addEdge(new Edge(parentNode, pGraph.getLastNode()));
                }

                else if (parameter instanceof CStruct) {
                    actualStructType = (CStruct) parameter;

                    if (actualStructType.equals(parentNode.getCType()) && actualDeepLevel < STRUCT_DEEP) {
                        //We take variables of parent because, if the struct is the same, the info about its variables are
                        //contained only in the original struct
                        pGraph.addNode(new StructNode(pGraph.getLastId()+1, parentNode.getCType(),
                                actualDeepLevel + 1));
                        pGraph.addEdge(new Edge(parentNode, pGraph.getLastNode()));

                        generateStructNodeTree((StructNode) pGraph.getLastNode(), parameters, pGraph);
                    }   //No else, because in this case we should create a null node

                    else if (!actualStructType.equals(parentNode.getCType())) {
                        pGraph.addNode(new StructNode(pGraph.getLastId()+1, parameter, 0));
                        pGraph.addEdge(new Edge(parentNode, pGraph.getLastNode()));

                        generateStructNodeTree((StructNode) pGraph.getLastNode(),
                                actualStructType.getStructVariables(), pGraph);
                    }
                }
            } else {
                //Se type of pointer
                CType typeOfPointer = ((CPointer) parameter).getType();

                if (!(typeOfPointer instanceof CStruct)) {
                    pGraph.addNode(new PointerNode(pGraph.getLastId()+1, parameter));
                    pGraph.addEdge(new Edge(parentNode, pGraph.getLastNode()));

                    generateArrayNodeTree((PointerNode) pGraph.getLastNode(), pGraph);
                } else {
                    //Check if create another pointer of struct
                    actualStructType = (CStruct) typeOfPointer;
                    if (!actualStructType.equals(parentNode.getCType()) || (actualStructType.equals(parentNode.getCType()) && actualDeepLevel < STRUCT_DEEP)) {
                        pGraph.addNode(new PointerNode(pGraph.getLastId()+1, parameter));
                        pGraph.addEdge(new Edge(parentNode, pGraph.getLastNode()));

                        generateArrayNodeTree((PointerNode) pGraph.getLastNode(), pGraph);
                    }
                }
            }
        }
    }
}
