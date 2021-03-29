package it.unisa.ocelot.suites.benchmarks;

import it.unisa.ocelot.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EvaluationBenchmarkCalculator extends BenchmarkCalculator<Integer> {
	private List<Integer> no_evaluations;

	public EvaluationBenchmarkCalculator() {
		super("Number of evaluations benchmark");

		this.no_evaluations = new ArrayList<>();
		this.labels = new ArrayList<>();
	}

	@Override
	public void start() {
		this.no_evaluations.add(0);
		this.labels.add("Start 1");
	}

	@Override
	public void measure(String pLabel, Set<TestCase> pSuite, Integer evaluations) {
		this.labels.add(this.getRealLabel(pLabel));
		this.no_evaluations.add(evaluations);
	}

	@Override
	public Map<String, Integer> getResults() {
		Map<String, Integer> result = new HashMap<String, Integer>();

		result.put(this.labels.get(0), 0);

		for (int i = 1; i < this.no_evaluations.size(); i++) {
			result.put(this.labels.get(i), this.no_evaluations.get(i));
		}

		return result;
	}

	@Override
	public Map<String, Integer> getCumulativeResults() {
		Map<String, Integer> result = new HashMap<String, Integer>();

		result.put(this.labels.get(0), 0);

		int evaluationCounter = 0;
		for (int i = 1; i < this.no_evaluations.size(); i++) {
			evaluationCounter += this.no_evaluations.get(i);
			result.put(this.labels.get(i), evaluationCounter);
		}

		return result;
	}

	@Override
	public void removeLast() {
		this.labels.remove(this.labels.size() - 1);
		this.no_evaluations.remove(this.no_evaluations.size() - 1);
	}

	@Override
	public List<String> getLabels() {
		return this.labels;
	}

	@Override
	public String toString() {
		return this.getSignature() + "\n" + this.getPrintableResults();
	}

}
