package it.unisa.ocelot.genetic;

import java.util.HashSet;
import java.util.Set;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.genetic.algorithms.AlgorithmStats;
import it.unisa.ocelot.suites.budget.BudgetManager;
import jmetal.core.Algorithm;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.experiments.Experiment;
import jmetal.experiments.Settings;

/**
 * Experiment with a single run, which allows to handle directly the solution
 * 
 * @author simone
 *
 */
public abstract class OcelotExperiment extends Experiment {
	protected OcelotAlgorithm algorithm;
	private Solution solution;
	private Set<LabeledEdge> serendipitousPotentials;
	private Set<Solution> serendipitousSolutions;
	
	protected BudgetManager budgetManager;
	
	public void initExperiment(BudgetManager pBudgetManager) {
		this.budgetManager = pBudgetManager;
		this.initExperiment();
	}

	public OcelotExperiment(String pResultsFolder, int pRuns) {
		this.experimentName_ = "TargetCoverage";
		this.algorithmNameList_ = new String[] { "PGGA" };
		this.problemList_ = new String[] { "TestCoverage" };

		this.paretoFrontFile_ = new String[2];

		this.indicatorList_ = new String[] { "HV", "SPREAD", "EPSILON" };

		int numberOfAlgorithms = this.algorithmNameList_.length;

		this.experimentBaseDirectory_ = pResultsFolder;

		this.algorithmSettings_ = new Settings[numberOfAlgorithms];

		this.independentRuns_ = pRuns;
		
		this.serendipitousPotentials = new HashSet<>();
		this.serendipitousSolutions = new HashSet<>();
	}

	@Override
	public final void algorithmSettings(String problemName, int problemId, Algorithm[] algorithm)
			throws ClassNotFoundException {
		this.algorithmSettings(algorithm);

		if (algorithm[0] instanceof OcelotAlgorithm)
			this.algorithm = (OcelotAlgorithm)algorithm[0];
		else
			throw new RuntimeException("Experiment error: you chose a non-Ocelot algorithm. Please, adapt it.");
	}

	public abstract void algorithmSettings(Algorithm[] pAlgorithm);
	
	public void setSerendipitousPotentials(Set<LabeledEdge> pPotentials) {
		this.serendipitousPotentials = pPotentials;
	}
	
	public AlgorithmStats getAlgorithmStats() {
		if (this.algorithm instanceof OcelotAlgorithm)
			return ((OcelotAlgorithm) this.algorithm).getStats();
		else
			return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Solution basicRun() throws ClassNotFoundException, jmetal.util.JMException {
		this.algorithmSettings(this.problemList_[0], 0, new Algorithm[1]);
		
		SerendipitousAlgorithm serendipitousAlgorithm = null;
		if (this.algorithm instanceof SerendipitousAlgorithm<?>)
			serendipitousAlgorithm = ((SerendipitousAlgorithm) this.algorithm);
				
		
		int iterations = this.budgetManager.getExperimentBudget(this);
		algorithm.setInputParameter("maxEvaluations", iterations);
		
		if (serendipitousAlgorithm != null)
			serendipitousAlgorithm.setSerendipitousPotentials(this.serendipitousPotentials);
		
		SolutionSet solutionSet = this.algorithm.execute();
		this.solution = solutionSet.get(0);
		
		if (serendipitousAlgorithm != null)
			this.serendipitousSolutions = serendipitousAlgorithm.getSerendipitousSolutions();
		
		this.budgetManager.reportConsumedBudget(this, this.algorithm.getStats().getEvaluations());
		
		if (this.solution.getObjective(0) != 0.0) {
			int moreBudget = this.budgetManager.askForMoreBudget(this, this.solution);
			
			if (moreBudget > 0) {
				algorithm.extraExecute(moreBudget);
				
				if (serendipitousAlgorithm != null)
					this.serendipitousSolutions.addAll(serendipitousAlgorithm.getSerendipitousSolutions());
			}
		}
		
		return this.solution;
	}
	
	public Set<Solution> getSerendipitousSolutions() {
		return this.serendipitousSolutions;
	}

	public double getFitnessValue() {
		return this.getFitnessValue(0);
	}

	public double getFitnessValue(int i) {
		return this.solution.getObjective(i);
	}

	public Variable[] getVariables() {
		return this.solution.getDecisionVariables();
	}
	
	public Solution getSolution() {
		return solution;
	}

	public int getNumberOfEvaluation() {
		if (this.algorithm instanceof OcelotAlgorithm)
			return ((OcelotAlgorithm) this.algorithm).getStats().getEvaluations();
		/*
		if (this.algorithm instanceof GeneticAlgorithm)
			return ((GeneticAlgorithm) this.algorithm).getNumberOfEvaluation();
		if (this.algorithm instanceof CDG_GA)
			return ((CDG_GA) this.algorithm).getNo_evaluations();
		*/
		return 0;
	}
}
