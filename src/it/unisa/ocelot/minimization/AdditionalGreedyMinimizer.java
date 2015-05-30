package it.unisa.ocelot.minimization;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.LabeledEdge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdditionalGreedyMinimizer implements MinimizationAlgorithm {
	public AdditionalGreedyMinimizer() {
	}
	
	@Override
	public Set<TestCase> minimize(Set<TestCase> pTestCases) {
		Set<TestCase> selected = new HashSet<TestCase>();
		Set<LabeledEdge> covered = new HashSet<LabeledEdge>();
		Set<LabeledEdge> totalCovered = new HashSet<LabeledEdge>();
		
		for (TestCase tc : pTestCases)
			totalCovered.addAll(tc.getCoveredEdges());
		
		do {
			Set<LabeledEdge> maxCoverage = new HashSet<LabeledEdge>();
			TestCase maxTC = null;
			
			//Search the TC with max additional coverage
			for (TestCase tc : pTestCases) {
				Set<LabeledEdge> localCoverage = new HashSet<LabeledEdge>(tc.getCoveredEdges());
				localCoverage.removeAll(covered);
				if (localCoverage.size() > maxCoverage.size()) {
					maxCoverage = localCoverage;
					maxTC = tc;
				}
			}
			
			selected.add(maxTC);
			covered.addAll(maxCoverage);
		} while (!covered.equals(totalCovered));
		
		return selected;
	}
	
}
