package jmetal.operators.localSearch;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.genetic.SerendipitousAlgorithm;
import it.unisa.ocelot.genetic.StandardProblem;

import java.util.HashMap;
import java.util.Set;

import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.util.JMException;
import jmetal.util.wrapper.XParam;
import jmetal.util.wrapper.XParamArray;

/**
 * Operator for the Alternating Variable Method local search heuristic
 * N.b: a memetic algorithm does not provide a restart to avoid local optima
 * 
 * @author giograno
 *
 */
public class AlternativeVariableMethodSearch extends Operator implements SerendipitousAlgorithm<LabeledEdge> {

	private static final int backwardMove = -1;
	private static final int forwardMove = 1;
	private static final int localOptima = 0;
	
	// AVM parameters
	private static final double initialMove = 0.1;
	private static final double repeatBase = 2;
	private static final int precision = 1;

	private static final long serialVersionUID = 1L;

	private StandardProblem problem;
	private int objectiveID; // the ID for the object in the fitness vector
	private int maxEvaluations; 
	private int evaluations; // evaluations actually consumed
	private int moveNoX;  // the number of moves toward a direction

	private BestSolution bestSolution; // best solution by the algorithm

	public static class AVMBuilder {
		// Required parameters
		private StandardProblem problem;
		private int maximumEvaluations;
		// Optional parameters
		private HashMap<String, Object> parameters = null;
		private int objectiveID = 0;
		private int currentEvaluations = 0;  // zero by default

		/**
		 * Constructor of AVM Builder
		 * @param problem problem to solve
		 * @param maxEvaluations maximum evaluations
		 */
		public AVMBuilder (Problem problem, int maxEvaluations) { 
			if (problem instanceof StandardProblem)
				this.problem = (StandardProblem)problem;
			else
				throw new RuntimeException("Non standard problem used with AVM algorithm.");
			this.maximumEvaluations = maxEvaluations;	 
		}
		
		public AVMBuilder consumedEvaluations(int evaluations) 
			{ this.currentEvaluations = evaluations;	return this; }
		
		public AVMBuilder parameters(HashMap<String, Object> parameters) 
		 	{ this.parameters = parameters;	return this; }
		
		public AVMBuilder objectiveId(LabeledEdge target) 
	 		{ this.objectiveID = target.getObjectiveID();	return this; }
				
		public AlternativeVariableMethodSearch build() {
			if (problem == null) 
			    throw new IllegalStateException("You need a problem to solve");
			if (maximumEvaluations == 0)
				throw new IllegalStateException("You need a maximum iteration number");
			
			return new AlternativeVariableMethodSearch(this);
		}

	}

	/**
	 * Unique constructor for the AVM operator
	 * @param builder a instance of <code>AVMBuilder</code>
	 */
	public AlternativeVariableMethodSearch(AVMBuilder builder) {
		super(builder.parameters);
		this.problem = builder.problem;
		this.objectiveID = builder.objectiveID;
		this.maxEvaluations = builder.maximumEvaluations;
		this.evaluations = builder.currentEvaluations;
		this.moveNoX = 0; 
	}

	@Override
	public Object execute(Object solution) throws JMException {
		Solution currentSolution = new Solution((Solution)solution);
		
		// Set initial solution as the currently best
		this.bestSolution = new BestSolution(currentSolution, currentSolution.getObjective(this.objectiveID));
		
		boolean isTargetCovered = false;
		
		while (evaluations < maxEvaluations && !isTargetCovered) {
		
			XParamArray bestGAModifier = new XParamArray(this.bestSolution.solution);
			
			// Store the last updated solution element
			int lastUpdatedArray = bestGAModifier.kinds() - 1;
			int lastUpdatedVariable = bestGAModifier.size(lastUpdatedArray) - 1;
			
			// Remember the last updated variable 
			while (lastUpdatedVariable == -1) {
				lastUpdatedArray--;
				lastUpdatedVariable = bestGAModifier.size(lastUpdatedArray) - 1;
			}
			
			boolean isLocalOptimum = false;
			int currentArray = 0;
			int currentVariable = -1;
			int direction = localOptima;
			
			while (!isLocalOptimum && this.evaluations < this.maxEvaluations && !isTargetCovered) {
			
				if (direction == localOptima) {
					
					if (currentVariable + 1 == bestGAModifier.size(currentArray)) {
						currentVariable = 0; // Restart from the first variable
						
						if (currentArray + 1 == bestGAModifier.kinds())
							currentArray = 0; // Restart from the first array
						else 
							currentArray++; // Look for the next array
						
						// Go to the next valid array (at least one variable)
						while (bestGAModifier.size(currentArray) == 0) {
							currentArray++;
							if (currentArray == bestGAModifier.kinds())
								currentArray = 0;
						}
						
					} else // the array is not finished yet 
						currentVariable++; // becomes 0 at first iteration
					
					// Gets the direction (and updates if a good direction is found)
					direction = this.getDirection(bestSolution, currentArray, currentVariable);
					
					if (direction == localOptima) {
						if (bestSolution.solution.getObjective(this.objectiveID) == 0.0)
							isTargetCovered = true;
						if (lastUpdatedArray == currentArray && lastUpdatedVariable == currentVariable)
							isLocalOptimum = true;
						
						continue;
					} // end looking for local optimum or for a solution
				}
				
				// Iterations toward a direction
				lastUpdatedArray = currentArray;
				lastUpdatedVariable = currentVariable;
				
				// Create a new solution 
				BestSolution improvedSolution = this.improveSolutionTowardADirection(bestSolution, 
						currentArray, currentVariable, direction);
				
				if (improvedSolution.branchDistance < bestSolution.branchDistance) {
					bestSolution.solution = improvedSolution.solution;
					bestSolution.branchDistance = improvedSolution.branchDistance;
					moveNoX++;
				} else {
					direction = localOptima;
					moveNoX = 0;
				}
				
				// Check for solution covered
				if (bestSolution.solution.getObjective(this.objectiveID) == 0.0)
					isTargetCovered = true;
				
			} // end improvement for a variable
		} // main while
			
		return bestSolution.solution;
	}

