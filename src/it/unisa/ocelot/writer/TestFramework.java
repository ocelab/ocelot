package it.unisa.ocelot.writer;

import it.unisa.ocelot.TestCase;

import java.util.HashSet;
import java.util.Set;

public class TestFramework {
	
	private TestFrameworkFactory factory;

	public TestFramework(TestFrameworkFactory pFactory) {
		this.factory = pFactory;
	}
	
	public String writeTestSuite(Set<TestCase> pSuite) {
		Set<TestCaseWriter> testCaseWriters = new HashSet<>();
		
		int id = 1;
		for (TestCase testCase : pSuite) {
			TestCaseWriter testCaseWriter = this.factory.getTestCaseWriterInstance(id);
			testCaseWriter.setTestCase(testCase);
			testCaseWriters.add(testCaseWriter);
			id++;
		}
		
		TestSuiteWriter testSuiteWriter = this.factory.getTestSuiteWriterInstance();
		
		testSuiteWriter.setTestSuite(testCaseWriters);
		
		return testSuiteWriter.write();
	}
}
