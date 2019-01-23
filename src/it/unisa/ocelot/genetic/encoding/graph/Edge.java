package it.unisa.ocelot.genetic.encoding.graph;

public class Edge {
    private Node nodeFrom;
    private Node nodeTo;

    public Edge (Node pNodeFrom, Node pNodeTo) {
        this.nodeFrom = pNodeFrom;
        this.nodeTo = pNodeTo;
    }

    public Node getNodeFrom() {
        return nodeFrom;
    }

    public Node getNodeTo() {
        return nodeTo;
    }

    public boolean contains (int idNode) {
        if (this.nodeFrom.getId() == idNode || this.nodeTo.getId() == idNode) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "from " + this.getNodeFrom().getId() + " to " + this.getNodeTo().getId();
    }
}
