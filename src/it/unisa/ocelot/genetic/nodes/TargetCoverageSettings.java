package it.unisa.ocelot.genetic.nodes;

import it.unisa.ocelot.conf.ConfigManager;

import java.util.HashMap;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.pgGA;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;
import jmetal.util.parallel.IParallelEvaluator;
import jmetal.util.parallel.MultithreadedEvaluator;

public class TargetCoverageSettings extends Settings {
    private int populationSize;
    private int maxEvaluations;
    private double mutationProbability;
    private double crossoverProbability;
    private int threads;
    private boolean debug;
    
	public TargetCoverageSettings(TargetCoverageProblem pProblem) {
		super();
		
		this.problem_ = pProblem;
				
		populationSize = 100;
        maxEvaluations = 25000;
        mutationProbability = 1.0 / problem_.getNumberOfVariables();
        crossoverProbability = 0.9;
        threads = 1;
	}
	
	public TargetCoverageSettings(TargetCoverageProblem pProblem, ConfigManager pConfig) {
		this(pProblem);
		
		try {
			populationSize = pConfig.getPopulationSize();
		} catch (NumberFormatException e) {}
		
		try {
			maxEvaluations = pConfig.getMaxEvolutions();
		} catch (NumberFormatException e) {}
		
		try {
			crossoverProbability = pConfig.getCrossoverProbability();
		} catch (NumberFormatException e) {}
		
		try {
			threads = pConfig.getThreads();
		} catch (NumberFormatException e) {}
	}
	
	public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Operator selection;
        Operator crossover;
        Operator mutation;
        
        HashMap<String, Double> parameters;

        IParallelEvaluator parallelEvaluator = new MultithreadedEvaluator(threads);

        // Creating the problem
        algorithm = new pgGA(problem_, parallelEvaluator);
        
        algorithm.setInputParameter("populationSize", populationSize);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations);

        // Mutation and Crossover Permutation codification
        parameters = new HashMap<String, Double>();
        parameters.put("probability", crossoverProbability);
        crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

        parameters = new HashMap<String, Double>();
        parameters.put("probability", mutationProbability);
        mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

        // Selection Operator 
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        return algorithm;
    }
}
