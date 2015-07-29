package it.unisa.ocelot.genetic;

import org.apache.commons.lang3.Range;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;

public abstract class StandardProblem extends Problem {
	private static final long serialVersionUID = -462606769605747252L;
	
	protected Class<Object>[] parameters;
	protected boolean debug;
	
	public StandardProblem(Class[] pParameters, Range<Double>[] pRanges) throws Exception {
		numberOfVariables_ = pParameters.length;
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;

		solutionType_ = new ArrayRealSolutionType(this);

		length_ = new int[numberOfVariables_];
		lowerLimit_ = new double[numberOfVariables_];
		upperLimit_ = new double[numberOfVariables_];
		for (int i = 0; i < numberOfVariables_; i++) {
			if (pRanges != null && pRanges.length > i && pRanges[i] != null) {
				lowerLimit_[i] = pRanges[i].getMinimum();
				upperLimit_[i] = pRanges[i].getMaximum();
			} else {
				if (pParameters[i] == Integer.class) {
					lowerLimit_[i] = Integer.MIN_VALUE;
					upperLimit_[i] = Integer.MAX_VALUE;
				} else {
					lowerLimit_[i] = Double.MIN_VALUE;
					upperLimit_[i] = Double.MAX_VALUE;
				}
			}
		}
		
		this.parameters = pParameters;
	}
	
	protected Object[] getParameters(Solution solution) {
		VariableTranslator translator = new VariableTranslator(solution.getDecisionVariables()[0]);

		Object[] arguments = translator.translateArray(this.parameters);
		
		return arguments;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
