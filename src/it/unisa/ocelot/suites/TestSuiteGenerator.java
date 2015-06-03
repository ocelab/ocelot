package it.unisa.ocelot.suites;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.suites.benchmarks.BenchmarkCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class TestSuiteGenerator {
	protected List<BenchmarkCalculator> benchmarkCalculators;
	
	public TestSuiteGenerator() {
		this.benchmarkCalculators = new ArrayList<BenchmarkCalculator>();
	}
	
	public void addBenchmark(BenchmarkCalculator pCalculator) {
		this.benchmarkCalculators.add(pCalculator);
	}
	
	protected void startBenchmarks() {
		for (BenchmarkCalculator benchmarkCalculator : this.benchmarkCalculators)
			benchmarkCalculator.start();
	}
	
	protected void measureBenchmarks(String pLabel, Set<TestCase> pSuite) {
		for (BenchmarkCalculator benchmarkCalculator : this.benchmarkCalculators)
			benchmarkCalculator.measure(pLabel, pSuite);
	}
	
	public abstract Set<TestCase> generateTestSuite() throws TestSuiteGenerationException;
}
