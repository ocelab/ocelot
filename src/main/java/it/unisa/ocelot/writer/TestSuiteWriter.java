package it.unisa.ocelot.writer;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;

import java.util.Set;

public abstract class TestSuiteWriter<T extends TestCaseWriter> {
	protected Set<T> testSuite;
	protected CFG cfg;
	
	public void setCfg(CFG cfg) {
		this.cfg = cfg;
	}
	
	public CFG getCfg() {
		return cfg;
	}
	
	public void setTestSuite(Set<T> testSuite) {
		this.testSuite = testSuite;
	}
	
	public Set<T> getTestSuite() {
		return testSuite;
	}
	
	public abstract String write(ConfigManager pConfigManager) throws TestWritingException;
}
