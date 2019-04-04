package it.unisa.ocelot.genetic.many_objective;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.algorithms.MOSA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.unisa.ocelot.genetic.encoding.graph.Graph;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.experiments.Settings;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.ConstantMetaMutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.mutation.UniformMutation;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;
import jmetal.util.MOSADominanceComparator;

/**
 * Settings for the Branch Coverage problem solved using the MOSA algorithm
 * proposed by Panichella et al in
 * "Reformulating Branch Coverage as a Many-Objective Optimization Problem"
 * 
 * @author giograno
 *
 */
public class MOSABranchCoverageSettings extends Settings {
	private int populationSize;
	private int maxEvaluations;
	private double mutationProbability;
	private double crossoverProbability;

	private List<LabeledEdge> target;

	private List<Double> numericConstants;

	/**
	 * Constructor with default parameters
	 * 
	 * @param problem
	 *            MOSABranchCoverageProblem to configure
	 */
	public MOSABranchCoverageSettings(MOSABranchCoverageProblem problem) {
		super();
		this.problem_ = problem;
		this.target = new ArrayList<>(problem.getTargetBranches());

		populationSize = 100;
		maxEvaluations = 25000;
		mutationProbability = 1.0 / problem_.getNumberOfVariables();
		crossoverProbability = 0.9;
//		threads = 1;
	}

	/**
	 * Constructor with specified parameters
	 * 
	 * @param problem
	 *            MOSABranchCoverageProblem to configure
	 * @param config
	 *            configuration taken by config.properties
	 */
	public MOSABranchCoverageSettings(MOSABranchCoverageProblem problem,
			ConfigManager config) {
		this(problem);

		try {
			populationSize = config.getPopulationSize();
		} catch (NumberFormatException e) {
		}

		try {
			maxEvaluations = config.getMaxEvaluations();
		} catch (NumberFormatException e) {
		}

		try {
			crossoverProbability = config.getCrossoverProbability();
		} catch (NumberFormatException e) {
		}

		try {
			mutationProbability = config.getMutationProbability();
		} catch (NumberFormatException e) {
		}
	}

	@Override
	public Algorithm configure() throws JMException {
		Algorithm algorithm;
		Operator selection;
		Operator crossover;
		Operator mutation;

		HashMap<String, Object> parameters;

		StandardProblem sp = (StandardProblem)problem_;
		List<Graph> graphList = sp.getGraphList();

		// Creating the problem
		algorithm = new MOSA((MOSABranchCoverageProblem) problem_, target, graphList);

		algorithm.setInputParameter("populationSize", populationSize);
		algorithm.setInputParameter("maxEvaluations", maxEvaluations);

		// Mutation and Crossover Permutation codification
		parameters = new HashMap<String, Object>();
		parameters.put("probability", crossoverProbability);
		crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover",
				parameters);

		parameters = new HashMap<String, Object>();


		parameters.put("probability", mutationProbability);
		parameters.put("realOperator", MutationFactory.getMutationOperator(
				"PolynomialMutation", parameters));
		parameters.put("metaMutationProbability", mutationProbability / 4);
		List<Double> mutationElements = this.numericConstants;
		parameters.put("mutationElements", mutationElements);
		mutation = new ConstantMetaMutation(parameters);

		// Selection Operator
//		parameters = null;
		
		/* code for custom selection */
		parameters = new HashMap<String, Object>();
		parameters.put("comparator", new MOSADominanceComparator(target));
		selection = SelectionFactory.getSelectionOperator("BinaryTournament2",
				parameters);

		// Add the operators to the algorithm
		algorithm.addOperator("crossover", crossover);
		algorithm.addOperator("mutation", mutation);
		algorithm.addOperator("selection", selection);

		return algorithm;
	}

	/**
	 * Get numeric constants literals extracted by target function
	 * 
	 * @return a List of Double numeric constants
	 */
	public List<Double> getNumericConstants() {
		return numericConstants;
	}

	/**
	 * Set numeric constants literals extracted by target function
	 * 
	 * @param numericConstants
	 *            literals constants
	 */
	public void setNumericConstants(List<Double> numericConstants) {
		this.numericConstants = numericConstants;
	}
}
