//  ArrayReal.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
// 
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
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

package jmetal.encodings.variable;

import it.unisa.ocelot.genetic.StandardProblem;
import jmetal.core.Problem;
import jmetal.core.Variable;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * Class implementing a decision encodings.variable representing an array of
 * real values. The real values of the array have their own bounds.
 */
public class ArrayParameters extends ArrayReal {
	/**
	 * Problem using the type
	 */
	private StandardProblem problem_;

	/**
	 * Stores the length of the array
	 */
	private int size_;
	
	private int id;

	/**
	 * Constructor
	 */
	public ArrayParameters() {
		problem_ = null;
		size_ = 0;
		array_ = null;
	} // Constructor

	/**
	 * Constructor
	 *
	 * @param size
	 *            Size of the array
	 */
	public ArrayParameters(int size, Problem problem, int id) {
		if (problem instanceof StandardProblem)
			problem_ = (StandardProblem)problem;
		else
			throw new RuntimeException("Invalid problem type!");
		
		size_ = size;
		array_ = new Double[size_];
		
		this.id = id;

		for (int i = 0; i < size_; i++) {
			array_[i] = PseudoRandom.randDouble()
					* (problem_.getUpperLimit(id, i) - problem_.getLowerLimit(id, i))
					+ problem_.getLowerLimit(id, i);
		} // for
	} // Constructor

	/**
	 * Copy Constructor
	 *
	 * @param arrayReal
	 *            The arrayReal to copy
	 */
	private ArrayParameters(ArrayParameters arrayReal) {
		problem_ = arrayReal.problem_;
		size_ = arrayReal.size_;
		array_ = new Double[size_];
		id = arrayReal.id;

		System.arraycopy(arrayReal.array_, 0, array_, 0, size_);
	} // Copy Constructor

	@Override
	public Variable deepCopy() {
		return new ArrayParameters(this);
	} // deepCopy

	/**
	 * Returns the length of the arrayReal.
	 *
	 * @return The length
	 */
	public int getLength() {
		return size_;
	} // getLength

	/**
	 * getValue
	 *
	 * @param index
	 *            Index of value to be returned
	 * @return the value in position index
	 */
	public double getValue(int index) throws JMException {
		if ((index >= 0) && (index < size_))
			return array_[index];
		else {
			Configuration.logger_
					.severe(jmetal.encodings.variable.ArrayReal.class
							+ ".getValue(): index value (" + index
							+ ") invalid");
			throw new JMException(jmetal.encodings.variable.ArrayReal.class
					+ ".ArrayReal: index value (" + index + ") invalid");
		} // if
	} // getValue

	/**
	 * setValue
	 *
	 * @param index
	 *            Index of value to be returned
	 * @param value
	 *            The value to be set in position index
	 */
	public void setValue(int index, double value) throws JMException {
		if ((index >= 0) && (index < size_))
			array_[index] = value;
		else {
			Configuration.logger_
					.severe(jmetal.encodings.variable.ArrayReal.class
							+ ".setValue(): index value (" + index
							+ ") invalid");
			throw new JMException(jmetal.encodings.variable.ArrayReal.class
					+ ": index value (" + index + ") invalid");
		} // else
	} // setValue

	/**
	 * Get the lower bound of a value
	 *
	 * @param index
	 *            The index of the value
	 * @return the lower bound
	 */
	public double getLowerBound(int index) throws JMException {
		if ((index >= 0) && (index < size_))
			return problem_.getLowerLimit(id, index);
		else {
			Configuration.logger_
					.severe(jmetal.encodings.variable.ArrayReal.class
							+ ".getLowerBound(): index value (" + index
							+ ") invalid");
			throw new JMException(jmetal.encodings.variable.ArrayReal.class
					+ ".getLowerBound: index value (" + index + ") invalid");
		} // else
	} // getLowerBound

	/**
	 * Get the upper bound of a value
	 *
	 * @param index
	 *            The index of the value
	 * @return the upper bound
	 */
	public double getUpperBound(int index) throws JMException {
		if ((index >= 0) && (index < size_))
			return problem_.getUpperLimit(id, index);
		else {
			Configuration.logger_
					.severe(jmetal.encodings.variable.ArrayReal.class
							+ ".getUpperBound(): index value (" + index
							+ ") invalid");
			throw new JMException(jmetal.encodings.variable.ArrayReal.class
					+ ".getUpperBound: index value (" + index + ") invalid");
		} // else
	} // getLowerBound

	/**
	 * Returns a string representing the object
	 *
	 * @return The string
	 */
	public String toString() {
		String string;

		string = "";
		for (int i = 0; i < (size_ - 1); i++)
			string += array_[i] + " ";

		string += array_[size_ - 1];
		return string;
	} // toString

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
} // ArrayReal
