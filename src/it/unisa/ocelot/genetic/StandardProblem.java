package it.unisa.ocelot.genetic;

import java.util.*;

import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;
import it.unisa.ocelot.genetic.encoding.graph.ScalarNode;
import it.unisa.ocelot.genetic.encoding.manager.GraphGenerator;
import it.unisa.ocelot.genetic.encoding.manager.GraphManager;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.SimulationException;

import jmetal.core.Variable;
import jmetal.encodings.solutionType.ArrayParametersSolutionType;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.encodings.variable.ArrayParameters;
import jmetal.encodings.variable.ArrayReal;
import org.apache.commons.lang3.Range;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.util.JMException;

public abstract class StandardProblem extends Problem {
	private static final int MAX_TRIES = 10;
	private static final long serialVersionUID = -462606769605747252L;

	protected List<Graph> graphList;
	protected boolean debug;
	protected int numberOfArrays;
	protected int arraySize;

	protected double[][] lowerLimits;
	protected double[][] upperLimits;

	protected Map<Integer, Integer> scalarNodeIndexMap;
	
	protected Map<Thread, CBridge> bridges;
	
	public StandardProblem(List<Graph> graphList, Range<Double>[] pRanges) throws Exception {
		this.bridges = new HashMap<Thread, CBridge>();
		//this.arraySize = pArraySize;

		this.graphList = graphList;
		//this.parameters = pParameters;

		GraphManager graphManager = new GraphManager();
		numberOfVariables_ = graphManager.getNumberOfScalarNodes(graphList.get(0));
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;

		//lowerLimit_ = new double[numberOfVariables_];
		//upperLimit_ = new double[numberOfVariables_];
		lowerLimits = new double[1][numberOfVariables_];
		upperLimits = new double[1][numberOfVariables_];

		for (int i = 0; i < numberOfVariables_; i++) {
			//lowerLimit_[i] = -10000;
			lowerLimits[0][i] = pRanges[0].getMinimum();
			//upperLimit_[i] = 10000;
			upperLimits[0][i] = pRanges[0].getMaximum();
		}

		solutionType_ = new ArrayParametersSolutionType(this);

		ArrayList<ScalarNode> scalarNodes = graphManager.getScalarNodes(graphList.get(0));
		this.scalarNodeIndexMap = new HashMap<>();

		for (int i = 0; i < numberOfVariables_; i++) {
			this.scalarNodeIndexMap.put(i, scalarNodes.get(i).getId());
		}

		//solutionType_ = new ArrayIntSolutionType(this);
	}

	public List<Graph> getGraphList() {
		return graphList;
	}

	public void setGraphList(List<Graph> graphList) {
		this.graphList = graphList;
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
		return upperLimits[pArray][i];
	}
	
	public void onError(Solution solution, Throwable e) {
		solution.setObjective(0, Double.POSITIVE_INFINITY);
		System.err.println("An error occurred: " + e.getMessage());
	}
	
	protected CBridge getCurrentBridge() {
		CBridge result = this.bridges.get(Thread.currentThread());
		
		if (result == null) {
			result = new CBridge(this.bridges.size());
			this.bridges.put(Thread.currentThread(), result); 
		}
		
		return result;
	}
	
	/**
	 * Evaluates a potential solution to the problem
	 * @param solution Solution to be evaluated
	 * @return The branch distance (if any)
	 * @throws JMException
	 * @throws SimulationException
	 */
	public abstract double evaluateSolution(Solution solution) throws JMException, SimulationException;
	
	@Override
	public final void evaluate(Solution solution) throws JMException {
		int tries = MAX_TRIES;
		
		while (tries > 0) {
			try {
				this.evaluateSolution(solution);
				return;
			} catch (SimulationException e) {
				tries--;
			}
		}
		
		throw new JMException("Unable to evaluate the solution " + solution.getDecisionVariables().toString());
	}
	
	public final double evaluateWithBranchDistance(Solution solution) throws JMException {
		int tries = MAX_TRIES;

		VariableTranslator variableTranslator = new VariableTranslator(this.graphList, this.scalarNodeIndexMap);
		Graph solutionGraph = variableTranslator.getGraphFromSolution(solution);
		Object[][][] arguments = variableTranslator.translateGraph(solutionGraph);


		for (Object obj : arguments[2][0]) {
			Integer pointerRef = (Integer)obj;
			if (pointerRef < 0 || pointerRef >= numberOfArrays) {
				System.err.println("Warning: invalid pointer for " + Arrays.toString(arguments[2][0]));
				solution.setObjective(0, Double.MAX_VALUE);
				return -1;
			}
		}
		
		while (tries > 0) {
			try {
				return this.evaluateSolution(solution);
			} catch (SimulationException e) {
				tries--;
			}
		}
		
		throw new JMException("Unable to evaluate the solution " + solution.getDecisionVariables().toString());
	}
}
