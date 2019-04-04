package it.unisa.ocelot.genetic.encoding.manager;

import it.unisa.ocelot.c.types.CPointer;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;
import it.unisa.ocelot.genetic.encoding.graph.Edge;
import it.unisa.ocelot.genetic.encoding.graph.ScalarNode;

import java.util.ArrayList;

public class GraphManager {

    public Node getNodeParent (Node pNode, Graph pGraph) {
        int minIndex = Integer.MAX_VALUE;
        Node parent = null;

        for (Edge edge : pGraph.getEdges()) {
            if (edge.getNodeFrom().equals(pNode)) {
                if (edge.getNodeTo().getId() < minIndex) {
                    parent = edge.getNodeTo();
                    minIndex = parent.getId();
                }
            }

            else if (edge.getNodeTo().equals(pNode)) {
                if (edge.getNodeFrom().getId() < minIndex) {
                    parent = edge.getNodeFrom();
                    minIndex = parent.getId();
                }
            }
        }

        return parent;
    }

    public ArrayList<Node> children (Graph graph, Node parent) {
        if (parent.getId() >= graph.getNodes().size()) {
            return null;
        }

        ArrayList<Node> children = new ArrayList<>();

        //Search incident
        int index = 0;
        for (int i = 0; i < graph.getEdges().size(); i++) {
            if (graph.getEdges().get(i).getNodeFrom().getId() == parent.getId() &&
                    graph.getEdges().get(i).getNodeTo().getId() > parent.getId()){
                children.add(graph.getEdges().get(i).getNodeTo());
            }

            else if (graph.getEdges().get(i).getNodeTo().getId() == parent.getId() &&
                    graph.getEdges().get(i).getNodeFrom().getId() > parent.getId()) {
                children.add(graph.getEdges().get(i).getNodeFrom());
            }
        }

        return children;
    }

    public CType getRealType (CType pointerType) {
        CType actualType = pointerType;
        while (actualType instanceof CPointer) {
            actualType = ((CPointer) actualType).getType();
        }

        return actualType;
    }

    public ArrayList<ScalarNode> getScalarNodes (Graph graph) {
        ArrayList<ScalarNode> scalarNodes = new ArrayList<>();

        for (Node node : graph.getNodes()) {
            if (node instanceof ScalarNode) {
                scalarNodes.add((ScalarNode) node);
            }
        }

        return scalarNodes;
    }

    public int getNumberOfScalarNodes (Graph graph) {
        return getScalarNodes(graph).size();
    }
}
