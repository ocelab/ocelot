package jmetal.operators.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

/**
 * This class implements a stochastic universal sampling operator
 * 
 * @author giograno
 *
 */
public class StochasticUniversalSampling extends Selection {

	private static final long serialVersionUID = 1L;
	public int number_of_individuals_ = 2;

	public StochasticUniversalSampling(HashMap<String, Object> parameters) {
		super(parameters);
	}

	/**
	 * Perform the operation
	 * 
	 * @param object
	 *            Object representing a solution
	 */
	public Object execute(Object object) throws JMException {
		SolutionSet population = (SolutionSet) object;

		double aggregateFitness = 0;

		// calculate the sum of all fitness values
		Iterator<Solution> solutions = population.iterator();
		while (solutions.hasNext()) {
			Solution currentSolution = solutions.next();
			aggregateFitness += currentSolution.getObjective(0);
		}

		// distance between pointers
		double distanceBetweenPointers = aggregateFitness / number_of_individuals_;

		// random number between 0 and distanceBetweenPointers
		double start = Math.random() * distanceBetweenPointers;

		List<Solution> parents = new ArrayList<>();
		int index = 0;

		double cumulativeExpectation = population.get(0).getObjective(0);
		for (int i = 0; i < number_of_individuals_; i++) {
			double pointer = start + i * distanceBetweenPointers;

			if (cumulativeExpectation >= pointer)
				parents.add(population.get(index));
			else {
				for (++index; index < population.size(); index++) {
					cumulativeExpectation += population.get(index).getObjective(0);
					if (cumulativeExpectation >= pointer) {
						parents.add(population.get(index));
						break;
					}
				}
			}
		}// end-for

		// solutions = population.iterator();
		// while (solutions.hasNext()) {
		// Solution currentSolution = solutions.next();
		//
		// cumulativeExpectation += currentSolution.getObjective(0)
		// / (aggregateFitness * number_of_individuals_);
		// while (cumulativeExpectation > start + index) {
		// parents.add(currentSolution);
		// index++;
		// }
		// }

		return parents;
	}
}
