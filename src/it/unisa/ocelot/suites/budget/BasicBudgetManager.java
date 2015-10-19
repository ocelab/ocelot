package it.unisa.ocelot.suites.budget;

import it.unisa.ocelot.genetic.OcelotExperiment;
import jmetal.core.Solution;

/**
 * A basic budget handler. It simply gives to each experiment a static fraction of budget.
 * @author simone
 *
 */
public class BasicBudgetManager extends BudgetManager<Object> {
	private int completedExperiments;
	
	public BasicBudgetManager(int pTotalBudget, int pNumberOfTargets) {
		super(pTotalBudget, pNumberOfTargets);
		
		this.completedExperiments = 0;
	}
	
	public int getExperimentBudget(OcelotExperiment pExperiment, Object pInformationBundle) {
		return this.budget / (this.numberOfExperiments-this.completedExperiments);
	};
	
	@Override
	public int askForMoreBudget(OcelotExperiment pExperiment,
			Solution pSolution) {
		return 0;
	}
	
	public int getTargetBudget(OcelotExperiment pExperiment) {
		return this.getExperimentBudget(pExperiment, null);
	}
	
	@Override
	public void reportConsumedBudget(OcelotExperiment pExperiment, int pBudget) {
		this.budget -= pBudget;
		this.completedExperiments++;
	}
}
