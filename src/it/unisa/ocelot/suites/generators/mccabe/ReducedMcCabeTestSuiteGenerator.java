package it.unisa.ocelot.suites.generators.mccabe;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.c.cfg.paths.PathSearchSimplex;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.edges.EdgeCoverageExperiment;
import it.unisa.ocelot.genetic.paths.PathCoverageExperiment;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Variable;
import jmetal.util.JMException;

public class ReducedMcCabeTestSuiteGenerator extends TestSuiteGenerator {
	private ConfigManager config;
	private CFG cfg;
	private CoverageCalculator calculator;

	public ReducedMcCabeTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		this.config = pConfigManager;
		this.cfg = pCFG;
		this.calculator = new CoverageCalculator(cfg);
	}

	@Override
	public Set<TestCase> generateTestSuite()
			throws TestSuiteGenerationException {
		Set<TestCase> suite = new HashSet<TestCase>();

		this.startBenchmarks();

		coverMcCabePaths(suite);

		calculator.calculateCoverage(suite);
		if (calculator.getBranchCoverage() < this.config.getRequiredCoverage()) {
			coverSingleTargets(suite);
			this.measureBenchmarks("Single targets", suite);
		}

		return suite;
	}

	@SuppressWarnings("unchecked")
	private void coverMcCabePaths(Set<TestCase> suite)
			throws TestSuiteGenerationException {
		McCabeCalculator mcCabeCalculator = new McCabeCalculator(cfg);
		mcCabeCalculator.calculateMcCabePaths();
		
		List<List<LabeledEdge>> mcCabePaths;
		PathSearchSimplex searchPath = new PathSearchSimplex(
				this.cfg, 
				mcCabeCalculator.getMcCabeVectorPaths(), 
				config.getReducedMcCabeCoverageTimes());
		
		searchPath.solve();
		mcCabePaths = searchPath.getChosenPaths();
		
		boolean improvement = true;
		
		double lastCoverage = 0;
		while (improvement && lastCoverage < this.config.getRequiredCoverage()) {
			try { 
				lastCoverage = calculator.getBranchCoverage();
			} catch (NullPointerException e) {
				lastCoverage = 0;
			}
			System.out.println("Starting an iteration...");
			Set<TestCase> lastIterationTestCases = new HashSet<TestCase>();
			for (List<LabeledEdge> aMcCabePath : mcCabePaths) {
				PathCoverageExperiment exp = new PathCoverageExperiment(cfg,
						config, cfg.getParameterTypes(), aMcCabePath);
	
				//this.printSeparator();
				this.print("Current target: ");
				this.println(aMcCabePath);
				
				exp.initExperiment();
				try {
					exp.basicRun();
				} catch (JMException | ClassNotFoundException e) {
					throw new TestSuiteGenerationException(e.getMessage());
				}
	
				double fitnessValue = exp.getFitnessValue();
				Variable[] params = exp.getVariables();
				VariableTranslator translator = new VariableTranslator(params[0]);
	
				Object[] numericParams = translator.translateArray(cfg
						.getParameterTypes());
	
				TestCase testCase = this
						.createTestCase(numericParams, suite.size());
				suite.add(testCase);
				lastIterationTestCases.add(testCase);
	
				this.println("Fitness function: " + fitnessValue + ". ");
				if (fitnessValue == 0.0)
					this.println("Path covered!");
				else
					this.println("Path not covered...");
				this.println("Parameters found: " + Arrays.toString(numericParams));
				this.printSeparator();
			}
			
			this.measureBenchmarks("McCabe paths iteration", suite);
			calculator.calculateCoverage(suite);
			improvement = calculator.getBranchCoverage() > lastCoverage;
			lastCoverage = calculator.getBranchCoverage();
			System.out.println("Iteration ended!");
			if (improvement) {
				searchPath.solve(this.getUncoveredEdges(suite));
				mcCabePaths = searchPath.getChosenPaths();
			} else {
				this.removeLastBenchmark();
				suite.removeAll(lastIterationTestCases);
			}
		}
	}
	
	private List<LabeledEdge> getUncoveredEdges(Set<TestCase> suite) {
		List<LabeledEdge> uncoveredEdges = new ArrayList<LabeledEdge>(cfg.edgeSet()); 
		for (TestCase tc : suite) {
			uncoveredEdges.removeAll(tc.getCoveredEdges());
		}
		
		return uncoveredEdges;
	}

	@SuppressWarnings("unchecked")
	private void coverSingleTargets(Set<TestCase> suite) throws TestSuiteGenerationException {
		List<LabeledEdge> uncoveredEdges = this.getUncoveredEdges(suite);
		
		Collections.shuffle(uncoveredEdges);
		while (!uncoveredEdges.isEmpty()) {
			LabeledEdge targetEdge = uncoveredEdges.remove(0); //avoids infinite loop
			
			EdgeCoverageExperiment exp = new EdgeCoverageExperiment(cfg, config, cfg.getParameterTypes(), targetEdge);
			exp.initExperiment();
			try {
				exp.basicRun();
			} catch (JMException|ClassNotFoundException e) {
				throw new TestSuiteGenerationException(e.getMessage());
			}
			
			this.printSeparator();
			this.print("Current target: ");
			this.println(targetEdge);
			
			double fitnessValue = exp.getFitnessValue();
			Variable[] params = exp.getVariables();
			VariableTranslator translator = new VariableTranslator(params[0]);
			
			this.print("Fitness function: " + fitnessValue + ". ");
			Object[] numericParams = translator.translateArray(cfg.getParameterTypes());
			TestCase testCase = this.createTestCase(numericParams, suite.size());
			
			if (fitnessValue == 0.0) {
				this.println("Target covered!");
				suite.add(testCase);
			} else
				this.println("Target not covered...");
			
			uncoveredEdges.removeAll(testCase.getCoveredEdges());
			
			this.println("Parameters found: " + Arrays.toString(numericParams));
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

	private void printSeparator() {
		if (this.config.getPrintResults())
			System.out
					.println("-------------------------------------------------------------------------------");
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
