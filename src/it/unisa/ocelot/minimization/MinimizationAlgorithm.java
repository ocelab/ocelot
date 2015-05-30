package it.unisa.ocelot.minimization;

import it.unisa.ocelot.TestCase;

import java.util.Set;

public interface MinimizationAlgorithm {
	public Set<TestCase> minimize(Set<TestCase> pTestCases);
}
