package it.unisa.ocelot.genetic.algorithms;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import it.unisa.ocelot.genetic.OcelotAlgorithm;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.comparators.FitnessComparator;
import jmetal.util.comparators.ObjectiveComparator;
import jmetal.util.parallel.IParallelEvaluator;

public class CDG_GA extends OcelotAlgorithm {

	private static final long serialVersionUID = 7006139362097837208L;

	IParallelEvaluator parallelEvaluator_;
	private int no_evaluations;

	public CDG_GA(Problem problem, IParallelEvaluator parallelEvaluator, List<Graph> graphList) {
		super(problem, graphList);
		parallelEvaluator_ = parallelEvaluator;
		no_evaluations = 0;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		int populationSize;
		int maxEvaluations;
		int evaluations;

		SolutionSet population;
		SolutionSet offspringPopulation;
		// solutions that reach target
		SolutionSet achievedPopulation;
		Solution bestFitnessSolution = null;

		Operator mutationOperator;
		Operator crossoverOperator;
		Operator selectionOperator;

		Comparator comparator;
		comparator = new ObjectiveComparator(0); // Single objective comparator
		Comparator fitnessComparator = new FitnessComparator();

		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

		boolean bestAchieved = false;

		// Initialize the variables
		population = new SolutionSet(populationSize);
		offspringPopulation = new SolutionSet(populationSize);
		achievedPopulation = new SolutionSet(populationSize);

		evaluations = 0;

		// Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");

		// Create the initial solutionSet
		Solution newSolution;
		for (int i = 0; i < populationSize; i++) {
			newSolution = new Solution(problem_);
			// parallelEvaluator_.addSolutionForEvaluation(newSolution);
			problem_.evaluate(newSolution);
			evaluations++;
			population.add(newSolution);
		}

		population.sort(comparator);

		double minFitness = Double.MAX_VALUE;
		
		while (evaluations < maxEvaluations && !bestAchieved) {
			// Copy the best two individuals to the offspring population
			offspringPopulation.add(new Solution(population.get(0)));
			offspringPopulation.add(new Solution(population.get(1)));

			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize / 2) - 1; i++) {
				// if (evaluations < maxEvaluations) {
				// stochastic universal sampling
				population.sort(comparator);

				// List<Solution> listOfParents = (List<Solution>)
				// selectionOperator
				// .execute(population);
				// parents[0] = listOfParents.get(0);
				// parents[1] = listOfParents.get(1);
				parents[0] = (Solution) selectionOperator.execute(population);
				parents[1] = (Solution) selectionOperator.execute(population);

				// discrete recombination
				Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);

				// mutation
				mutationOperator.execute(offSpring[0]);
				mutationOperator.execute(offSpring[1]);

				problem_.evaluate(offSpring[0]);
				problem_.evaluate(offSpring[1]);
				offspringPopulation.add(offSpring[0]);
				offspringPopulation.add(offSpring[1]);
				evaluations += 2;
				// } // if
			} // for


			Iterator<Solution> offSpringIterator = offspringPopulation.iterator();
			while (offSpringIterator.hasNext()) {
				Solution current = offSpringIterator.next();
				double currentFitness = current.getFitness();

				if (current.getObjective(0) == 0 && currentFitness < minFitness) {
					this.algorithmStats.setStat("UpdateFitness", currentFitness);
					bestFitnessSolution = new Solution(current);
					minFitness = currentFitness;
					this.no_evaluations = evaluations;
					if (currentFitness == 0)
						bestAchieved = true;
				}
			}

			// The offspring population becomes the new current population
			population.clear();
			for (int i = 0; i < populationSize; i++) {
				population.add(offspringPopulation.get(i));
			}
			offspringPopulation.clear();
		} // while
		
		this.algorithmStats.setEvaluations(evaluations);

		SolutionSet bestFitnessResult = new SolutionSet(1);

		if (bestFitnessSolution == null) {
			this.algorithmStats.setStat("NullSolution", true);
			population.sort(comparator);
			bestFitnessResult.add(population.get(0));
			no_evaluations = evaluations;
		} else {
			bestFitnessResult.add(bestFitnessSolution);
		}
		return bestFitnessResult;
	}

	public int getNo_evaluations() {
		return this.no_evaluations;
	}

}
