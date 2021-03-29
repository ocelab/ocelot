package it.unisa.ocelot.suites.budget;

import it.unisa.ocelot.genetic.OcelotExperiment;
import jmetal.core.Solution;

/**
 * A basic budget handler. It simply gives to each experiment a static fraction of budget.
 * @author simone
 *
 */
public class BasicBudgetManager extends BudgetManager {
	@Deprecated
	public BasicBudgetManager() {
	}
	
	public BasicBudgetManager(int pTotalBudget, int pNumberOfTargets) {
		super(pTotalBudget, pNumberOfTargets);
		
		System.err.println("BBH: Started with " + pTotalBudget + " for " + pNumberOfTargets + "!");
	}
	
	public int getExperimentBudget(OcelotExperiment pExperiment) {
		int allocated = this.budget / this.numberOfExperiments;
		System.err.println("BBH: Allocated " + allocated + " for you.");
		return allocated; 
	};
	
	@Override
	public int askForMoreBudget(OcelotExperiment pExperiment,
			Solution pSolution) {
		System.err.println("BBH: Ehi! You greedy...");
		return 0;
	}
	
	@Override
	public void reportConsumedBudget(OcelotExperiment pExperiment, int pBudget) {
		this.budget -= pBudget;
		this.numberOfExperiments--;
		
		System.err.println("BBH: Reported " + pBudget + ". Status: "+ this.budget + " for " + this.numberOfExperiments + "!");
	}
	
	@Override
	public void updateTargets(int pNumberOfExperiments) {
		this.numberOfExperiments = pNumberOfExperiments;
		System.err.println("BBH: Now the targets are " + pNumberOfExperiments);
	}
}
