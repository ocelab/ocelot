package jmetal.util.wrapper;

import jmetal.core.Solution;
import jmetal.core.SolutionType;
import jmetal.encodings.solutionType.ArrayParametersSolutionType;
import jmetal.encodings.variable.ArrayParameters;
import jmetal.util.Configuration;
import jmetal.util.JMException;

/**
 * Wrapper for accessing real-coded solutions
 */
public class XParamArray {
	private Solution solution_ ;
	private SolutionType type_ ;

	/**
	 * Constructor
	 */
	public XParamArray() {
	} // Constructor

	/**
	 * Constructor
	 * @param solution
	 */
	public XParamArray(Solution solution) {
		this() ;
		type_ = solution.getType() ;
		solution_ = solution ;
	}

	/**
	 * Gets value of a encodings.variable
	 * @param index Index of the encodings.variable
	 * @return The value of the encodings.variable
	 * @throws JMException
	 */
	public double getValue(int kind, int index) throws JMException {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[kind])).array_[index];
		} else {
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.getValue, solution type " +
					type_ + "+ invalid") ;		
		}
		return 0.0 ;
	}

	/**
	 * Sets the value of a encodings.variable
	 * @param index Index of the encodings.variable
	 * @param value Value to be assigned
	 * @throws JMException
	 */
	public void setValue(int kind, int index, double value) throws JMException {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			((ArrayParameters)(solution_.getDecisionVariables()[kind])).array_[index]=value;
		} else
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.setValue, solution type " +
					type_ + "+ invalid") ;		
	} // setValue	

	/**
	 * Gets the lower bound of a encodings.variable
	 * @param index Index of the encodings.variable
	 * @return The lower bound of the encodings.variable
	 * @throws JMException
	 */
	public double getLowerBound(int kind, int index) throws JMException {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[kind])).getLowerBound(index);
		} else {
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.getLowerBound, solution type " +
					type_ + "+ invalid") ;		

		}
		return 0.0 ;
	} // getLowerBound

	/**
	 * Gets the upper bound of a encodings.variable
	 * @param index Index of the encodings.variable
	 * @return The upper bound of the encodings.variable
	 * @throws JMException
	 */
	public double getUpperBound(int kind, int index) throws JMException {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[kind])).getUpperBound(index);
		} else
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.getUpperBound, solution type " +
					type_ + "+ invalid") ;		

		return 0.0 ;
	} // getUpperBound

	/**
	 * Returns the number of variables of the solution
	 * @return
	 */
	public int getNumberOfDecisionVariables(int kind) {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[kind])).getLength() ;
		} else
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.size, solution type " +
					type_ + "+ invalid") ;		
		return 0 ;
	} // getNumberOfDecisionVariables
	
	/**
	 * Returns the number of variables of the solution
	 * @return
	 */
	public int size(int kind) {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[kind])).getLength() ;
		} else
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.size, solution type " +
					type_ + "+ invalid") ;		
		return 0 ;
	} // size
	
	public int kinds() {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return solution_.getDecisionVariables().length;
		} else
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.kinds, solution type " +
					type_ + "+ invalid") ;		
		return 0 ;
	}
} // XParam