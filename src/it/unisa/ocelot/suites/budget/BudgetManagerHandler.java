package it.unisa.ocelot.suites.budget;

import it.unisa.ocelot.conf.ConfigManager;

public class BudgetManagerHandler {
	
	public static final String BASIC_MANAGER = "Basic";
	public static final String ADAPTIVE_MANAGER = "Adaptive";
	
	public static BudgetManager<Object> getInstance(ConfigManager pConfigManager, int pTotalBudget, int pNumberOfExperiments) {
		return getInstance(pConfigManager.getBudgetManager(), pConfigManager, pTotalBudget, pNumberOfExperiments);
	}
	
	public static BudgetManager<Object> getInstance(String name, ConfigManager pConfigManager, int pTotalBudget, int pNumberOfExperiments) {
		if (name.equalsIgnoreCase(BASIC_MANAGER)) {
			return new BasicBudgetManager(pTotalBudget, pNumberOfExperiments);
		}
		
		throw new RuntimeException("ERROR: No budget handler " + name + " found!");
	}
}
