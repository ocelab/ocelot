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
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.VariableTranslator;
import jmetal.core.*;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveComparator;
import jmetal.util.wrapper.XParamArray;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


/**
 * Alternating Variable Method.
 */

public class AVM extends OcelotAlgorithm implements SerendipitousAlgorithm<LabeledEdge> {
	private static final long serialVersionUID = 720067051970162535L;
	
	
	private static final double EPSILON = 0.001;
	private static final double DELTA = 1;
	
	private Comparator<Solution> comparator;
	private int evaluations;
	private SolutionBundle solutionBundle;
	private double derivate;
	
	private double delta;
	private double epsilon;
	
	private Set<Solution> serendipitousSolutions;
	private Set<LabeledEdge> serendipitousPotentials;
	
	private StandardProblem problem;
	
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
		
		if (problem instanceof StandardProblem)
			this.problem = (StandardProblem)problem;
		else
			throw new RuntimeException("Non standard problem used with AVM algorithm.");
		
		this.solutionBundle = new SolutionBundle(null, 0);
		this.serendipitousSolutions = new HashSet<>();
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
		
		boolean dontRestart = (solutionBundle.solution != null);
		// Generations
		while (evaluations < maxEvaluations && !targetCovered) {
			//Creates a random solution
			if (!dontRestart) {
				solutionBundle.solution = new Solution(problem_);
				
				this.prepareSerendipitous();
				solutionBundle.branchDistance = problem.evaluateWithBranchDistance(solutionBundle.solution);
				this.checkSerendipitous(solutionBundle.solution);
				dontRestart = false;
			}
			
			XParamArray solutionModifier = new XParamArray(solutionBundle.solution);
			
			
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
					direction = this.getDirection(solutionBundle, currentKind, currentVariable);
					
					if (direction == 0.0) {
						if (solutionBundle.solution.getObjective(0) == 0.0)
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
				SolutionBundle newSolution = this.newSolution(
						solutionBundle, 
						currentKind, currentVariable, 
						direction, strength);
				
				int comparison = comparator.compare(newSolution.solution, solutionBundle.solution);
				if (comparison == -1 || (comparison == 0 && newSolution.branchDistance < solutionBundle.branchDistance)) {
					solutionBundle.solution = newSolution.solution;
					solutionBundle.branchDistance = newSolution.branchDistance;
					strength++;
				} else {
					direction = 0;
				}
				
				if (solutionBundle.solution.getObjective(0) == 0.0)
					targetCovered = true;
			}
		} // while

		SolutionSet resultPopulation = new SolutionSet(1);
		resultPopulation.add(solutionBundle.solution);

