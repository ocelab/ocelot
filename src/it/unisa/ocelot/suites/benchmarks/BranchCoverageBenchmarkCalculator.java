package it.unisa.ocelot.suites.benchmarks;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.simulator.CoverageCalculator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class BranchCoverageBenchmarkCalculator implements BenchmarkCalculator {
	private List<Double> coverages;
	private List<String> labels;
	private CoverageCalculator calculator;
	
	public BranchCoverageBenchmarkCalculator(CFG pCFG) {
		this.coverages = new ArrayList<Double>();
		this.labels = new ArrayList<String>();
		this.calculator = new CoverageCalculator(pCFG);
	}
	
	@Override
	public void start() {
		this.coverages.add(0D);
		this.labels.add("Start");
	}
	
	@Override
	public void measure(String pLabel, Set<TestCase> pSuite) {
		this.calculator.calculateCoverage(pSuite);
		
		this.labels.add(pLabel);
		this.coverages.add(this.calculator.getBranchCoverage());
	}

	@Override
	public String getResults() {
		String result = "";
		
		for (int i = 0; i < this.coverages.size(); i++) {
			result += this.labels.get(i) + ": " + this.coverages.get(i) + "\n";
		}
		
		return result;
	}
	
	@Override
	public String getSignature() {
		String signature = "-------------------------\n";
		signature 		+= "Branch cov. benchmark\n";
		signature 		+= "-------------------------";
		
		return signature;
	}
	
	@Override
	public String toString() {
		return this.getSignature()+"\n"+this.getResults();
	}
}
