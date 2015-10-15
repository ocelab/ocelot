package it.unisa.ocelot.writer;

import java.util.Set;

public abstract class TestSuiteWriter<T extends TestCaseWriter> {
	protected Set<T> testSuite;
	
	public void setTestSuite(Set<T> testSuite) {
		this.testSuite = testSuite;
	}
	
	public Set<T> getTestSuite() {
		return testSuite;
	}
	
	public abstract String write();
}
