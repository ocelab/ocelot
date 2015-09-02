package it.unisa.ocelot.genetic.settings;

import java.util.HashMap;
import java.util.List;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.operators.crossover.SBXGenericCrossover;
import jmetal.operators.mutation.GenericPolynomialMutation;
import jmetal.operators.mutation.PolynomialMutationParams;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;
import jmetal.util.parallel.IParallelEvaluator;
import jmetal.util.parallel.MultithreadedEvaluator;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.CDG_GA;
import it.unisa.ocelot.genetic.StandardSettings;
import it.unisa.ocelot.genetic.algorithms.GeneticAlgorithm;
import it.unisa.ocelot.genetic.edges.CDG_BasedProblem;

public class GASettings extends StandardSettings {
	public GASettings(Problem pProblem) {
		super(pProblem);
	}
	
	public GASettings(Problem pProblem, ConfigManager pConfig) {
		super(pProblem, pConfig);
	}
	
	public Algorithm configure(Algorithm algorithm) throws JMException {
        Operator selection;
        Operator crossover;
        Operator mutation;
        
        HashMap<String, Object> parameters;
        
        algorithm.setInputParameter("populationSize", populationSize);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations);

        // Mutation and Crossover Permutation codification
        parameters = new HashMap<String, Object>();
        parameters.put("probability", crossoverProbability);
        //crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);
        crossover = new SBXGenericCrossover(parameters);

        parameters = new HashMap<String, Object>();
        parameters.put("probability", mutationProbability);
        if (!this.useMetaMutator) {
	        mutation = new PolynomialMutationParams(parameters);
        } else {
            parameters.put("realOperator", new PolynomialMutationParams(parameters));
            parameters.put("metaMutationProbability", 0.001);
            List<Double> mutationElements = this.numericConstants;
            parameters.put("mutationElements", mutationElements);
            mutation = new GenericPolynomialMutation(parameters);
        }

        // Selection Operator 
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        return algorithm;
	}
	
	public Algorithm configure() throws JMException {
		Algorithm algorithm;
		
		IParallelEvaluator parallelEvaluator = new MultithreadedEvaluator(threads);
		
		// Creating the problem
        if (problem_ instanceof CDG_BasedProblem)
        	algorithm = new CDG_GA(problem_, parallelEvaluator);
        else 
        	algorithm = new GeneticAlgorithm(problem_, parallelEvaluator);
        
		return configure(algorithm);
    }
}
