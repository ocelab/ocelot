package it.unisa.ocelot.genetic.operators;

import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;
import it.unisa.ocelot.genetic.encoding.graph.ScalarNode;
import jmetal.core.Solution;
import jmetal.operators.mutation.Mutation;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class IndexMutation extends Mutation {
    private static int NUMBER_OF_MUTANT = 1;
    private Double probability = null;
    private HashMap<Integer, Boolean> scalarNodeMap;

    public IndexMutation(HashMap<String, Object> parameters, List<Graph> graphList) {
        super(parameters);

        this.scalarNodeMap = new HashMap<>();
        for (Node node : graphList.get(0).getNodes()) {
            if (node instanceof ScalarNode) {
                this.scalarNodeMap.put(node.getId(), true);
            } else {
                this.scalarNodeMap.put(node.getId(), false);
            }
        }

        if (parameters.get("mutationProbability") != null)
            probability = (Double) parameters.get("mutationProbability");
    }

    public void doMutation (Double probability, Solution solution) throws JMException {
        double random = Math.random();

        if (random < probability) {
            List<Integer> index = new ArrayList<>();
            for (int i = 0; i < solution.getDecisionVariables().length; i++) {
                if (scalarNodeMap.get(i))
                    index.add(i);
            }

            Collections.shuffle(index);

            for (int i = 0; i < NUMBER_OF_MUTANT; i++) {
                int randomIndex = (int)(Math.random() * 100);
                solution.getDecisionVariables()[index.get(i)].setValue(randomIndex);
            }
        }
    }

    @Override
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        doMutation(probability, solution);

        return solution;
    }
}
