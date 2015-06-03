package it.unisa.ocelot.suites;

import it.unisa.ocelot.c.cfg.CFG;

import it.unisa.ocelot.conf.ConfigManager;

public class TestSuiteGeneratorHandler {
	public static final String MCCABE_SUITE_GENERATOR = "McCabe";
	public static final String SINGLE_TARGET_SUITE_GENERATOR = "Target";
	
	public static TestSuiteGenerator getInstance(ConfigManager pConfigManager, CFG pCFG) {
		String name = pConfigManager.getTestSuiteGenerator();
		if (name.equalsIgnoreCase(MCCABE_SUITE_GENERATOR))
			return new McCabeTestSuiteGenerator(pConfigManager, pCFG);
		else if (name.equalsIgnoreCase(SINGLE_TARGET_SUITE_GENERATOR))
			return new SingleTargetTestSuiteGenerator(pConfigManager, pCFG);
		
		return null;
	}
}
