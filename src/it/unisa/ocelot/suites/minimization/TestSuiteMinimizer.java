package it.unisa.ocelot.suites.minimization;

import java.util.Set;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;

public abstract class TestSuiteMinimizer extends TestSuiteGenerator implements CascadeableGenerator {
	public abstract Set<TestCase> minimize(Set<TestCase> pTestCases);
	
	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite) throws TestSuiteGenerationException {
		return this.minimize(pSuite);
	}
	
	public TestSuiteMinimizer(CFG cfg) {
		super(cfg);
	}
	
	/**
	 * Always returns false, because a minimizer never increases the coverage.
	 */
	@Override
	public boolean isSatisfied() {
		return false;
	}
	
	@Override
	public boolean needsBudget() {
		return false;
	}
}
