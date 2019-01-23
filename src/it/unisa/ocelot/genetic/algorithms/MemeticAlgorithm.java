package it.unisa.ocelot.genetic.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unisa.ocelot.genetic.encoding.graph.Graph;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.operators.localSearch.AlternativeVariableMethodSearch;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveComparator;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.genetic.SerendipitousAlgorithm;

/**
 * Memetic algorithm with include an AVM local search operator 
 * for the best candidate solution at the end of each 
 * @author giograno
 *
 */
public class MemeticAlgorithm extends GeneticAlgorithm implements
		SerendipitousAlgorithm<LabeledEdge> {
	private static final long serialVersionUID = -6964554472242168211L;

	@SuppressWarnings("unused")
	private int no_evaluation;

	@SuppressWarnings("unused")
	private Set<Solution> serendipitousSolutions;
	@SuppressWarnings("unused")
	private Set<LabeledEdge> serendipitousPotentials;

	private SolutionSet lastPopulation;
	private Problem problem;

	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public MemeticAlgorithm(Problem problem, List<Graph> graphList) {
		super(problem, graphList);

		this.problem = problem;

		this.serendipitousSolutions = new HashSet<Solution>();
		this.serendipitousPotentials = new HashSet<LabeledEdge>();

		this.lastPopulation = null;

		no_evaluation = 0;
	}

	/**
	 * Runs the Memetic Algorithm
	 * 
	 * @return a <code>SolutionSet</code>
	 */
	@SuppressWarnings({ "unused", "rawtypes" })
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		int populationSize;
		int maxEvaluations;
		int evaluations;
		int numberOfThreads;

		SolutionSet population;
		SolutionSet offspringPopulation;
		SolutionSet union;

		Operator mutationOperator;
		Operator crossoverOperator;
		Operator selectionOperator;

		Comparator comparator;
		comparator = new ObjectiveComparator(0); // Single objective comparator

		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

		// parallelEvaluator_.startEvaluator(problem_);

		// Initialize the variables. If this is an extra execution, it keeps
		// using the last population
		if (lastPopulation == null)
			population = new SolutionSet(populationSize);
		else
			population = lastPopulation;
		offspringPopulation = new SolutionSet(populationSize);

		evaluations = 0;

		// Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");

		// Create the initial solutionSet
		Solution newSolution;
		List<Solution> solutionList = new ArrayList<>();
		for (int i = 0; i < populationSize; i++) {
			newSolution = new Solution(problem_);
			solutionList.add(newSolution);
		}

		for (Solution solution : solutionList) {
			prepareSerendipitous();
			problem_.evaluate(solution);
			checkSerendipitous(solution);

			population.add(solution);
			evaluations++;
		}

		population.sort(comparator);

		boolean targetCovered = false;
		// Generations
		while (evaluations < maxEvaluations && !targetCovered) {
			// Copy the best two individuals to the offspring population
			offspringPopulation.add(new Solution(population.get(0)));
			offspringPopulation.add(new Solution(population.get(1)));

			Solution[] parents = new Solution[2];
			List<Solution> solutions = new ArrayList<>();
			for (int i = 0; i < (populationSize / 2) - 1; i++) {
				if (evaluations < maxEvaluations) {
					// obtain parents
					parents[0] = (Solution) selectionOperator.execute(population);
					parents[1] = (Solution) selectionOperator.execute(population);
					Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);
					solutions.add(offSpring[0]);
					solutions.add(offSpring[1]);
				} // if
			} // for

			AlternativeVariableMethodSearch avmLocalOperator = new AlternativeVariableMethodSearch.AVMBuilder(
					this.problem, maxEvaluations).consumedEvaluations(evaluations).build();
			for (Solution solution : solutions) {
				prepareSerendipitous();
				problem_.evaluate(solution);
				evaluations++;
				checkSerendipitous(solution);

				if (solution.getObjective(0) == 0.0)
					targetCovered = true;

				offspringPopulation.add(solution);
			}

			population.clear();
			offspringPopulation.sort(comparator);

			int i = !targetCovered ? 0 : 1;

			if (!targetCovered) {
				Solution bestCandidate = (Solution) avmLocalOperator.execute(offspringPopulation
						.get(i));
				int oldEvaluations = evaluations;
				evaluations = avmLocalOperator.getEvaluations();
//				System.out.println("Evaluations consumed during the AVM process = "
//						+ (evaluations - oldEvaluations));
				population.add(bestCandidate);
			}

			// The offspring becomes the new current population
			for (; i < populationSize; i++)
				population.add(offspringPopulation.get(i));
			offspringPopulation.clear();

		} // end GA cycle!

		// Return a population with the best individual
		SolutionSet resultPopulation = new SolutionSet(1);
		resultPopulation.add(population.get(0));

		this.lastPopulation = population;

		this.algorithmStats.setEvaluations(evaluations);
		this.no_evaluation = evaluations;
		return resultPopulation;
	}
}
