package it.unisa.ocelot.simulator;

import it.unisa.ocelot.c.types.CDouble;
import it.unisa.ocelot.c.types.CInteger;
import it.unisa.ocelot.genetic.encoding.graph.Edge;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;

import java.util.ArrayList;

public class CBridgeStub extends CBridge {
    private static int n;
    private static boolean initialized;
    
    private int values;
    private int arrays;
    private int pointers;

    public CBridgeStub () {
        super();
    }

    public CBridgeStub(int pCoreID, int numberOfValue, int numberOfArray, int numberOfPointer) {
        super(pCoreID);

        this.values = numberOfValue;
        this.arrays = numberOfArray;
        this.pointers = numberOfPointer;
    }

    public void initialize(Graph graph) {
        if (!initialized) {
            int numberOfValue = 0;
        }
    }

    public void getEvents(EventsHandler pHandler, Graph graph) {
        Object [] values = generateRandomValues(graph);
        Object [][] arrays = generateRandomArrayValues(this.arrays, 3);
        Object [] pointers = generateRandomPointer(this.pointers, this.arrays);

        super.getEvents(pHandler, values, arrays, pointers);
    }

    private Object [] generateRandomValues (Graph graph) {
        //Get all root's scalarNode
        ArrayList<Node> scalarNodes = new ArrayList<>();
        int idRoot = 0;
        for (Edge edge : graph.getEdges()) {
            if (edge.contains(idRoot)) {
                if (edge.getNodeFrom().getId() == idRoot) {
                    scalarNodes.add(edge.getNodeTo());
                } else {
                    scalarNodes.add(edge.getNodeFrom());
                }
            }
        }

        Object [] randomValues = new Object[scalarNodes.size()];

        for (int i = 0; i < scalarNodes.size(); i++) {
            double random = (Math.random() * 20000) - 10000;
            if (scalarNodes.get(i).getCType() instanceof CDouble) {
                randomValues[i] = random;
            } else if (scalarNodes.get(i).getCType() instanceof CInteger) {
                randomValues[i] = (int)random;
            } else {
                randomValues[i] = (char)random;
            }
        }

        return randomValues;
    }

    private Object [][] generateRandomArrayValues (int numberOfArray, int numberOfValue) {
        Object [][] ramdomValues = new Object[numberOfArray][numberOfValue];

        for (int i = 0; i < numberOfArray; i++) {
            for (int j = 0; j < numberOfValue; j++) {
                ramdomValues[i][j] = (int)(Math.random() * 100);
            }
        }

        return ramdomValues;
    }

    private Object [] generateRandomPointer (int numberOfPointer, int numberOfArray) {
        Object [] ramdomValues = new Object[numberOfPointer];

        for (int i = 0; i < numberOfPointer; i++) {
            ramdomValues[i] = (int)(Math.random() * numberOfArray);
        }

        return ramdomValues;
    }
}