	private int getDirection(BestSolution bestSolution, int array, int element) 
			throws JMException {
		Solution solutionUp = new Solution(bestSolution.solution);
		Solution solutionDown = new Solution(bestSolution.solution);
		
		XParamArray modifierUp = new XParamArray(solutionUp);
		XParamArray modifierDown = new XParamArray(solutionDown);
		
		double currentValue = modifierDown.getValue(array, element);
		
		// Check for bound overflow
		if (currentValue + initialMove > modifierUp.getUpperBound(array, element))
			modifierUp.setValue(array, element, currentValue);
		else
			modifierUp.setValue(array, element, currentValue + initialMove);
		
		// Check for bound underflow
		if (currentValue - initialMove < modifierDown.getUpperBound(array, element))
			modifierDown.setValue(array, element, currentValue);
		else 
			modifierDown.setValue(array, element, currentValue - initialMove);
		
		double branchDistanceUp = this.problem.evaluateWithBranchDistance(solutionUp);
		double branchDistanceDown = this.problem.evaluateWithBranchDistance(solutionDown);
		this.evaluations += 2;
		
		if (branchDistanceUp < branchDistanceDown) {
			// Compare with the current best solution
			if (branchDistanceUp < this.bestSolution.branchDistance) {
				// The solution up improves the best solution
				
				this.bestSolution.solution = solutionUp;
				this.bestSolution.branchDistance = branchDistanceUp;
				
				if (this.bestSolution.solution.getObjective(this.objectiveID) == 0.0)
					return localOptima; // Objective covered
				
				return backwardMove;
			} else 
				return localOptima;
		} else if (branchDistanceDown < branchDistanceUp) {
			if (branchDistanceDown < this.bestSolution.branchDistance) {
			
			this.bestSolution.solution = solutionDown;
			this.bestSolution.branchDistance = branchDistanceDown;
			
			if (this.bestSolution.solution.getObjective(this.objectiveID) == 0.0)
				return localOptima; // Objective covered
			
			return forwardMove;
			} else 
				return localOptima;
		} else
			return localOptima;
	}
	
	private BestSolution improveSolutionTowardADirection (BestSolution bestSolution, 
			int array, int variable, int direction) throws JMException {
		
		Solution solution = new Solution(bestSolution.solution);
		XParamArray modifier = new XParamArray(solution);
		
		double currentValue = modifier.getValue(array, variable);
		double move = Math.pow(repeatBase, moveNoX) * Math.pow(10, precision * -1) * direction;
		
		currentValue += move;
		
		if (currentValue > modifier.getUpperBound(array, variable))
			currentValue = modifier.getUpperBound(array, variable);
		else if (currentValue < modifier.getLowerBound(array, variable))
			currentValue = modifier.getLowerBound(array, variable);
		
		modifier.setValue(array, variable, currentValue);
		
		double branchDistance = problem.evaluateWithBranchDistance(solution);
		this.evaluations++;
			
		return new BestSolution(solution, branchDistance);
	}
		
	public int getEvaluations() {
		return evaluations;
	}

	@Override
	public Set<Solution> getSerendipitousSolutions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSerendipitousPotentials(Set<LabeledEdge> pPotentials) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Auxiliary class which store the best solution reached by the AVM
	 * algorithm
	 */
	class BestSolution {
		public Solution solution;
		public double branchDistance;
		
		public BestSolution (Solution solution, double branchDistance) {
			this.solution = solution;
			this.branchDistance = branchDistance;
		}
	}
}