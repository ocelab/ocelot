package it.unisa.ocelot.suites.benchmarks;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.simulator.CoverageCalculator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TestSuiteSizeBenchmarkCalculator implements BenchmarkCalculator {
	private List<Integer> sizes;
	private List<String> labels;
	
	public TestSuiteSizeBenchmarkCalculator(CFG pCFG) {
		this.sizes = new ArrayList<Integer>();
		this.labels = new ArrayList<String>();
	}
	
	@Override
	public void start() {
		this.sizes.add(0);
		this.labels.add("Start");
	}
	
	@Override
	public void measure(String pLabel, Set<TestCase> pSuite) {
		this.labels.add(pLabel);
		this.sizes.add(pSuite.size());
	}

	@Override
	public String getResults() {
		String result = "";
		
		for (int i = 0; i < this.sizes.size(); i++) {
			result += this.labels.get(i) + ": " + this.sizes.get(i) + "\n";
		}
		
		return result;
	}
	
	@Override
	public String getSignature() {
		String signature = "-------------------------\n";
		signature 		+= "Test suite size benchmark\n";
		signature 		+= "-------------------------";
		
		return signature;
	}
	
	@Override
	public String toString() {
		return this.getSignature()+"\n"+this.getResults();
	}
}
