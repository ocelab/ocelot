package it.unisa.ocelot.suites.generators.random;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.types.CDouble;
import it.unisa.ocelot.c.types.CInteger;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.edges.EdgeCoverageExperiment;
import it.unisa.ocelot.genetic.nodes.NodeCoverageExperiment;
import it.unisa.ocelot.genetic.paths.PathCoverageExperiment;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.Range;

import jmetal.core.Variable;
import jmetal.util.JMException;

/**
 * Randomly generates test cases, keeping only the ones that improve coverage. The maximum number of generations
 * is set in the config file.
 * @author simone
 *
 */
public class RandomTestSuiteGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private Random random;
	private Range<Double>[] ranges;
	private boolean satisfied;
	
	public RandomTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		super(pCFG);
		this.config = pConfigManager;
		this.random = new Random();
		this.ranges = this.config.getTestRanges();
	}
	
	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite) throws TestSuiteGenerationException {
		Set<TestCase> suite = new HashSet<TestCase>(pSuite);
		
		this.startBenchmarks();
		
		calculator.calculateCoverage(suite);
		
		this.generateRandomSuite(suite);
		
		calculator.calculateCoverage(suite);
		if (calculator.getBranchCoverage() >= this.config.getRequiredCoverage()) {
			this.satisfied = true;
		}
		
		return suite;
	}
	
	public boolean isSatisfied() {
		return satisfied;
	}
	
	private void generateRandomSuite(Set<TestCase> suite) throws TestSuiteGenerationException {
		long time = 0;
		long timeout = System.currentTimeMillis() + this.config.getRandomTimeLimit()*1000;
		if (this.config.getRandomTimeLimit() < 0)
			timeout = Long.MAX_VALUE;
		
		int sizeout = config.getRandomSizeLimit();
		if (sizeout < 0)
			sizeout = Integer.MAX_VALUE;
		
		double lastCoverage = 0.0;
		while (calculator.getBranchCoverage() < this.config.getRequiredCoverage() &&
				suite.size() <= sizeout &&
				time < timeout) {
			coverRandom(suite);
			calculator.calculateCoverage(suite);
			
			if (calculator.getBranchCoverage() > lastCoverage) {
				this.measureBenchmarks("Random", suite, null);
				lastCoverage = calculator.getBranchCoverage();
				
				this.println(calculator.getBranchCoverage());
				this.println(suite.size());
				this.printSeparator();
			}
			if (suite.size() % 10000 == 0) {
				this.print("Temporary size: ");
				this.println(suite.size());
			}
			
			time = System.currentTimeMillis();
			this.println("Time: " + (timeout-time));
		}
		
		this.measureBenchmarks("End", suite, null);
	}
	
	@SuppressWarnings("unchecked")
	private void coverRandom(Set<TestCase> suite) throws TestSuiteGenerationException {
		for (int i = 0; i < this.config.getRandomGranularity(); i++) {
			Object[][][] numericParams = random(cfg.getParameterTypes());
			TestCase testCase = this.createTestCase(numericParams, suite.size());
			suite.add(testCase);
		}
	}
	
	//TODO Make this method work. Now it doesn't, due to the major change of parameters
	private Object[][][] random(CType[] pParamTypes) {
		Object[][][] parameters = new Object[pParamTypes.length][][];
		parameters[0] = new Object[1][];
		parameters[0][0] = new Object[pParamTypes.length];
		
		for (int i = 0; i < pParamTypes.length; i++) {
			if (pParamTypes[i] instanceof CDouble) {
				double param = random.nextDouble() * (ranges[i].getMaximum() - ranges[i].getMinimum());
				param += ranges[i].getMinimum();
				parameters[0][0][i] = param;
			} else if (pParamTypes[i] instanceof CInteger) {
				int param = random.nextInt((int)(ranges[i].getMaximum() - ranges[i].getMinimum()));
				param += ranges[i].getMinimum();
				parameters[0][0][i] = param;
			} else {
				double param = random.nextDouble() * (ranges[i].getMaximum() - ranges[i].getMinimum());
				param += ranges[i].getMinimum();
				parameters[0][0][i] = param;
			}
		}
		
		return parameters; 
	}
	
	@Override
	public int getNumberOfEvaluations() {
		// TODO Auto-generated method stub
		return 0;
	}
}
