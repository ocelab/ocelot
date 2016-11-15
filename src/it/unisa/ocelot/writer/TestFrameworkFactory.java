package it.unisa.ocelot.writer;

public interface TestFrameworkFactory {
	/**
	 * Returns the concrete writer of the test case
	 * @return
	 */
	public TestCaseWriter getTestCaseWriterInstance(int id);
	
	/**
	 * Returns the concrete writer of the test suite. All the concrete factories have to specify
	 * the parametrized return type.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public TestSuiteWriter getTestSuiteWriterInstance();
}
