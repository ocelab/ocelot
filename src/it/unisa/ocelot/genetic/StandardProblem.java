package it.unisa.ocelot.genetic;

import java.util.HashMap;
import java.util.Map;

import it.unisa.ocelot.c.types.CType;

import org.apache.commons.lang3.Range;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayParametersSolutionType;
import jmetal.encodings.solutionType.ArrayRealSolutionType;

public abstract class StandardProblem extends Problem {
	private static final long serialVersionUID = -462606769605747252L;
	
	protected CType[] parameters;
	protected boolean debug;
	protected int numberOfArrays;
	protected int arraySize;
	
	protected double[][] lowerLimits;
	protected double[][] upperLimits;
	
	public StandardProblem(CType[] pParameters, Range<Double>[] pRanges, int pArraySize) throws Exception {
		this.arraySize = pArraySize;
		
		int numberOfReferences = 0;
		for (CType type : pParameters) {
			if (type.isPointer())
				numberOfReferences++;
		}
		
		//Splits the types in two partitions: value types and pointer types (keeps the original order)
		CType[] valuesTypes = new CType[pParameters.length - numberOfReferences];
		CType[] pointerTypes = new CType[numberOfReferences];
		
		Map<Integer, Integer> posValueParams = new HashMap<Integer, Integer>();
		Map<Integer, Integer> posPointerParams = new HashMap<Integer, Integer>();
		
		int valueId = 0;
		int pointerId = 0;
		for (int i = 0; i < pParameters.length; i++)
			if (!pParameters[i].isPointer()) {
				posValueParams.put(valueId, i);
				valuesTypes[valueId] = pParameters[i];
				valueId++;
			} else {
				posPointerParams.put(pointerId, i);
				pointerTypes[pointerId] = pParameters[i];
				pointerId++;
			}
		
		numberOfArrays = numberOfReferences;
		numberOfVariables_ = pParameters.length + 1;
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;

		solutionType_ = new ArrayParametersSolutionType(this);

		lowerLimits = new double[numberOfReferences + 2][];
		upperLimits = new double[numberOfReferences + 2][];
		
		lowerLimits[0] = new double[valuesTypes.length];
		upperLimits[0] = new double[valuesTypes.length];
		//Sets up the ranges for the variables values
		for (int i = 0; i < valuesTypes.length; i++) {
			int pos = posValueParams.get(i);
			if (pRanges != null && pRanges.length > pos && pRanges[pos] != null) {
				lowerLimits[0][i] = pRanges[pos].getMinimum();
				upperLimits[0][i] = pRanges[pos].getMaximum();
			} else {
				lowerLimits[0][i] = valuesTypes[i].getMinValue();
				upperLimits[0][i] = valuesTypes[i].getMaxValue();
			}
		}
		
		for (int i = 0; i < numberOfReferences; i++) {
			lowerLimits[i+1] = new double[pArraySize];
			upperLimits[i+1] = new double[pArraySize];
			
			for (int j = 0; j < pArraySize; j++) {
				int pos = posPointerParams.get(i);
				if (pRanges != null && pRanges.length > pos && pRanges[pos] != null) {
					lowerLimits[i+1][j] = pRanges[pos].getMinimum();
					upperLimits[i+1][j] = pRanges[pos].getMaximum();
				} else {
					lowerLimits[i+1][j] = pointerTypes[i].getMinValue();
					upperLimits[i+1][j] = pointerTypes[i].getMaxValue();
				}
			}
		}
		
		lowerLimits[1 + numberOfReferences] = new double[numberOfReferences];
		upperLimits[1 + numberOfReferences] = new double[numberOfReferences];
		//Sets up the ranges for the variables references (if any)
		for (int i = 0; i < numberOfReferences; i++) {
			lowerLimits[1 + numberOfReferences][i] = 0;
			upperLimits[1 + numberOfReferences][i] = numberOfReferences;
		}
		
		this.parameters = pParameters;
	}
	
	protected Object[][][] getParameters(Solution solution) {
		VariableTranslator translator = new VariableTranslator(solution);

		Object[][][] arguments = translator.translateArray(this.parameters);
		
		return arguments;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public int getNumberOfArrays() {
		return this.numberOfArrays;
	}

	public int getArraySize() {
		return this.arraySize;
	}
	
	public double getLowerLimit(int pArray, int i) {
		return lowerLimits[pArray][i];
	}
	
	public double getUpperLimit(int pArray, int i) {
		// TODO Auto-generated method stub
		return upperLimits[pArray][i];
	}
	
	public void onError(Solution solution, Throwable e) {
		solution.setObjective(0, Double.POSITIVE_INFINITY);
		System.err.println("An error occurred: " + e.getMessage());
	}
}
