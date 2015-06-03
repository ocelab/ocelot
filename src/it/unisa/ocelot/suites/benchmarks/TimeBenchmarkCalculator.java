package it.unisa.ocelot.suites.benchmarks;

import it.unisa.ocelot.TestCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TimeBenchmarkCalculator implements BenchmarkCalculator {
	private List<Long> times;
	private List<String> labels;
	
	public TimeBenchmarkCalculator() {
		this.times = new ArrayList<Long>();
		this.labels = new ArrayList<String>();
	}
	
	@Override
	public void start() {
		this.measure("Start", null);
	}
	
	@Override
	public void measure(String pLabel, Set<TestCase> pSuite) {
		this.labels.add(pLabel);
		this.times.add(new Date().getTime());
	}

	@Override
	public String getResults() {
		String result = "";
		
		for (int i = 0; i < this.times.size()-1; i++) {
			long difference = this.times.get(i+1) - this.times.get(i);
			
			long seconds = (difference/1000) % 60;
			long minutes = (difference/60000) % 60;
			
			result += this.labels.get(i+1) + ": " + minutes + " minutes and " + seconds + " seconds.\n";
		}
		
		return result;
	}
	
	@Override
	public String getSignature() {
		String signature = "-------------------------\n";
		signature 		+= "Time benchmark\n";
		signature 		+= "-------------------------";
		
		return signature;
	}
	
	@Override
	public String toString() {
		return this.getSignature()+"\n"+this.getResults();
	}
}
