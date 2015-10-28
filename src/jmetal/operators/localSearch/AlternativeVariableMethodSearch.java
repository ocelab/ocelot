package jmetal.operators.localSearch;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;

import java.util.HashMap;

import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.util.JMException;
import jmetal.util.wrapper.XParamArray;

/**
 * Operator for the Alternating Variable Method local search heuristic
 * 
 * @author giograno
 *
 */
public class AlternativeVariableMethodSearch extends Operator {

	private final int backwardMove = -1;
	private final int forwardMove = 1;

	private static final long serialVersionUID = 1L;

	private Problem problem;
	private int objectiveID; // the ID for the object in the fitness vector
	private int maxEvaluations; 
	
	private double searchPattern; // initial move
	private boolean isTargetCovered;

	public static class AVMBuilder {
		// Required parameters
		private Problem problem;
		private int evaluations;
		// Optional parameters
		private HashMap<String, Object> parameters = null;
		private int objectiveID = 0;

		public AVMBuilder problem(Problem problem, int evaluations) 
			{ this.problem = problem; this.evaluations = evaluations;	return this; }
		
		public AVMBuilder parameters(HashMap<String, Object> parameters) 
		 	{ this.parameters = parameters;	return this; }
		
		public AVMBuilder objectiveId(LabeledEdge target) 
	 		{ this.objectiveID = target.getObjectiveID();	return this; }
				
		public AlternativeVariableMethodSearch build() {
			if (problem == null) 
			    throw new IllegalStateException("You need a problem to solve");
			if (evaluations == 0)
				throw new IllegalStateException("You need a maximum iteration number");
			
			return new AlternativeVariableMethodSearch(this);
		}

	}

	public AlternativeVariableMethodSearch(AVMBuilder builder) {
		super(builder.parameters);
		this.problem = builder.problem;
		this.objectiveID = builder.objectiveID;
		this.maxEvaluations = builder.evaluations;
		this.searchPattern = 0.1;
		this.isTargetCovered = false;
	}

	@Override
	public Object execute(Object solution) throws JMException {
		Solution bestGeneticAlgorithmSolution = (Solution) solution;
		XParamArray bestGAModifier = new XParamArray(bestGeneticAlgorithmSolution);

		int numberOfArrays = bestGAModifier.kinds();

		for (int array = 0; array < numberOfArrays; array++) {
			for (int element = 0; element < bestGAModifier.getNumberOfDecisionVariables(array); element++) {
				// try to find the local optimum moving a variable

				double[] backwardSearchResult = moveVariable(bestGeneticAlgorithmSolution, array,
						element, backwardMove);
				double[] forwardSearchResult = moveVariable(bestGeneticAlgorithmSolution, array,
						element, forwardMove);

				if (backwardSearchResult[1] <= forwardSearchResult[1])
					bestGAModifier.setValue(array, element, backwardSearchResult[0]);
				else
					bestGAModifier.setValue(array, element, forwardSearchResult[0]);

				problem.evaluate(bestGeneticAlgorithmSolution);

				if (this.isTargetCovered)
					return bestGeneticAlgorithmSolution;
			}
		}
		return bestGeneticAlgorithmSolution;
	}

	private double[] moveVariable(Solution solution, int arrayNumber, int element, int direction)
			throws JMException {
		Solution solutionToImprove = new Solution(solution);
		XParamArray solutionModifier = new XParamArray(solutionToImprove);

		boolean localOptimum = false;

		// array that store value and fitness
		double[] currentValueAndFitness = new double[2];
		currentValueAndFitness[0] = solutionModifier.getValue(arrayNumber, element);
		currentValueAndFitness[1] = solutionToImprove.getObjective(this.objectiveID);

		double[] newValueAndFitness = new double[2];

		while (!localOptimum && !this.isTargetCovered) {

			double move = direction * this.searchPattern;
			newValueAndFitness[0] = currentValueAndFitness[0] + move;

			// check the bounds
			if (newValueAndFitness[0] > solutionModifier.getUpperBound(arrayNumber, element))
				newValueAndFitness[0] = solutionModifier.getUpperBound(arrayNumber, element);
			else if (newValueAndFitness[0] < solutionModifier.getLowerBound(arrayNumber, element))
				newValueAndFitness[0] = solutionModifier.getLowerBound(arrayNumber, element);

			// update the value
			solutionModifier.setValue(arrayNumber, element, newValueAndFitness[0]);
			// evaluate the new solution
			problem.evaluate(solutionToImprove);

			newValueAndFitness[1] = solutionToImprove.getObjective(this.objectiveID);

			if (newValueAndFitness[1] == 0) {
				// target covered with AVM local search
				this.isTargetCovered = true;
				currentValueAndFitness[0] = newValueAndFitness[0];
				currentValueAndFitness[1] = newValueAndFitness[1];
			} else if (newValueAndFitness[1] < currentValueAndFitness[1]) {
				// solution improved
				currentValueAndFitness[0] = newValueAndFitness[0];
				currentValueAndFitness[1] = newValueAndFitness[1];
				updateSearchPattern();
			} else
				localOptimum = true;

		}
		restoreSearchPattern();
		return currentValueAndFitness;
	}

	private void updateSearchPattern() {
		this.searchPattern *= 2;
	}

	private void restoreSearchPattern() {
		this.searchPattern = 1;
	}
}