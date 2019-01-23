package it.unisa.ocelot.suites.minimization;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;

import java.util.HashSet;
import java.util.Set;

public class AdditionalGreedyMinimizer extends TestSuiteMinimizer {
	public AdditionalGreedyMinimizer(CFG cfg) {
		super(cfg);
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
			
			if (maxTC != null) {
				selected.add(maxTC);
				covered.addAll(maxCoverage);
			}
		} while (!covered.equals(totalCovered));
		
		return selected;
	}
	
	@Override
	public int getNumberOfEvaluations() {
		return 0;
	}
}
