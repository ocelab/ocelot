package it.unisa.ocelot.suites.benchmarks;

import it.unisa.ocelot.TestCase;

import java.util.Set;

public interface BenchmarkCalculator {
	public void start();
	
	public void measure(String pLabel, Set<TestCase> pSuite);
	
	public String getResults();
	
	public String getSignature();
}
