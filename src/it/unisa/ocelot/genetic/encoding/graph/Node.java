package it.unisa.ocelot.genetic.encoding.graph;

import it.unisa.ocelot.c.types.CType;

public class Node {
    private int id;
    private CType type;

    public Node (int pId, CType pType) {
        this.id = pId;
        this.type = pType;
    }

    public int getId() {
        return id;
    }

    public CType getCType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        Node nodeObj = (Node) obj;
        if (this.getId() == nodeObj.getId())
            return true;

        return false;
    }
}
