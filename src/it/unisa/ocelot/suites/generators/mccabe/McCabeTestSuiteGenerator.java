package it.unisa.ocelot.suites.generators.mccabe;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.nodes.NodeCoverageExperiment;
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

public class McCabeTestSuiteGenerator extends TestSuiteGenerator {
	private ConfigManager config;
	private CFG cfg;
	private CoverageCalculator calculator;

	public McCabeTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
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
		}

		return suite;
	}

	@SuppressWarnings("unchecked")
	private void coverMcCabePaths(Set<TestCase> suite)
			throws TestSuiteGenerationException {
		McCabeCalculator mcCabeCalculator = new McCabeCalculator(cfg);
		mcCabeCalculator.calculateMcCabePaths();
		ArrayList<ArrayList<LabeledEdge>> mcCabePaths = mcCabeCalculator
				.getMcCabeEdgePaths();

		for (ArrayList<LabeledEdge> aMcCabePath : mcCabePaths) {
			PathCoverageExperiment exp = new PathCoverageExperiment(cfg,
					config, cfg.getParameterTypes(), aMcCabePath);

			exp.initExperiment();
			try {
				exp.basicRun();
			} catch (JMException | ClassNotFoundException e) {
				throw new TestSuiteGenerationException(e.getMessage());
			}

			//this.printSeparator();
			this.print("Current target: ");
			this.println(aMcCabePath);

			double fitnessValue = exp.getFitnessValue();
			Variable[] params = exp.getVariables();
			VariableTranslator translator = new VariableTranslator(params[0]);

			Object[] numericParams = translator.translateArray(cfg
					.getParameterTypes());

			TestCase testCase = this
					.createTestCase(numericParams, suite.size());
			suite.add(testCase);

			this.println("Fitness function: " + fitnessValue + ". ");
			if (fitnessValue == 0.0)
				this.println("Path covered!");
			else
				this.println("Path not covered...");
			this.println("Parameters found: " + Arrays.toString(numericParams));
			this.printSeparator();
			
			this.measureBenchmarks("McCabe path", suite);
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
			LabeledEdge targetEdge = uncoveredEdges.get(0);
			uncoveredEdges.remove(0); //avoids infinite loop
			CFGNode targetNode = cfg.getEdgeTarget(targetEdge);
			
			NodeCoverageExperiment exp = new NodeCoverageExperiment(cfg, config, cfg.getParameterTypes(), targetNode);
			exp.initExperiment();
			try {
				exp.basicRun();
			} catch (JMException|ClassNotFoundException e) {
				throw new TestSuiteGenerationException(e.getMessage());
			}
			
			this.printSeparator();
			this.print("Current target: ");
			this.println(targetNode);
			
			double fitnessValue = exp.getFitnessValue();
			Variable[] params = exp.getVariables();
			VariableTranslator translator = new VariableTranslator(params[0]);
				
			this.print("Fitness function: " + fitnessValue + ". ");
			if (fitnessValue == 0.0)
				this.println("Target covered!");
			else
				this.println("Target not covered...");
			
			Object[] numericParams = translator.translateArray(cfg.getParameterTypes());
			TestCase testCase = this.createTestCase(numericParams, suite.size());
			suite.add(testCase);
			
			uncoveredEdges.removeAll(testCase.getCoveredEdges());
			
			this.println("Parameters found: " + Arrays.toString(numericParams));
			
			this.measureBenchmarks("Single target", suite);
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
