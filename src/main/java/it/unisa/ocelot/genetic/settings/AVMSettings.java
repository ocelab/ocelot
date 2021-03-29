package it.unisa.ocelot.genetic.settings;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.util.JMException;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.StandardSettings;
import it.unisa.ocelot.genetic.algorithms.AVM;

public class AVMSettings extends StandardSettings {
	private double epsilon;
	private double delta;

	public AVMSettings(Problem pProblem) {
		super(pProblem);
	}
	
	public AVMSettings(Problem pProblem, ConfigManager pConfig) {
		super(pProblem, pConfig);
		
		this.epsilon = pConfig.getAvmEpsilon();
		this.delta = pConfig.getAvmDelta();
	}
	
	public Algorithm configure() throws JMException {
        Algorithm algorithm;
        
        algorithm = new AVM(problem_);
        
        algorithm.setInputParameter("maxEvaluations", this.maxEvaluations);
        algorithm.setInputParameter("epsilon", this.epsilon);
        algorithm.setInputParameter("delta", this.delta);
        
        if (problem_ instanceof StandardProblem) {
        	algorithm.setInputParameter("parametersTypes", ((StandardProblem) problem_).getParametersTypes());
        }

        return algorithm;
    }
}
