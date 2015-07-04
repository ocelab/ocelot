package it.unisa.ocelot.suites.benchmarks;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.simulator.CoverageCalculator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestSuiteSizeBenchmarkCalculator extends BenchmarkCalculator<Integer> {
	private List<Integer> sizes;
	private List<String> labels;
	
	public TestSuiteSizeBenchmarkCalculator(CFG pCFG) {
		super("Test suite size benchmark");
		
		this.sizes = new ArrayList<Integer>();
		this.labels = new ArrayList<String>();
	}
	
	@Override
	public void start() {
		this.sizes.add(0);
		this.labels.add("Start 1");
	}
	
	@Override
	public void measure(String pLabel, Set<TestCase> pSuite) {
		this.labels.add(this.getRealLabel(pLabel));
		this.sizes.add(pSuite.size());
	}
	
	@Override
	public void removeLast() {
		this.labels.remove(this.labels.size()-1);
		this.sizes.remove(this.sizes.size()-1);
	}
	
	@Override
	public String toString() {
		return this.getSignature()+"\n"+this.getPrintableResults();
	}
	
	@Override
	public Map<String, Integer> getResults() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		result.put(this.labels.get(0), this.sizes.get(0));
		for (int i = 0; i < this.sizes.size()-1; i++) {
			int difference = this.sizes.get(i+1) - this.sizes.get(i);
			
			result.put(this.labels.get(i+1), difference);
		}
		
		return result;
	}

	@Override
	public Map<String, Integer> getCumulativeResults() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		for (int i = 0; i < this.sizes.size(); i++) {
			result.put(this.labels.get(i), this.sizes.get(i));
		}
		
		return result;
	}
	
	@Override
	public List<String> getLabels() {
		return this.labels;
	}
}
