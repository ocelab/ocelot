package jmetal.encodings.solutionType;

import it.unisa.ocelot.genetic.StandardProblem;
import jmetal.core.Problem;
import jmetal.core.SolutionType;
import jmetal.core.Variable;
import jmetal.encodings.variable.ArrayParameters;
import jmetal.encodings.variable.ArrayReal;


public class ArrayParametersSolutionType extends SolutionType {
	/**
	 * Constructor
	 * @param problem Problem to solve
	 */
	public ArrayParametersSolutionType(Problem problem) {
		super(problem) ;
	}
	
	/**
	 * Creates the variables of the solution
	 */
	public Variable[] createVariables() {
		Variable[] variables;
		
		if (problem_ instanceof StandardProblem) {
			StandardProblem problem = (StandardProblem)problem_;
			int numberOfArrays = problem.getNumberOfArrays();
			
			variables = new Variable[2 + numberOfArrays];
			variables[0] = new ArrayParameters(problem_.getNumberOfVariables() - problem.getNumberOfArrays() - 1, problem_, 0);
			for (int i = 1; i <= numberOfArrays; i++) {
				variables[i] = new ArrayParameters(problem.getArraySize(), problem_, i);
			}
			variables[numberOfArrays+1] = new ArrayParameters(numberOfArrays, problem_, numberOfArrays+1);
		} else {
			variables = new Variable[1];
			variables[0] = new ArrayParameters(problem_.getNumberOfVariables(), problem_, 0);
		}
			
    return variables ;
	} // createVariables
	
	/**
	 * Copy the variables
	 * @param vars Variables
	 * @return An array of variables
	 */
	public Variable[] copyVariables(Variable[] vars) {
		Variable[] variables = new Variable[vars.length];
		
		for (int i = 0; i < vars.length; i++)
			variables[i] = vars[i].deepCopy();
		
//		if (problem_ instanceof StandardProblem) {
//			StandardProblem problem = (StandardProblem)problem_;
//			int numberOfArrays = problem.getNumberOfArrays();
//			
//			variables = new Variable[1 + numberOfArrays];
//			variables[0] = vars[0].deepCopy();
//			for (int i = 1; i <= numberOfArrays; i++) {
//				variables[i] = vars[i].deepCopy();
//			}
//		} else {
//			variables = new Variable[1];
//			variables[0] = vars[0].deepCopy();
//		}
		
		return variables ;
	} // copyVariables
} // ArrayRealSolutionType
