package it.unisa.ocelot.genetic.paths;

import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.FastPgGA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.pgGA;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.ConstantMetaMutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.mutation.UniformMutation;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;
import jmetal.util.parallel.IParallelEvaluator;
import jmetal.util.parallel.MultithreadedEvaluator;

public class PathCoverageSettings extends Settings {
    private int populationSize;
    private int maxEvaluations;
    private double mutationProbability;
    private double crossoverProbability;
    private int threads;
	private List<Double> numericConstants;
    
	public List<Double> getNumericConstants() {
		return numericConstants;
	}

	public void setNumericConstants(List<Double> numericConstants) {
		this.numericConstants = numericConstants;
	}

	public PathCoverageSettings(PathCoverageProblem pProblem) {
		super();
		
		this.problem_ = pProblem;
				
		populationSize = 100;
        maxEvaluations = 25000;
        mutationProbability = 1.0 / problem_.getNumberOfVariables();
        crossoverProbability = 0.9;
        threads = 1;
	}
	
	public PathCoverageSettings(PathCoverageProblem pProblem, ConfigManager pConfig) {
		this(pProblem);
		
		try {
			populationSize = pConfig.getPopulationSize();
		} catch (NumberFormatException e) {}
		
		try {
			maxEvaluations = pConfig.getMaxEvaluations();
		} catch (NumberFormatException e) {}
		
		try {
			crossoverProbability = pConfig.getCrossoverProbability();
		} catch (NumberFormatException e) {}
		
		try {
			mutationProbability = pConfig.getMutationProbability();
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
        
        HashMap<String, Object> parameters;

        IParallelEvaluator parallelEvaluator = new MultithreadedEvaluator(threads);

        // Creating the problem
        algorithm = new FastPgGA(problem_, parallelEvaluator);
        
        algorithm.setInputParameter("populationSize", populationSize);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations);

        // Mutation and Crossover Permutation codification
        parameters = new HashMap<String, Object>();
        parameters.put("probability", crossoverProbability);
        crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

        parameters = new HashMap<String, Object>();
        parameters.put("probability", mutationProbability);
        parameters.put("realOperator", MutationFactory.getMutationOperator("PolynomialMutation", parameters));
        parameters.put("metaMutationProbability", mutationProbability/4);
        List<Double> mutationElements = this.numericConstants;
        parameters.put("mutationElements", mutationElements);
        mutation = new ConstantMetaMutation(parameters);

        // Selection Operator 
        parameters = new HashMap<String, Object>();
        parameters.put("problem", this.problem_);
        parameters.put("populationSize", populationSize);
        selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        return algorithm;
    }
}
