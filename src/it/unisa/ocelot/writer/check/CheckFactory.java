package it.unisa.ocelot.writer.check;

import it.unisa.ocelot.writer.TestCaseWriter;
import it.unisa.ocelot.writer.TestFrameworkFactory;
import it.unisa.ocelot.writer.TestSuiteWriter;

public class CheckFactory implements TestFrameworkFactory {

	@Override
	public TestSuiteWriter<CheckTestCaseWriter> getTestSuiteWriterInstance() {
		return new CheckTestSuiteWriter();
	}

	@Override
	public TestCaseWriter getTestCaseWriterInstance(int id) {
		return new CheckTestCaseWriter(id);
	}

}
