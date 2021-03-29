package it.unisa.ocelot.suites.budget;

import it.unisa.ocelot.conf.ConfigManager;

public class BudgetManagerHandler {
	
	public static final String BASIC_MANAGER = "Basic";
	public static final String ADAPTIVE_MANAGER = "Adaptive";
	public static final String LEGACY_MANAGER = "Legacy";
	
	public static BudgetManager getInstance(ConfigManager pConfigManager, int pNumberOfExperiments) {
		return getInstance(pConfigManager.getBudgetManager(), pConfigManager.getMaxEvaluations(), pNumberOfExperiments);
	}
	
	public static BudgetManager getInstance(String name, int pTotalBudget, int pNumberOfExperiments) {
		if (name.equalsIgnoreCase(BASIC_MANAGER)) {
			return new BasicBudgetManager(pTotalBudget, pNumberOfExperiments);
		} else if (name.equalsIgnoreCase(LEGACY_MANAGER)) {
			return new LegacyBudgetManager(pTotalBudget, pNumberOfExperiments);
		}
		
		throw new RuntimeException("ERROR: No budget handler " + name + " found!");
	}
}
