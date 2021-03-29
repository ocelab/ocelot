package it.unisa.ocelot.suites.budget;

import it.unisa.ocelot.genetic.OcelotExperiment;
import jmetal.core.Solution;

public abstract class BudgetManager {
	protected int budget;
	private int totalBudget;
	protected int numberOfExperiments;
	
	public BudgetManager(int pTotalBudget, int pNumberOfTargets) {
		this.setupBudget(pTotalBudget);
		this.numberOfExperiments = pNumberOfTargets;
	}
	
	/**
	 * This method is called automatically when performing a changeTo operation. Do not call manually!
	 */
	@Deprecated
	protected BudgetManager() {
	}
	
	/**
	 * Returns the budget allocated for a given experiment.
	 * @param pExperiment
	 * @return
	 */
	public abstract int getExperimentBudget(OcelotExperiment pExperiment);
	
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
	
	public abstract void updateTargets(int pNumberOfExperiments);
	
	public BudgetManager changeTo(Class<? extends BudgetManager> pClass) throws InstantiationException {
		try {
			BudgetManager instance = pClass.newInstance();
			instance.setupBudget(this.budget);
			instance.numberOfExperiments = this.numberOfExperiments;
			return instance;
		} catch (IllegalAccessException e) {
			throw new InstantiationException(e.getMessage());
		}
	}
	
	public int getConsumedBudget() {
		return totalBudget-budget;
	}
	
	protected void setupBudget(int pBudget) {
		this.budget = pBudget;
		this.totalBudget = pBudget;
	}
}
