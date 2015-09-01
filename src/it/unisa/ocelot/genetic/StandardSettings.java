package it.unisa.ocelot.genetic;

import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.algorithms.GeneticAlgorithm;
import it.unisa.ocelot.genetic.edges.CDG_BasedProblem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.experiments.Settings;
import jmetal.operators.crossover.SBXGenericCrossover;
import jmetal.operators.mutation.GenericPolynomialMutation;
import jmetal.operators.mutation.PolynomialMutationParams;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;
import jmetal.util.parallel.IParallelEvaluator;
import jmetal.util.parallel.MultithreadedEvaluator;

public abstract class StandardSettings extends Settings {
    protected int populationSize;
    protected int maxEvaluations;
    protected double mutationProbability;
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
        mutationProbability = 1.0 / problem_.getNumberOfVariables();
        crossoverProbability = 0.9;
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
		
		try {
			mutationProbability = pConfig.getMutationProbability();
		} catch (NumberFormatException e) {}
		
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
