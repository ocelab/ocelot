package it.unisa.ocelot.suites.generators;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.OcelotExperiment;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.benchmarks.BenchmarkCalculator;
import it.unisa.ocelot.suites.budget.BudgetManager;
import it.unisa.ocelot.suites.budget.BudgetManagerHandler;
import it.unisa.ocelot.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Solution;

public abstract class TestSuiteGenerator {
	protected List<BenchmarkCalculator> benchmarkCalculators;
	protected ConfigManager config;
	public CoverageCalculator calculator;
	public CFG cfg;
	protected BudgetManager budgetManager;
	private int fixedBudget;
	
	public TestSuiteGenerator(CFG pCFG) {
		this.benchmarkCalculators = new ArrayList<BenchmarkCalculator>();
		this.cfg = pCFG;
		this.calculator = new CoverageCalculator(pCFG);
		this.fixedBudget = -1;
	}
	
	public void addBenchmark(BenchmarkCalculator pCalculator) {
		this.benchmarkCalculators.add(pCalculator);
	}
	
	protected void startBenchmarks() {
		for (BenchmarkCalculator benchmarkCalculator : this.benchmarkCalculators)
			benchmarkCalculator.start();
	}
	
	protected void measureBenchmarks(String pLabel, Set<TestCase> pSuite, Integer evaluations) {
		for (BenchmarkCalculator benchmarkCalculator : this.benchmarkCalculators)
			benchmarkCalculator.measure(pLabel, pSuite, evaluations);
	}
	
	protected void removeLastBenchmark() {
		for (BenchmarkCalculator benchmarkCalculator : this.benchmarkCalculators)
			benchmarkCalculator.removeLast();
	}
	
	public Set<TestCase> generateTestSuite() throws TestSuiteGenerationException {
		Set<TestCase> suite = this.generateTestSuite(new HashSet<TestCase>());
		this.measureBenchmarks("End", suite, 0);
		return suite;
	}
	
	public abstract Set<TestCase> generateTestSuite(Set<TestCase> pSuite) throws TestSuiteGenerationException;
	
	protected void printSeparator() {
		if (this.config.getPrintResults())
			System.out
					.println("-------------------------------------------------------------------------------");
	}

	protected void print(Object pObject) {
		if (this.config.getPrintResults())
			System.out.print(pObject);
	}
	
	protected void printStat(OcelotExperiment experiment, String pStat) {
		if (experiment.getAlgorithmStats() != null)
			this.println(experiment.getAlgorithmStats().getStat(pStat));
		else
			System.err.println("No stats found! Please check the generator...");
	}

	protected void println(Object pObject) {
		if (this.config.getPrintResults())
			System.out.println(pObject);
	}

	protected List<LabeledEdge> getUncoveredEdges(Set<TestCase> suite) {
		List<LabeledEdge> uncoveredEdges = new ArrayList<LabeledEdge>(cfg.edgeSet()); 
		for (TestCase tc : suite) {
			uncoveredEdges.removeAll(tc.getCoveredEdges());
		}
		
		return uncoveredEdges;
	}

	protected TestCase createTestCase(Object[][][] pParams, int id) {
		this.calculator.calculateCoverage(pParams);
	
		TestCase tc = new TestCase();
		tc.setId(id);
		tc.setCoveredPath(calculator.getCoveredPath());
		tc.setParameters(pParams);
	
		return tc;
	}
	
	protected void addSerendipitousTestCases(OcelotExperiment exp, Set<TestCase> suite) {
		if (!config.getSerendipitousCoverage())
			return;
		
		Set<Solution> solutions = exp.getSerendipitousSolutions();
		
		for (Solution solution : solutions) {
			VariableTranslator translator = new VariableTranslator(solution);
			
			Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());
			TestCase testCase = this.createTestCase(numericParams, suite.size());
			calculator.calculateCoverage(suite);
			double prevCoverage = calculator.getBranchCoverage();
			
			suite.add(testCase);
			calculator.calculateCoverage(suite);
			if (calculator.getBranchCoverage() == prevCoverage)
				suite.remove(testCase);
			else {
				this.print("Serendipitous coverage! ");
				this.measureBenchmarks("Serendipitous", suite, exp.getNumberOfEvaluation());
	
				this.println("Parameters found: " + Utils.printParameters(numericParams));
			}
		}
	}
	
	public boolean needsBudget() {
		return true;
	}
	
	protected void setFixedBudget(int pFixedBudget) {
		this.fixedBudget = pFixedBudget;
	}
	
	public void setupBudgetManager(int pNumberOfExperiments) {
		if (this.budgetManager == null)
			if (this.fixedBudget == -1)
				this.budgetManager = BudgetManagerHandler.getInstance(config, pNumberOfExperiments);
			else
				this.budgetManager = BudgetManagerHandler.getInstance(config.getBudgetManager(), this.fixedBudget, pNumberOfExperiments);
	}
	
	public abstract int getNumberOfEvaluations();
}
