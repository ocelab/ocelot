package it.unisa.ocelot.suites.benchmarks;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.simulator.CoverageCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BranchCoverageBenchmarkCalculator extends BenchmarkCalculator<Double> {
	private List<Double> coverages;
	private CoverageCalculator calculator;
	
	public BranchCoverageBenchmarkCalculator(CFG pCFG) {
		super("Branch coverage benchmark");
		
		this.coverages = new ArrayList<Double>();
		this.labels = new ArrayList<String>();
		this.calculator = new CoverageCalculator(pCFG);
	}
	
	@Override
	public void start() {
		this.coverages.add(0D);
		this.labels.add("Start 1");
	}
	
	@Override
	public void measure(String pLabel, Set<TestCase> pSuite, Integer evaluations) {
		this.calculator.calculateCoverage(pSuite);
		
		this.labels.add(this.getRealLabel(pLabel));
		this.coverages.add(this.calculator.getBranchCoverage());
	}
	
	@Override
	public void removeLast() {
		this.labels.remove(this.labels.size()-1);
		this.coverages.remove(this.coverages.size()-1);
	}

	@Override
	public Map<String, Double> getResults() {
		Map<String, Double> result = new HashMap<String, Double>();
		
		result.put(this.labels.get(0), this.coverages.get(0));
		for (int i = 0; i < this.coverages.size()-1; i++) {
			double difference = this.coverages.get(i+1) - this.coverages.get(i);
			
			result.put(this.labels.get(i+1), difference);
		}
		
		return result;
	}

	@Override
	public Map<String, Double> getCumulativeResults() {
		Map<String, Double> result = new HashMap<String, Double>();
		
		for (int i = 0; i < this.coverages.size(); i++) {
			result.put(this.labels.get(i), this.coverages.get(i));
		}
		
		return result;
	}
	
	@Override
	public List<String> getLabels() {
		return this.labels;
	}
}
