package it.unisa.ocelot.suites.generators.mccabe;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.c.cfg.paths.PathSearchSimplex;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.edges.EdgeCoverageExperiment;
import it.unisa.ocelot.genetic.many_nodes.PathCoverageExperiment;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;
import it.unisa.ocelot.util.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.util.JMException;

public class ReducedMcCabeTestSuiteGenerator extends TestSuiteGenerator {
	public ReducedMcCabeTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		super(pCFG);
		this.config = pConfigManager;
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
		}

		return suite;
	}

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
				if (this.config.getDebug())
					Utils.waitForEnter();
				
				exp.initExperiment();
				try {
					exp.basicRun();
				} catch (JMException | ClassNotFoundException e) {
					throw new TestSuiteGenerationException(e.getMessage());
				}
	
				double fitnessValue = exp.getFitnessValue();
				VariableTranslator translator = new VariableTranslator(exp.getSolution());
	
				Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());
	
				TestCase testCase = this.createTestCase(numericParams, suite.size());
				suite.add(testCase);
				
				//TODO: Fix with number of evaluations!
				this.measureBenchmarks("McCabe", suite, 0);
				lastIterationTestCases.add(testCase);
				
				this.println("Fitness function: " + fitnessValue + ". ");
				if (fitnessValue == 0.0)
					this.println("Path covered!");
				else
					this.println("Path not covered...");
				this.println("Parameters found: ");
				this.println(Arrays.toString(numericParams[0][0]));
				for (int k = 0; k < numericParams[1].length; k++)
					this.println(Arrays.toString(numericParams[1][k]));
				this.println(Arrays.toString(numericParams[2][0]));
				this.printSeparator();
			}
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
			VariableTranslator translator = new VariableTranslator(exp.getSolution());
			
			this.print("Fitness function: " + fitnessValue + ". ");
			Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());
			TestCase testCase = this.createTestCase(numericParams, suite.size());
			
			if (fitnessValue == 0.0) {
				this.println("Target covered!");
				suite.add(testCase);
				
				//TODO: Fix with number of evaluations!
				this.measureBenchmarks("McCabe", suite, 0);
			} else
				this.println("Target not covered...");
			
			uncoveredEdges.removeAll(testCase.getCoveredEdges());
			
			this.println("Parameters found: ");
			this.println(Arrays.toString(numericParams[0][0]));
			for (int k = 0; k < numericParams[1].length; k++)
				this.println(Arrays.toString(numericParams[1][k]));
			this.println(Arrays.toString(numericParams[2][0]));
		}
	}
}
