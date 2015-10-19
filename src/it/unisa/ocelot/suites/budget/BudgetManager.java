package it.unisa.ocelot.suites.budget;

import it.unisa.ocelot.genetic.OcelotExperiment;
import jmetal.core.Solution;

public abstract class BudgetManager<T> {
	protected int budget;
	protected int numberOfExperiments;
	
	public BudgetManager(int pTotalBudget, int pNumberOfTargets) {
		this.budget = pTotalBudget;
		this.numberOfExperiments = pNumberOfTargets;
	}
	
	/**
	 * Returns the budget allocated for a given experiment.
	 * @param pExperiment
	 * @return
	 */
	public abstract int getExperimentBudget(OcelotExperiment pExperiment, T pInformationBundle);
	
	/**
	 * Asks for an extra budget. If the handler allows it, this method returns the amount of extra
	 * iterations allowed. Otherwise, returns 0.
	 * @param pExperiment Current experiment.
	 * @param pSolution Solution found
	 * @return 0 if no more budget can be allocated, the number of extra evaluations otherwise.
	 */
	public abstract int askForMoreBudget(OcelotExperiment pExperiment, Solution pSolution);
	
	/**
	 * Reports the amount of budget actually consumed by an experiment.
	 * @param pExperiment Experiment
	 * @param pConsumed Budget consumed
	 */
	public abstract void reportConsumedBudget(OcelotExperiment pExperiment, int pConsumed);
}
