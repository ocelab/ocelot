package it.unisa.ocelot.writer;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;

public abstract class TestCaseWriter {
	protected TestCase testCase;
	protected String name;
	protected CFG cfg;
	
	public void setCfg(CFG cfg) {
		this.cfg = cfg;
	}
	
	public CFG getCfg() {
		return cfg;
	}
	
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
	
	public abstract String write(ConfigManager pConfigManager) throws TestWritingException;
}
