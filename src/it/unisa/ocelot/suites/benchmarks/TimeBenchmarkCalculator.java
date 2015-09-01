package it.unisa.ocelot.suites.benchmarks;

import it.unisa.ocelot.TestCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TimeBenchmarkCalculator extends BenchmarkCalculator<Integer> {
	private List<Long> times;
	
	public TimeBenchmarkCalculator() {
		super("Time benchmark");
		
		this.times = new ArrayList<Long>();
		this.labels = new ArrayList<String>();
	}
	
	@Override
	public void start() {
		this.measure("Start", null, null);
	}
	
	@Override
	public void measure(String pLabel, Set<TestCase> pSuite, Integer evaluations) {
		this.labels.add(this.getRealLabel(pLabel));
		this.times.add(new Date().getTime());
	}
	
	@Override
	public void removeLast() {
		this.labels.remove(this.labels.size()-1);
		this.times.remove(this.times.size()-1);
	}
	
	@Override
	public String toString() {
		return this.getSignature()+"\n"+this.getPrintableResults();
	}
	
	@Override
	public Map<String, Integer> getResults() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		result.put(this.labels.get(0), 0);
		for (int i = 0; i < this.times.size()-1; i++) {
			long difference = this.times.get(i+1) - this.times.get(i);
			
			long seconds = (difference/1000) % 60;
			
			result.put(this.labels.get(i+1), (int)seconds);
		}
		
		return result;
	}

	@Override
	public Map<String, Integer> getCumulativeResults() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		long first = this.times.get(0);
		for (int i = 0; i < this.times.size(); i++) {
			long difference = this.times.get(i) - first;
			
			long seconds = (difference/1000);
			
			result.put(this.labels.get(i), (int)seconds);
		}
		
		return result;
	}
	
	@Override
	public List<String> getLabels() {
		return this.labels;
	}
}
