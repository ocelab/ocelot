package it.unisa.ocelot.writer;

import it.unisa.ocelot.TestCase;

public abstract class TestCaseWriter {
	protected TestCase testCase;
	protected String name;
	
	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}
	
	public TestCase getTestCase() {
		return testCase;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public abstract String write();
}
