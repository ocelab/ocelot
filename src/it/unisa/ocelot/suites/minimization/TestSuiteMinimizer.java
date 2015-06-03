package it.unisa.ocelot.suites.minimization;

import it.unisa.ocelot.TestCase;

import java.util.Set;

public interface TestSuiteMinimizer {
	public Set<TestCase> minimize(Set<TestCase> pTestCases);
}
