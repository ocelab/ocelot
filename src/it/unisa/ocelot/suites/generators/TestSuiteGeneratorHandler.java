package it.unisa.ocelot.suites.generators;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.suites.generators.edge.SingleTargetTestSuiteGenerator;
import it.unisa.ocelot.suites.generators.mccabe.McCabeTestSuiteGenerator;
import it.unisa.ocelot.suites.generators.mccabe.ReducedMcCabeTestSuiteGenerator;
import it.unisa.ocelot.suites.generators.mccabe.VanillaMcCabeTestSuiteGenerator;
import it.unisa.ocelot.suites.generators.random.RandomTestSuiteGenerator;

public class TestSuiteGeneratorHandler {
	public static final String MCCABE_SUITE_GENERATOR = "McCabe";
	public static final String SINGLE_TARGET_SUITE_GENERATOR = "Target";
	public static final String REDUCED_MCCABE_SUITE_GENERATOR = "ReducedMcCabe";
	//generator for only McCabe Path without considering non covered branches 
	public static final String VANILLA_MCCABE_SUITE_GENERATOR = "Vanilla";
	public static final String RANDOM_SUITE_GENERATOR = "Random";
	
	public static TestSuiteGenerator getInstance(ConfigManager pConfigManager, CFG pCFG) {
		return getInstance(pConfigManager.getTestSuiteGenerator(), pConfigManager, pCFG);
	}
	
	public static TestSuiteGenerator getInstance(String name, ConfigManager pConfigManager, CFG pCFG) {
		if (name.equalsIgnoreCase(MCCABE_SUITE_GENERATOR))
			return new McCabeTestSuiteGenerator(pConfigManager, pCFG);
		else if(name.equalsIgnoreCase(VANILLA_MCCABE_SUITE_GENERATOR))
			return new VanillaMcCabeTestSuiteGenerator(pConfigManager, pCFG);
		else if (name.equalsIgnoreCase(SINGLE_TARGET_SUITE_GENERATOR))
			return new SingleTargetTestSuiteGenerator(pConfigManager, pCFG);
		else if (name.equalsIgnoreCase(REDUCED_MCCABE_SUITE_GENERATOR))
			return new ReducedMcCabeTestSuiteGenerator(pConfigManager, pCFG);
		else if (name.equalsIgnoreCase(RANDOM_SUITE_GENERATOR))
			return new RandomTestSuiteGenerator(pConfigManager, pCFG);
		
		return null;
	}
}
