package it.unisa.ocelot.genetic.algorithms;

//  pgGA.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//
//  Copyright (c) 2013 Antonio J. Nebro
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.genetic.OcelotAlgorithm;
import it.unisa.ocelot.genetic.SerendipitousAlgorithm;
import it.unisa.ocelot.genetic.SerendipitousProblem;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A multithreaded generational genetic algorithm
 */

public class GeneticAlgorithm extends OcelotAlgorithm implements SerendipitousAlgorithm<LabeledEdge> {
	private static final long serialVersionUID = -2679014653669190929L;
	
	private int no_evaluation;

	private Set<Solution> serendipitousSolutions;
	private Set<LabeledEdge> serendipitousPotentials;

	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 * @param evaluator
	 *            Parallel evaluator
	 */
	public GeneticAlgorithm(Problem problem) {
		super(problem);
		
		this.serendipitousSolutions = new HashSet<Solution>();
		this.serendipitousPotentials = new HashSet<LabeledEdge>();

		no_evaluation = 0;
	} // pgGA

	/**
	 * Runs the pgGA algorithm.
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
	 * @throws jmetal.util.JMException
	 */
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
		populationSize = ((Integer) getInputParameter("populationSize"))
				.intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations"))
				.intValue();

//		parallelEvaluator_.startEvaluator(problem_);

		// Initialize the variables
		population = new SolutionSet(populationSize);
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
					parents[0] = (Solution) selectionOperator
							.execute(population);
					parents[1] = (Solution) selectionOperator
							.execute(population);
					Solution[] offSpring = (Solution[]) crossoverOperator
							.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);
					solutions.add(offSpring[0]);
					solutions.add(offSpring[1]);
				} // if
			} // for


			for (Solution solution : solutions) {
				prepareSerendipitous();
				problem_.evaluate(solution);
				checkSerendipitous(solution);
				
				offspringPopulation.add(solution);
				if (solution.getObjective(0) == 0.0)
					targetCovered = true;
				evaluations++;
			}

			// The offspring population becomes the new current population
			population.clear();
			for (int i = 0; i < populationSize; i++) {
				population.add(offspringPopulation.get(i));
			}
			offspringPopulation.clear();
			population.sort(comparator);
		} // while


		// Return a population with the best individual
		SolutionSet resultPopulation = new SolutionSet(1);
		resultPopulation.add(population.get(0));

		this.algorithmStats.setEvaluations(evaluations);
		this.no_evaluation = evaluations;
		return resultPopulation;
	} // execute
	
	public int getNumberOfEvaluation(){
		return this.no_evaluation;
	}
	
	private void prepareSerendipitous() {
		if (problem_ instanceof SerendipitousProblem<?>) {
			SerendipitousProblem<LabeledEdge> problem = (SerendipitousProblem<LabeledEdge>)problem_;
			
			problem.setSerendipitousPotentials(this.serendipitousPotentials);
		}
	}
	
	private void checkSerendipitous(Solution solution) {
		if (problem_ instanceof SerendipitousProblem<?>) {
			SerendipitousProblem<LabeledEdge> problem = (SerendipitousProblem<LabeledEdge>)problem_;
			
			if (problem.getSerendipitousCovered().size() > 0) {
				this.serendipitousPotentials.removeAll(problem.getSerendipitousCovered());
				
				this.serendipitousSolutions.add(solution);
			}
		}
	}

	@Override
	public Set<Solution> getSerendipitousSolutions() {
		return this.serendipitousSolutions;
	}

	@Override
	public void setSerendipitousPotentials(Set<LabeledEdge> pPotentials) {
		this.serendipitousPotentials = pPotentials;
	}
	
} // pgGA
