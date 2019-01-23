package it.unisa.ocelot.genetic;

import it.unisa.ocelot.genetic.algorithms.AlgorithmStats;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

import java.util.List;

public abstract class OcelotAlgorithm extends Algorithm {
	private static final long serialVersionUID = -7835611261947665056L;

	protected List<Graph> graphList;
	protected AlgorithmStats algorithmStats;

	public OcelotAlgorithm(Problem problem, List<Graph> graphList) {
		super(problem);

		this.graphList = graphList;
		this.algorithmStats = new AlgorithmStats();
	}
	
	public AlgorithmStats getStats() {
		return algorithmStats;
	}

	public List<Graph> getGraphList() {
		return graphList;
	}

	/**
	 * Keeps executing the algorithm from the last population for k more iterations.
	 * @param pIterations Number of additional iterations
	 * @return
	 * @throws ClassNotFoundException
	 * @throws JMException
	 */
	public SolutionSet extraExecute(int pIterations) throws ClassNotFoundException, JMException {
		this.setInputParameter("maxEvaluations", pIterations);
		
		return this.execute();
	}
}
