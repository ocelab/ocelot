package jmetal.util.wrapper;

import jmetal.core.Solution;
import jmetal.core.SolutionType;
import jmetal.encodings.solutionType.ArrayParametersSolutionType;
import jmetal.encodings.solutionType.ArrayRealAndBinarySolutionType;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.encodings.variable.ArrayParameters;
import jmetal.encodings.variable.ArrayReal;
import jmetal.util.Configuration;
import jmetal.util.JMException;

/**
 * Wrapper for accessing real-coded solutions
 */
public class XParam {
	private Solution solution_ ;
	private SolutionType type_ ;

	/**
	 * Constructor
	 */
	public XParam() {
	} // Constructor

	/**
	 * Constructor
	 * @param solution
	 */
	public XParam(Solution solution) {
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
	public double getValue(int index) throws JMException {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[0])).array_[index];
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
	public void setValue(int index, double value) throws JMException {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			((ArrayParameters)(solution_.getDecisionVariables()[0])).array_[index]=value;
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
	public double getLowerBound(int index) throws JMException {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[0])).getLowerBound(index);
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
	public double getUpperBound(int index) throws JMException {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[0])).getUpperBound(index);
		} else
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.getUpperBound, solution type " +
					type_ + "+ invalid") ;		

		return 0.0 ;
	} // getUpperBound

	/**
	 * Returns the number of variables of the solution
	 * @return
	 */
	public int getNumberOfDecisionVariables() {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[0])).getLength() ;
		} else
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.size, solution type " +
					type_ + "+ invalid") ;		
		return 0 ;
	} // getNumberOfDecisionVariables
	
	/**
	 * Returns the number of variables of the solution
	 * @return
	 */
	public int size() {
		if (type_.getClass() == ArrayParametersSolutionType.class) {
			return ((ArrayParameters)(solution_.getDecisionVariables()[0])).getLength() ;
		} else
			Configuration.logger_.severe("jmetal.util.wrapper.XParam.size, solution type " +
					type_ + "+ invalid") ;		
		return 0 ;
	} // size
} // XParam