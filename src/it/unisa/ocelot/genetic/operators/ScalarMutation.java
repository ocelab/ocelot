package it.unisa.ocelot.genetic.operators;

import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.ScalarNode;
import jmetal.core.Solution;
import jmetal.operators.mutation.Mutation;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/*
 * This
 */
public class ScalarMutation extends Mutation {
    private Double scalarMutationProbability = null;
    private Double numberOfMutants;
    private List<Graph> graphList;

    public ScalarMutation(HashMap<String, Object> parameters, List<Graph> graphList) {
        super(parameters);

        this.graphList = graphList;

        if (parameters.get("scalarMutationProbability") != null)
            scalarMutationProbability = (Double) parameters.get("scalarMutationProbability");

        if (parameters.get("numberOfMutants") != null)
            numberOfMutants = (Double) parameters.get("numberOfMutants");
    }

    public void doMutation(double probability, double numberOfMutants, Solution solution)
            throws JMException {

            double random = Math.random();
            if (random < scalarMutationProbability) {
                //Map: index scalar node -> reference graph's index
                HashMap<Integer, Integer> scalarNodeMap = new HashMap<>();
                Graph modelGraph = graphList.get(0);    //Generic graph


                for (int i = 0; i < solution.getDecisionVariables().length; i++) {
                    int indexOfGraph = (int) solution.getDecisionVariables()[i].getValue();

                    if (modelGraph.getNode(i) instanceof ScalarNode) {
                        scalarNodeMap.put(i, indexOfGraph);
                    }
                }

                List<Integer> indexOfScalarNodes = new ArrayList<>(scalarNodeMap.keySet());
                Collections.shuffle(indexOfScalarNodes);

                //Get the first "numberOfMutants" nodes from index list
                int sizeOfMutants = (int) (numberOfMutants * indexOfScalarNodes.size());

                for (int i = 0; i < sizeOfMutants; i++) {
                    int indexOfScalarNode = indexOfScalarNodes.get(i);
                    int indexOfGraph = scalarNodeMap.get(indexOfScalarNode);

                    double randomValue = (Math.random() * 200) - 100;
                    ((ScalarNode)graphList.get(indexOfGraph).getNodes().get(indexOfScalarNode)).setValue(randomValue);
                }
            }
    }

    @Override
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        doMutation(scalarMutationProbability, numberOfMutants, solution);

        return solution;
    }
}