		this.algorithmStats.setEvaluations(evaluations);
		return resultPopulation;
	} // execute
	
	
	private SolutionBundle newSolution(
			SolutionBundle pSolutionBundle,
			int currentKind, int currentVariable,
			double pDirection,
			int pStength)
					throws JMException {
		Solution solution = new Solution(pSolutionBundle.solution);
		XParamArray solutionModifier = new XParamArray(solution);
		
		double updateValue = solutionModifier.getValue(currentKind, currentVariable);
		double difference = this.derivate * this.delta * pDirection * Math.pow(1.3, pStength);
		if (Math.abs(difference) < epsilon)
			difference = epsilon*pDirection;
		
		updateValue = updateValue + difference;
		
		if (updateValue > solutionModifier.getUpperBound(currentKind, currentVariable))
			updateValue = solutionModifier.getUpperBound(currentKind, currentVariable);
		else if (updateValue < solutionModifier.getLowerBound(currentKind, currentVariable))
			updateValue = solutionModifier.getLowerBound(currentKind, currentVariable);
		
		double prevValue = solutionModifier.getValue(currentKind, currentVariable);
		solutionModifier.setValue(currentKind, currentVariable, updateValue);
		
		this.prepareSerendipitous();
		double branchDistance = problem.evaluateWithBranchDistance(solution);
		this.checkSerendipitous(solution);
		evaluations++;
				
		this.derivate = this.derivate(
				solution.getObjective(0), branchDistance, 
				pSolutionBundle.solution.getObjective(0), pSolutionBundle.branchDistance, 
				Math.abs(difference));
		
		return new SolutionBundle(solution, branchDistance);
	}
	
	//TODO Adaptive epsilon based on the branch distance value (denormalization required)
	private double getDirection(SolutionBundle pSolutionBundle, int pCurrentKind, int pCurrentVariable) throws JMException {
		Solution solutionPlus = new Solution(pSolutionBundle.solution);
		Solution solutionMinus = new Solution(pSolutionBundle.solution);
		
		XParamArray modifierPlus = new XParamArray(solutionPlus);
		XParamArray modifierMinus = new XParamArray(solutionMinus);
		double currentValue = modifierPlus.getValue(pCurrentKind, pCurrentVariable);
		if (currentValue+this.epsilon > modifierPlus.getUpperBound(pCurrentKind, pCurrentVariable))
			modifierPlus.setValue(pCurrentKind, pCurrentVariable, currentValue);
		else
			modifierPlus.setValue(pCurrentKind, pCurrentVariable, currentValue + this.epsilon);
		
		if (currentValue-this.epsilon < modifierMinus.getLowerBound(pCurrentKind, pCurrentVariable))
			modifierMinus.setValue(pCurrentKind, pCurrentVariable, currentValue);
		else
			modifierMinus.setValue(pCurrentKind, pCurrentVariable, currentValue - this.epsilon);
		
		this.prepareSerendipitous();
		double branchDistancePlus = problem.evaluateWithBranchDistance(solutionPlus);
		this.checkSerendipitous(solutionPlus);
		
		this.prepareSerendipitous();
		double branchDistanceMinus = problem.evaluateWithBranchDistance(solutionMinus);
		this.checkSerendipitous(solutionMinus);
		evaluations += 2;
				
		int comparation = comparator.compare(solutionPlus, solutionMinus);
		
		if (comparation < 0 || (comparation == 0 && branchDistancePlus < branchDistanceMinus)) { //if solPlus < solMinus
			int comparation2 = comparator.compare(solutionPlus, pSolutionBundle.solution);
			if (comparation2 < 0 || (comparation2 == 0 && branchDistancePlus < pSolutionBundle.branchDistance)) { //if solPlus < solution
				this.derivate = this.derivate(
						solutionPlus.getObjective(0), branchDistancePlus, 
						pSolutionBundle.solution.getObjective(0), pSolutionBundle.branchDistance,
						this.epsilon);
				
				this.solutionBundle.solution = solutionPlus;
				this.solutionBundle.branchDistance = branchDistancePlus;
				
				if (this.solutionBundle.solution.getObjective(0) == 0.0)
					return 0.0;
				
				return 1;
			} else
				return 0;
		} else if (comparation > 0 || (comparation == 0 && branchDistancePlus > branchDistanceMinus)) {
			int comparation2 = comparator.compare(solutionMinus, pSolutionBundle.solution);
			if (comparation2 < 0 || (comparation2 == 0 && branchDistanceMinus < pSolutionBundle.branchDistance)) {
				this.derivate = this.derivate(
						solutionMinus.getObjective(0), branchDistanceMinus,
						pSolutionBundle.solution.getObjective(0), pSolutionBundle.branchDistance,
						this.epsilon);
				
				this.solutionBundle.solution = solutionMinus;
				this.solutionBundle.branchDistance = branchDistanceMinus;
				
				if (this.solutionBundle.solution.getObjective(0) == 0.0)
					return 0.0;
				
				return -1;
			} else
				return 0;
		} else
			return 0;
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
	
	private double derivate(double a, double bdA, double b, double bdB, double dx) {
		double alA = Math.floor(a);
		double alB = Math.floor(b);
		
		if (alA == alB)
			return Math.abs(bdA - bdB)/dx;
		else
			return (alA - alB)/dx;
	}
} // pgGA

class SolutionBundle {
	public Solution solution;
	public double branchDistance;
	
	public SolutionBundle(Solution pSolution, double pBranchDistance) {
		this.solution = pSolution;
		this.branchDistance = pBranchDistance;
	}
}
