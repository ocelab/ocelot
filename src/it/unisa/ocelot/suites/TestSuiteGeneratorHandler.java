package it.unisa.ocelot.suites;

import it.unisa.ocelot.c.cfg.CFG;

import it.unisa.ocelot.conf.ConfigManager;

public class TestSuiteGeneratorHandler {
	public static final String MCCABE_SUITE_GENERATOR = "McCabe";
	public static final String SINGLE_TARGET_SUITE_GENERATOR = "Target";
	public static final String REDUCED_MCCABE_SUITE_GENERATOR = "ReducedMcCabe";
	//generator for only McCabe Path without considering non covered branches 
	public static final String VANILLA_MCCABE_SUITE_GENERATOR = "Vanilla";
	
	public static TestSuiteGenerator getInstance(ConfigManager pConfigManager, CFG pCFG) {
		String name = pConfigManager.getTestSuiteGenerator();
		if (name.equalsIgnoreCase(MCCABE_SUITE_GENERATOR))
			return new McCabeTestSuiteGenerator(pConfigManager, pCFG);
		else if(name.equalsIgnoreCase(VANILLA_MCCABE_SUITE_GENERATOR))
			return new VanillaMcCabeTestSuiteGenerator(pConfigManager, pCFG);
		else if (name.equalsIgnoreCase(SINGLE_TARGET_SUITE_GENERATOR))
			return new SingleTargetTestSuiteGenerator(pConfigManager, pCFG);
		else if (name.equalsIgnoreCase(REDUCED_MCCABE_SUITE_GENERATOR))
			return new ReducedMcCabeTestSuiteGenerator(pConfigManager, pCFG);
		
		return null;
	}
}
