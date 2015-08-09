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

import jmetal.core.*;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveComparator;
import jmetal.util.parallel.IParallelEvaluator;
import jmetal.util.wrapper.XParam;
import jmetal.util.wrapper.XParamArray;

import java.util.Comparator;
import java.util.List;

/**
 * A multithreaded generational genetic algorithm
 */

public class AVM extends Algorithm {
	private static final long serialVersionUID = 720067051970162535L;
	
	
	private static final double EPSILON = 0.001;
	private static final double DELTA = 1;
	
	private Comparator<Solution> comparator;
	private int evaluations;
	private Solution solution;
	private double derivate;
	
	private double delta;
	private double epsilon;
	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 * @param evaluator
	 *            Parallel evaluator
	 */
	public AVM(Problem problem) {
		super(problem);
	}

	/**
	 * Runs the AVM algorithm.
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
	 * @throws jmetal.util.JMException
	 */
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		int maxEvaluations;
		
		comparator = new ObjectiveComparator(0); // Single objective comparator

		maxEvaluations = ((Integer) getInputParameter("maxEvaluations"))
				.intValue();
		
		if (getInputParameter("delta") != null)
			delta = ((Double) getInputParameter("delta")).doubleValue();
		else
			delta = DELTA;
		
		if (getInputParameter("epsilon") != null)
			epsilon = ((Double) getInputParameter("epsilon")).doubleValue();
		else
			epsilon = EPSILON;
		
		evaluations = 0;
		
		int strength = 0;

		boolean targetCovered = false;
		// Generations
		while (evaluations < maxEvaluations && !targetCovered) {
			//Creates a random solution
			solution = new Solution(problem_);
			XParamArray solutionModifier = new XParamArray(solution);
			problem_.evaluate(solution);
			evaluations++;
			
			int lastUpdatedKind = solutionModifier.kinds() - 1;
			int lastUpdatedVariable = solutionModifier.size(lastUpdatedKind) - 1;
			//In order to find local optima, it stores the last updated kind/variable
			while (lastUpdatedVariable == -1) {
				lastUpdatedKind--;
				lastUpdatedVariable = solutionModifier.size(lastUpdatedKind) - 1;
			}
			
			boolean localOptimum = false;
			int currentKind = 0;
			int currentVariable = -1; //It will become 0 in the first iteration
			double direction = 0;
			
			while (!localOptimum && evaluations < maxEvaluations && !targetCovered) {
				
				//If we are in a optimum local to the specific kind/variable...
				if (direction == 0) {
					strength = 0;
					//If update overflows the number of variables of this kind
					if (currentVariable+1 == solutionModifier.size(currentKind)) {
						currentVariable = 0; //Reset variable
						
						//If update overflows the number of kinds
						if (currentKind+1 == solutionModifier.kinds())
							currentKind = 0; //Reset kind
						else //If the update doesn't overflow the number of kinds
							currentKind++; //Normally increments the kind
						
						//Searches for the kind with at least a variable
						while (solutionModifier.size(currentKind) == 0) {
							currentKind++;
							if (currentKind == solutionModifier.kinds())
								currentKind = 0;
						}
						
					} else //If the update doesn't overflow the number of variables
						currentVariable++; //Normally increments the variable
					
					//Gets the direction of update (minus or plus). Besides, it updates the solution.
					direction = this.getDirection(solution, currentKind, currentVariable);
					
					if (direction == 0.0) {
						if (solution.getObjective(0) == 0.0)
							targetCovered = true;
						if (lastUpdatedKind == currentKind && lastUpdatedVariable == currentVariable)
							localOptimum = true;
						
						continue;
					}
				}
				
				//Updates the last updated kind/variable to the current ones
				lastUpdatedKind = currentKind;
				lastUpdatedVariable = currentVariable;
				
				//Creates a new solution, updated toward the specified direction.
				Solution newSolution = this.newSolution(solution, currentKind, currentVariable, direction, strength);
				
				if (comparator.compare(newSolution, solution) == -1) {
					solution = newSolution;
					strength++;
				} else {
					direction = 0;
				}
			}
		} // while

		SolutionSet resultPopulation = new SolutionSet(1);
		resultPopulation.add(solution);

		System.out.println("Evaluations: " + evaluations);
		return resultPopulation;
	} // execute
	
	private Solution newSolution(
			Solution pSolution, 
			int currentKind, int currentVariable,
			double pDirection,
			int pStength)
					throws JMException {
		Solution solution = new Solution(pSolution);
		XParamArray solutionModifier = new XParamArray(solution);
		
		double updateValue = solutionModifier.getValue(currentKind, currentVariable);
		double difference = this.derivate * this.delta * pDirection * Math.pow(1.3, pStength);
		updateValue = updateValue + difference;
		
		if (updateValue > solutionModifier.getUpperBound(currentKind, currentVariable))
			updateValue = solutionModifier.getUpperBound(currentKind, currentVariable);
		else if (updateValue < solutionModifier.getLowerBound(currentKind, currentVariable))
			updateValue = solutionModifier.getLowerBound(currentKind, currentVariable);
		
		
		solutionModifier.setValue(currentKind, currentVariable, updateValue);
		
		problem_.evaluate(solution);
		evaluations++;
		
		this.derivate = this.derivate(solution.getObjective(0), pSolution.getObjective(0), Math.abs(difference));
		
		return solution;
	}
	
	//TODO Adaptive epsilon based on the branch distance value (denormalization required)
	private double getDirection(Solution pSolution, int pCurrentKind, int pCurrentVariable) throws JMException {
		Solution solutionPlus = new Solution(pSolution);
		Solution solutionMinus = new Solution(pSolution);
		
		XParamArray modifierPlus = new XParamArray(solutionPlus);
		XParamArray modifierMinus = new XParamArray(solutionMinus);
		double currentValue = modifierPlus.getValue(pCurrentKind, pCurrentVariable);
		modifierPlus.setValue(pCurrentKind, pCurrentVariable, currentValue + this.epsilon);
		modifierMinus.setValue(pCurrentKind, pCurrentVariable, currentValue - this.epsilon);
		
		problem_.evaluate(solutionPlus);
		problem_.evaluate(solutionMinus);
		evaluations += 2;
		
		if (comparator.compare(solutionPlus, solutionMinus) < 0) { //if solPlus < solMinus
			if (comparator.compare(solutionPlus, pSolution) < 0) { //if solPlus < solution
				this.solution = solutionPlus;
				
				this.derivate = this.derivate(solutionPlus.getObjective(0), pSolution.getObjective(0), this.epsilon);
				return 1;
			} else
				return 0;
		} else {
			if (comparator.compare(solutionMinus, pSolution) < 0) {
				this.solution = solutionMinus;
				
				this.derivate = this.derivate(solutionMinus.getObjective(0), pSolution.getObjective(0), this.epsilon);
				return -1;
			} else
				return 0;
		}
	}
	
	private double derivate(double a, double b, double dx) {
		double bdA = this.denormalize(a - Math.floor(a));
		double bdB = this.denormalize(b - Math.floor(b));
		
		return Math.abs(bdA - bdB)/dx;
	}
	
	private double denormalize(double x) {
		return x/(1 - x);
	}
} // pgGA
