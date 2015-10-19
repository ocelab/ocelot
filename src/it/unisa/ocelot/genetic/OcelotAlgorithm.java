package it.unisa.ocelot.genetic;

import it.unisa.ocelot.genetic.algorithms.AlgorithmStats;
import jmetal.core.Algorithm;
import jmetal.core.Problem;

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
}
