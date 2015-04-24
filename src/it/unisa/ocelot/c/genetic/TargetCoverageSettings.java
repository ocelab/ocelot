package it.unisa.ocelot.c.genetic;

import java.util.HashMap;
import java.util.Properties;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.pgGA;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;
import jmetal.util.parallel.IParallelEvaluator;
import jmetal.util.parallel.MultithreadedEvaluator;

public class TargetCoverageSettings extends Settings {
	public int populationSize_;
    public int maxEvaluations_;

    public double mutationProbability_;
    public double crossoverProbability_;

    public int threads_;
    
    private Properties defaultProperties;
    
	public TargetCoverageSettings(TargetCoverageProblem pProblem) {
		super();
		
		pProblem.setTarget(pProblem.getCFG().getStart().navigate(pProblem.getCFG()).goFlow().goFlow().goFalse().goFlow().goFlow().goFalse().goFlow().goFalse().goTrue().node());
		
		this.problem_ = pProblem;
		
		populationSize_ = 100;
        maxEvaluations_ = 25000;
        mutationProbability_ = 1.0 / problem_.getNumberOfVariables();
        crossoverProbability_ = 0.9;
        threads_ = 1;
        
        defaultProperties = new Properties();
		
		defaultProperties.setProperty("populationSize", String.valueOf(populationSize_));
		defaultProperties.setProperty("maxEvaluations", String.valueOf(maxEvaluations_));
		defaultProperties.setProperty("crossoverProbability", String.valueOf(crossoverProbability_));
		defaultProperties.setProperty("mutationProbability", String.valueOf(mutationProbability_));
		defaultProperties.setProperty("threads", String.valueOf(threads_));
	}
	
	@Override
	public Algorithm configure() throws JMException {		
		return configure(defaultProperties);
	}
	
	public Algorithm configure(Properties configuration) throws JMException {
        Algorithm algorithm;
        Operator selection;
        Operator crossover;
        Operator mutation;
        
        // Algorithm parameters
        int populationSize = Integer.parseInt(configuration.getProperty("populationSize", String.valueOf(this.populationSize_)));
        int maxEvaluations = Integer.parseInt(configuration.getProperty("maxEvaluations", String.valueOf(this.maxEvaluations_)));
        double crossoverProbability = Double.parseDouble(configuration.getProperty("crossoverProbability", String.valueOf(this.crossoverProbability_)));
        double mutationProbability = Double.parseDouble(configuration.getProperty("mutationProbability", String.valueOf(this.mutationProbability_)));
        int threads = Integer.parseInt(configuration.getProperty("threads", String.valueOf(this.threads_)));

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
