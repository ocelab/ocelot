package it.unisa.ocelot.genetic.settings;

import java.util.HashMap;
import java.util.List;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.operators.crossover.SBXGenericCrossover;
import jmetal.operators.mutation.GenericPolynomialMutation;
import jmetal.operators.mutation.PolynomialMutationParams;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;
import jmetal.util.parallel.IParallelEvaluator;
import jmetal.util.parallel.MultithreadedEvaluator;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.StandardSettings;
import it.unisa.ocelot.genetic.algorithms.AVM;
import it.unisa.ocelot.genetic.algorithms.GeneticAlgorithm;

public class AVMSettings extends StandardSettings {
	public AVMSettings(Problem pProblem) {
		super(pProblem);
	}
	
	public AVMSettings(Problem pProblem, ConfigManager pConfig) {
		super(pProblem, pConfig);
	}
	
	public Algorithm configure() throws JMException {
        Algorithm algorithm;
        
        algorithm = new AVM(problem_);
        
        algorithm.setInputParameter("maxEvaluations", this.maxEvaluations);
        algorithm.setInputParameter("epsilon", 1D);
        algorithm.setInputParameter("delta", 1D);

        return algorithm;
    }
}
