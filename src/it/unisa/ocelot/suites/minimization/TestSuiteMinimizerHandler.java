package it.unisa.ocelot.suites.minimization;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;

public class TestSuiteMinimizerHandler {
	public static final String ADDITIONAL_GREEDY_MINIMIZER = "AdditionalGreedy";
		
	public static TestSuiteMinimizer getInstance(ConfigManager pConfigManager, CFG cfg) {
		String name = pConfigManager.getTestSuiteMinimizer();
		if (name.equalsIgnoreCase(ADDITIONAL_GREEDY_MINIMIZER))
			return new AdditionalGreedyMinimizer(cfg);
		
		return null;
	}
}
