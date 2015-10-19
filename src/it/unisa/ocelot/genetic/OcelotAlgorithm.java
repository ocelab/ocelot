package it.unisa.ocelot.genetic;

import it.unisa.ocelot.genetic.algorithms.AlgorithmStats;
import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

public abstract class OcelotAlgorithm extends Algorithm {
	private static final long serialVersionUID = -7835611261947665056L;
	
	protected AlgorithmStats algorithmStats;

	public OcelotAlgorithm(Problem problem) {
		super(problem);
		
		this.algorithmStats = new AlgorithmStats();
	}
	
	public AlgorithmStats getStats() {
		return algorithmStats;
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
