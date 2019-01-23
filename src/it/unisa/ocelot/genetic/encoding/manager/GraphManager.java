package it.unisa.ocelot.genetic.encoding.manager;

import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;
import it.unisa.ocelot.genetic.encoding.graph.Edge;

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
}
