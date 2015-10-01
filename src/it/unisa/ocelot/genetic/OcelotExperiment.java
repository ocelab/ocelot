package it.unisa.ocelot.genetic;

import it.unisa.ocelot.genetic.algorithms.CDG_GA;
import it.unisa.ocelot.genetic.algorithms.GeneticAlgorithm;
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
	protected Algorithm algorithm;
	private Solution solution;

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
	}

	@Override
	public final void algorithmSettings(String problemName, int problemId, Algorithm[] algorithm)
			throws ClassNotFoundException {
		this.algorithmSettings(algorithm);

		this.algorithm = algorithm[0];
	}

	public abstract void algorithmSettings(Algorithm[] pAlgorithm);

	public Solution basicRun() throws ClassNotFoundException, jmetal.util.JMException {
		this.algorithmSettings(this.problemList_[0], 0, new Algorithm[1]);
		SolutionSet solutionSet = this.algorithm.execute();
		this.solution = solutionSet.get(0);
		return this.solution;
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
		if (this.algorithm instanceof GeneticAlgorithm)
			return ((GeneticAlgorithm) this.algorithm).getNumberOfEvaluation();
		if (this.algorithm instanceof CDG_GA)
			return ((CDG_GA) this.algorithm).getNo_evaluations();
		return 0;
	}
}
