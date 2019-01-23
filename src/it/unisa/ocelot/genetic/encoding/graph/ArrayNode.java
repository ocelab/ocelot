package it.unisa.ocelot.genetic.encoding.graph;

import it.unisa.ocelot.c.types.CType;

import java.util.ArrayList;

public class ArrayNode extends Node {
    private static final int MAX_ARRAY_SIZE = 5;
    private ArrayList<Node> arrayNodes;

    public ArrayNode(int pId, CType pType) {
        super(pId, pType);

        this.arrayNodes = new ArrayList<>();
    }

    public void setArrayNodes(ArrayList<Node> arrayNodes) {
        this.arrayNodes = arrayNodes;
    }
}
