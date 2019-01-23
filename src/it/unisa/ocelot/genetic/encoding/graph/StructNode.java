package it.unisa.ocelot.genetic.encoding.graph;

import it.unisa.ocelot.c.types.CType;

public class StructNode extends Node {
    private int deepStructLevel;

    public StructNode(int pId, CType pType, int pDeepStructLevel) {
        super(pId, pType);
        this.deepStructLevel = pDeepStructLevel;
    }

    public int getDeepStructLevel() {
        return deepStructLevel;
    }
}
