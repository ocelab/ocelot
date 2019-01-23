package it.unisa.ocelot.genetic;

import java.util.*;

import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;
import it.unisa.ocelot.genetic.encoding.manager.GraphGenerator;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.CBridgeStub;
import it.unisa.ocelot.simulator.SimulationException;

import jmetal.core.Variable;
import jmetal.encodings.solutionType.IntSolutionType;
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
	
	protected Map<Thread, CBridge> bridges;
	protected Map<Thread, CBridgeStub> bridgesStub;
	
	public StandardProblem(List<Graph> graphList, Range<Double>[] pRanges) throws Exception {
		this.bridges = new HashMap<Thread, CBridge>();
		this.bridgesStub = new HashMap<Thread, CBridgeStub>();
		//this.arraySize = pArraySize;

		this.graphList = graphList;
		//this.parameters = pParameters;

		numberOfVariables_ = graphList.get(0).getNodes().size();
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;

		lowerLimit_ = new double[numberOfVariables_];
		upperLimit_ = new double[numberOfVariables_];
		for (int i = 0; i < numberOfVariables_; i++) {
			lowerLimit_[i] = 0;
			upperLimit_[i] = 99;
		}

		solutionType_ = new IntSolutionType(this);
		//solutionType_ = new ArrayIntSolutionType(this);
	}

	//CANCELLATO PER RIMOZIONE OBJECT [][][]
	/*protected Object[][][] getParameters(Solution solution) {
		VariableTranslator translator = new VariableTranslator(solution);

		Object[][][] arguments = translator.translateArray(this.parameters);
		
		return arguments;
	}*/

	protected Graph getGraphFromSolution(Solution solution) throws JMException {
		GraphGenerator graphGenerator = new GraphGenerator();
		ArrayList<Node> nodesOfSolution = new ArrayList<>();

		Variable [] variables = solution.getDecisionVariables();

		//Take nodes from chromosome
		for (int i = 0; i < variables.length; i++) {
			int graphIndex = (int) variables[i].getValue();

			//Get i-Node from graphIndex's graphList
			Node nodeToAdd = this.graphList.get(graphIndex).getNode(i);
			nodesOfSolution.add(nodeToAdd);
		}

		//Build graph from these nodes
		Graph newGraph = graphGenerator.generateGraphFromArrayNodes(nodesOfSolution, graphList.get(0));

		return newGraph;
	}

	public List<Graph> getGraphList() {
		return graphList;
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

	protected CBridgeStub getCurrentBridgeStub() {
		CBridgeStub result = this.bridgesStub.get(Thread.currentThread());

		if (result == null) {
			result = new CBridgeStub(this.bridges.size(), 9, 0, 0);
			this.bridgesStub.put(Thread.currentThread(), result);
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
		VariableTranslator variableTranslator = new VariableTranslator(solution);

		Graph solutionGraph = this.getGraphFromSolution(solution);
		Object[][][] arguments = variableTranslator.translateGraph(solutionGraph);

		for (Object obj : arguments[2][0]) {
			Integer pointerRef = (Integer)obj;
			if (pointerRef < 0 || pointerRef >= numberOfArrays) {
				System.err.println("Warning: invalid pointer for " + Arrays.toString(arguments[2][0]));
				solution.setObjective(0, Double.MAX_VALUE);
				return;
			}
		}
		
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

		VariableTranslator variableTranslator = new VariableTranslator(solution);
		//Object[][][] arguments = this.getParameters(solution);
		Graph solutionGraph = variableTranslator.getGraphFromSolution(getGraphList());
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
