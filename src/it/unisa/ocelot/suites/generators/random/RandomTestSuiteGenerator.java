package it.unisa.ocelot.suites.generators.random;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.edges.EdgeCoverageExperiment;
import it.unisa.ocelot.genetic.nodes.NodeCoverageExperiment;
import it.unisa.ocelot.genetic.paths.PathCoverageExperiment;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
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


public class RandomTestSuiteGenerator extends TestSuiteGenerator {
	private ConfigManager config;
	private CFG cfg;
	private CoverageCalculator calculator;
	private Random random;
	private Range<Double>[] ranges;
	
	public RandomTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		this.config = pConfigManager;
		this.cfg = pCFG;
		this.calculator = new CoverageCalculator(cfg);
		this.random = new Random();
		this.ranges = this.config.getTestRanges();
	}
	
	@Override
	public Set<TestCase> generateTestSuite() throws TestSuiteGenerationException {
		Set<TestCase> suite = new HashSet<TestCase>();
		
		long time = 0;
		long timeout = System.currentTimeMillis() + this.config.getRandomTimeLimit()*1000;
		if (this.config.getRandomTimeLimit() < 0)
			timeout = Long.MAX_VALUE;
		
		int sizeout = config.getRandomSizeLimit();
		if (sizeout < 0)
			sizeout = Integer.MAX_VALUE;
		
		this.startBenchmarks();
		calculator.calculateCoverage(suite);
		double lastCoverage = 0.0;
		while (calculator.getBranchCoverage() < this.config.getRequiredCoverage() &&
				suite.size() <= sizeout &&
				time < timeout) {
			coverRandom(suite);
			calculator.calculateCoverage(suite);
			
			if (calculator.getBranchCoverage() > lastCoverage) {
				this.measureBenchmarks("Random", suite);
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
		
		this.measureBenchmarks("End", suite);
				
		return suite;
	}
	
	@SuppressWarnings("unchecked")
	private void coverRandom(Set<TestCase> suite) throws TestSuiteGenerationException {
		for (int i = 0; i < this.config.getRandomGranularity(); i++) {
			Object[] numericParams = random(cfg.getParameterTypes());
			TestCase testCase = this.createTestCase(numericParams, suite.size());
			suite.add(testCase);
		}
	}
	
	private TestCase createTestCase(Object[] pParams, int id) {
		this.calculator.calculateCoverage(pParams);
		
		TestCase tc = new TestCase();
		tc.setId(id);
		tc.setCoveredEdges(calculator.getCoveredEdges());
		tc.setParameters(pParams);
		
		return tc;
	}
	
	private Object[] random(Class[] pParamTypes) {
		Object[] parameters = new Object[pParamTypes.length];
		
		for (int i = 0; i < pParamTypes.length; i++) {
			if (pParamTypes[i] == Double.class) {
				double param = random.nextDouble() * (ranges[i].getMaximum() - ranges[i].getMinimum());
				param += ranges[i].getMinimum();
				parameters[i] = param;
			} else if (pParamTypes[i] == Integer.class) {
				int param = random.nextInt((int)(ranges[i].getMaximum() - ranges[i].getMinimum()));
				param += ranges[i].getMinimum();
				parameters[i] = param;
			} else {
				double param = random.nextDouble() * (ranges[i].getMaximum() - ranges[i].getMinimum());
				param += ranges[i].getMinimum();
				parameters[i] = param;
			}
		}
		
		return parameters; 
	}
	
	private void printSeparator() {
		if (this.config.getPrintResults())
			System.out.println("------------------------------------------");
	}
	
	private void print(Object pObject) {
		if (this.config.getPrintResults())
			System.out.print(pObject);
	}
	
	private void println(Object pObject) {
		if (this.config.getPrintResults())
			System.out.println(pObject);
	}
}
