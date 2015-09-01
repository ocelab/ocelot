package it.unisa.ocelot.suites.benchmarks;

import it.unisa.ocelot.TestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BenchmarkCalculator<T> {
	private String name;
	private Map<String, Integer> labelCategories;
	protected List<String> labels;
	
	public BenchmarkCalculator(String pName) {
		this.name = pName;
		this.labelCategories = new HashMap<String, Integer>();
	}
	
	public abstract void start();
	
	public abstract void measure(String pLabel, Set<TestCase> pSuite, Integer evalations);
	
	public String getPrintableResults() {
		String result = "";
		
		for (Map.Entry<String, T> entry : this.getCumulativeResults().entrySet()) {
			result += entry.getKey() + ": " + entry.getValue() + "\n";
		}
		
		return result;
	}

	public String getPrintableCumulativeResults() {
		String result = "";
		Map<String, T> results = this.getCumulativeResults();
		for (String label : this.labels) {
			result += label + ": " + results.get(label).toString() + "\n";
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return this.getSignature()+"\n"+this.getPrintableResults();
	}
	
	public abstract Map<String, T> getResults();
	public abstract Map<String, T> getCumulativeResults();
	public abstract void removeLast();
	public abstract List<String> getLabels();
	
	public String getSignature() {
		return this.name;
	}
	
	protected String getRealLabel(String pLabel) {
		if (labelCategories.containsKey(pLabel)) {
			labelCategories.put(pLabel, labelCategories.get(pLabel) + 1);
		} else {
			labelCategories.put(pLabel, 1);
		}
		
		return pLabel + " " + labelCategories.get(pLabel);
	}
}
