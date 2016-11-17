package it.unisa.ocelot.writer;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;

import java.util.HashSet;
import java.util.Set;

public class TestFramework {
	
	private TestFrameworkFactory factory;

	public TestFramework(TestFrameworkFactory pFactory) {
		this.factory = pFactory;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String writeTestSuite(Set<TestCase> pSuite, CFG pCfg, ConfigManager pConfig) throws TestWritingException {
		Set<TestCaseWriter> testCaseWriters = new HashSet<>();
		
		int id = 1;
		for (TestCase testCase : pSuite) {
			TestCaseWriter testCaseWriter = this.factory.getTestCaseWriterInstance(id);
			testCaseWriter.setCfg(pCfg);
			testCaseWriter.setTestCase(testCase);
			testCaseWriters.add(testCaseWriter);
			id++;
		}
		
		TestSuiteWriter testSuiteWriter = this.factory.getTestSuiteWriterInstance();
		testSuiteWriter.setCfg(pCfg);
		
		testSuiteWriter.setTestSuite(testCaseWriters);
		
		return testSuiteWriter.write(pConfig);
	}
}
