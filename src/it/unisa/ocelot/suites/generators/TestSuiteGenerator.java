package it.unisa.ocelot.suites.generators;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.benchmarks.BenchmarkCalculator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TestSuiteGenerator {
	protected List<BenchmarkCalculator> benchmarkCalculators;
	protected ConfigManager config;
	public CoverageCalculator calculator;
	public CFG cfg;
	
	public TestSuiteGenerator(CFG pCFG) {
		this.benchmarkCalculators = new ArrayList<BenchmarkCalculator>();
		this.cfg = pCFG;
		this.calculator = new CoverageCalculator(pCFG);
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
		return this.generateTestSuite(new HashSet<TestCase>());
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
		tc.setCoveredEdges(calculator.getCoveredEdges());
		tc.setParameters(pParams);
	
		return tc;
	}
}
