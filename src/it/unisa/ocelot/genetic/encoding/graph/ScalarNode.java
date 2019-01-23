package it.unisa.ocelot.genetic.encoding.graph;

import it.unisa.ocelot.c.types.CType;

public class ScalarNode extends Node {
    private double value;

    public ScalarNode(int pId, CType pType) {
        super(pId, pType);
        this.value = 0;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
