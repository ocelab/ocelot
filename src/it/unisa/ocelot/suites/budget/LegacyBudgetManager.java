package it.unisa.ocelot.suites.budget;

import it.unisa.ocelot.genetic.OcelotExperiment;
import jmetal.core.Solution;

/**
 * Null budget manager. Just a 
 * @author simone
 *
 */

public class LegacyBudgetManager extends BudgetManager {
	
	@Deprecated
	public LegacyBudgetManager() {
	}
	
	public LegacyBudgetManager(int pTotalBudget, int pNumberOfTargets) {
		super(pTotalBudget, pNumberOfTargets);
	}
	
	@Override
	public int askForMoreBudget(OcelotExperiment pExperiment, Solution pSolution) {
		return 0;
	}
	
	@Override
	public int getExperimentBudget(OcelotExperiment pExperiment) {
		System.err.println("Warning: you are using a legacy budget manager. Please, fix!");
		return this.budget;
	}
	
	public void reportConsumedBudget(OcelotExperiment pExperiment, int pConsumed) {
	};
	
	@Override
	public void updateTargets(int pNumberOfExperiments) {	
	}
}
//*/