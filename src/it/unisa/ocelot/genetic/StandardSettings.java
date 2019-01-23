package it.unisa.ocelot.genetic;

import it.unisa.ocelot.conf.ConfigManager;

import java.util.List;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.experiments.Settings;
import jmetal.util.JMException;

public abstract class StandardSettings extends Settings {
    protected int populationSize;
    protected int maxEvaluations;
    protected double mutationProbability;
	protected double scalarMutationProbability;
	protected double numberOfMutants;				//Number of mutants that must be muted, if mutation's operator is executed
	protected double crossoverProbability;
    protected int threads;
    protected boolean debug;
    
    protected List<Double> numericConstants;
    protected boolean useMetaMutator;
    
	public StandardSettings(Problem pProblem) {
		super();
		
		this.problem_ = pProblem;
				
		populationSize = 100;
        maxEvaluations = 25000;
        crossoverProbability = 0.9;
		mutationProbability = 1.0 / problem_.getNumberOfVariables();
		scalarMutationProbability = 0.2;
		numberOfMutants = 0.2;
        threads = 1;
	}
	
	public StandardSettings(Problem pProblem, ConfigManager pConfig) {
		this(pProblem);
		
		try {
			populationSize = pConfig.getPopulationSize();
		} catch (NumberFormatException e) {}
		
		try {
			maxEvaluations = pConfig.getMaxEvaluations();
		} catch (NumberFormatException e) {}
		
		try {
			crossoverProbability = pConfig.getCrossoverProbability();
		} catch (NumberFormatException e) {}
		
		if (!pConfig.isDynamicMutation()) {
			try {
				mutationProbability = pConfig.getMutationProbability();
			} catch (NumberFormatException e) {}
		}

		scalarMutationProbability = 0.2;
		numberOfMutants = 0.2;


		try {
			threads = pConfig.getThreads();
		} catch (NumberFormatException e) {}
		
	}
	
	public void useMetaMutator() {
		this.useMetaMutator = true;
	}
	
	public abstract Algorithm configure() throws JMException;

	public List<Double> getNumericConstants() {
		return numericConstants;
	}

	public void setNumericConstants(List<Double> numericConstants) {
		this.numericConstants = numericConstants;
	}
}
