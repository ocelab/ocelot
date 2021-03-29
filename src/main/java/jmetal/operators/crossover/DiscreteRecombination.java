package jmetal.operators.crossover;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.util.Configuration;
import jmetal.util.PseudoRandom;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.JMException;
import jmetal.util.wrapper.XReal;

public class DiscreteRecombination extends Crossover {

	private static final long serialVersionUID = 1L;

	private Double crossoverProbability_ = null;

	// valid solutions for this operator
	@SuppressWarnings("rawtypes")
	private static final List VALID_TYPES = Arrays.asList(RealSolutionType.class,
			ArrayRealSolutionType.class);

	/**
	 * Creates a new instance of discrete recombination operator
	 * 
	 * @param parameters
	 */
	public DiscreteRecombination(HashMap<String, Object> parameters) {
		super(parameters);
		if (parameters.get("probability") != null)
			crossoverProbability_ = (Double) parameters.get("probability");
	}

	/**
	 * Execute the operation
	 * 
	 * @param object
	 *            An object containing an array of two parents
	 * @return An object containing the offSpring
	 */
	public Object execute(Object object) throws JMException {
		Solution[] parents = (Solution[]) object;

		if (parents.length != 2) {
			Configuration.logger_.severe("DiscreteRecombination needs two parents");
			Class<String> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		}

		if (!(VALID_TYPES.contains(parents[0].getType().getClass()) && VALID_TYPES
				.contains(parents[1].getType().getClass()))) {
			Configuration.logger_.severe("DiscreteRecombination: the solutions type "
					+ parents[0].getType() + " is not allowed with this operator");

			Class<String> cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		}

		Solution[] offSpring;
		offSpring = doCrossover(crossoverProbability_, parents[0], parents[1]);
		return offSpring;
	}

	private Solution[] doCrossover(double probability, Solution parent1, Solution parent2)
			throws JMException {

		Solution[] offSpring = new Solution[2];
		offSpring[0] = new Solution(parent1);
		offSpring[1] = new Solution(parent2);
		
		XReal xRealParent1 = new XReal(parent1) ;		
		XReal xRealParent2 = new XReal(parent2) ;		
		XReal xOffSpring1 = new XReal(offSpring[0]) ;
		XReal xOffSpring2 = new XReal(offSpring[1]) ;

		if (PseudoRandom.randDouble() < probability) {
			for (int i = 0; i < parent1.numberOfVariables(); i++) {
				for (int j = 0; j < offSpring.length; j++) {
					int choice = PseudoRandom.randInt(0, 1);
					double value = 0;
					if (choice == 0){
						value = xRealParent1.getValue(i);
					}
					else if (choice == 1)
						value = xRealParent2.getValue(i);
					else
						Configuration.logger_
								.severe("Something goes wrong in PseudoRandom function");

					if (j == 0)
						xOffSpring1.setValue(i, value);
					else if (j == 1)
						xOffSpring2.setValue(i, value);
				} // while-offspring
			}
		} // end-if

		return offSpring;
	}

}
