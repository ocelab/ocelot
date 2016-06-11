package it.unisa.ocelot.genetic.algorithms;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.OcelotAlgorithm;
import it.unisa.ocelot.genetic.SerendipitousAlgorithm;
import it.unisa.ocelot.genetic.SerendipitousProblem;
import it.unisa.ocelot.genetic.StandardProblem;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveComparator;
import jmetal.util.wrapper.XParamArray;


/**
 * Alternating Variable Method.
 */

public class AVM extends OcelotAlgorithm implements SerendipitousAlgorithm<LabeledEdge>, SeedableAlgorithm {
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
	
	private Solution seededSolution;
	private SolutionSet lastPopulation;
	
	private Map<Integer, Map<Integer, Boolean>> discreteValues;
	
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
	
	@Override
	public void seedStartingPopulation(SolutionSet set, int keepNumber) {
		if (keepNumber != 0 && set != null)
			this.seededSolution = set.get(0);
	}
	
	@Override
	public SolutionSet getLastPopulation() {
		return this.lastPopulation;
	}

	/**
	 * Runs the AVM algorithm.
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
	 * @throws jmetal.util.JMException
	 */
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		try {
			CType[] types = (CType[])getInputParameter("parametersTypes");
			this.initializeDiscreteness(types);
		} catch (ClassNotFoundException e) {
		}
		
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
		
		if (this.seededSolution != null) {
			solutionBundle.solution = new Solution(this.seededSolution);
			this.prepareSerendipitous();
			solutionBundle.branchDistance = problem.evaluateWithBranchDistance(solutionBundle.solution);
			this.checkSerendipitous(solutionBundle.solution);
			evaluations++;
		}

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
			}
			dontRestart = false;
			
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
			boolean firstSelectionChange = false;
			
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
					} else
						firstSelectionChange = true;
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
					firstSelectionChange = false;
				} else {
					if (firstSelectionChange) {
						if (strength >= 0)
							strength -= 1;
						else
							strength *= 2;
					} else
						direction = 0;
				}
				
				if (solutionBundle.solution.getObjective(0) == 0.0)
					targetCovered = true;
			}
		} // while

		SolutionSet resultPopulation = new SolutionSet(1);
		this.lastPopulation = new SolutionSet(1);
		resultPopulation.add(solutionBundle.solution);
		this.lastPopulation.add(solutionBundle.solution);

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
		
		double roundEpsilon = Math.pow(10, Math.floor(Math.log10(epsilon)));
		
		updateValue = Math.floor(updateValue / roundEpsilon) * roundEpsilon; //Rounds to the epsilonth decimal
		
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
	
	private double getDirection(SolutionBundle pSolutionBundle, int pCurrentKind, int pCurrentVariable) throws JMException {
		double epsilon;
		
		if (this.isDiscrete(pCurrentKind, pCurrentVariable)) {
			epsilon = 1;
		} else {
			epsilon = this.epsilon;
		}
		
		Solution solutionPlus = new Solution(pSolutionBundle.solution);
		Solution solutionMinus = new Solution(pSolutionBundle.solution);
		
		XParamArray modifierPlus = new XParamArray(solutionPlus);
		XParamArray modifierMinus = new XParamArray(solutionMinus);
		double currentValue = modifierPlus.getValue(pCurrentKind, pCurrentVariable);
		if (currentValue+epsilon > modifierPlus.getUpperBound(pCurrentKind, pCurrentVariable)) {
			modifierPlus.setValue(pCurrentKind, pCurrentVariable, currentValue);
		} else
			modifierPlus.setValue(pCurrentKind, pCurrentVariable, currentValue + epsilon);
		
		if (currentValue-epsilon < modifierMinus.getLowerBound(pCurrentKind, pCurrentVariable)) {
			modifierMinus.setValue(pCurrentKind, pCurrentVariable, currentValue);
		} else
			modifierMinus.setValue(pCurrentKind, pCurrentVariable, currentValue - epsilon);
		
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
						epsilon);
				
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
						epsilon);
				
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
	
	private void initializeDiscreteness(CType[] types) throws ClassNotFoundException {
		Solution test = new Solution(problem_);
		
		XParamArray modifier = new XParamArray(test);
		
		this.discreteValues = new HashMap<>();
		int typeIndex = 0;
		for (int i = 0; i < modifier.kinds(); i++) {
			Map<Integer, Boolean> kindMap = new HashMap<Integer, Boolean>();
			
			for (int j = 0; j < modifier.size(i); j++) {
				if (i == modifier.kinds() - 1 && i > 0) {
					//Pointers are always discrete
					kindMap.put(j, true);
				} else {
					if (types[typeIndex].isDiscrete()) {
						System.out.println("Discrete");
						kindMap.put(j, true);
					} else
						kindMap.put(j, false);
					
					if (i == 0) {
						typeIndex++;
					}
				}
			}
			
			if (i != 0) {
				typeIndex++;
			}
			
			this.discreteValues.put(i, kindMap);
		}
	}
	
	private boolean isDiscrete(int pKind, int pVariable) {
		return this.discreteValues.get(pKind).get(pVariable);
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
