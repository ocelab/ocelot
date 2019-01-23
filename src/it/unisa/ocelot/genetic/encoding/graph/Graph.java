package it.unisa.ocelot.genetic.encoding.graph;

import java.util.ArrayList;

public class Graph {
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;

    public Graph () {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public Graph (ArrayList<Node> pNodes, ArrayList<Edge> pEdges) {
        this.nodes = pNodes;
        this.edges = pEdges;
    }

    public Node getNode(int index) {
        return nodes.get(index);
    }

    public Node getLastNode() {
        if (!this.nodes.isEmpty()) {
            return this.nodes.get(this.nodes.size()-1);
        }

        return null;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Node> getIncidentNodes (Node node) {
        ArrayList<Node> incidentNodes = new ArrayList<>();

        for (Edge e : this.edges) {
            if (e.getNodeFrom().getId() == node.getId()) {
                incidentNodes.add(e.getNodeTo());
            }

            else if (e.getNodeTo().getId() == node.getId()) {
                incidentNodes.add(e.getNodeFrom());
            }
        }

        return incidentNodes;
    }

    public void addNode (Node pNode) {
        this.nodes.add(pNode);
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void addEdge (Edge pEdge) {
        this.edges.add(pEdge);
    }

    public int getLastId () {
        return getLastNode().getId();
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Graph graphClone = new Graph();
        ArrayList<Node> nodeClone = new ArrayList<>(nodes);
        ArrayList<Edge> edgeClone = new ArrayList<>(edges);

        graphClone.setNodes(nodeClone);
        graphClone.setEdges(edgeClone);

        return graphClone;
    }
}
