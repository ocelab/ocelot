package it.unisa.ocelot.genetic.settings;



import it.unisa.ocelot.conf.ConfigManager;

import it.unisa.ocelot.genetic.algorithms.MemeticAlgorithm;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.util.JMException;
import jmetal.util.parallel.IParallelEvaluator;
import jmetal.util.parallel.MultithreadedEvaluator;

public class MemeticSettings extends GASettings {

	public MemeticSettings(Problem pProblem) {
		super(pProblem);
	}
	
	public MemeticSettings(Problem pProblem, ConfigManager pConfig) {
		super(pProblem, pConfig);
	}
	
	@SuppressWarnings("unused")
	public Algorithm configure() throws JMException {
		Algorithm algorithm;
		
		IParallelEvaluator parallelEvaluator = new MultithreadedEvaluator(threads);
		
		// Creating the problem
		algorithm = new MemeticAlgorithm(problem_);
        
		return configure(algorithm);
	}

}
