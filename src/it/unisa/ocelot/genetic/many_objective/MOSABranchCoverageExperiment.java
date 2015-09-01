package it.unisa.ocelot.genetic.many_objective;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CaseEdge;
import it.unisa.ocelot.c.cfg.FalseEdge;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.TrueEdge;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.MOSA;
import it.unisa.ocelot.genetic.OcelotExperiment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Algorithm;
import jmetal.core.SolutionSet;

import org.apache.commons.lang3.Range;

/**
 * A Branch Coverage experiment performed with MOSA algorithm proposed by
 * Panichella et al. in
 * "Reformulating Branch Coverage as a Many-Objective Optimization Problem" ICST
 * 2015
 * 
 * @author giograno
 *
 */
public class MOSABranchCoverageExperiment extends OcelotExperiment {
	private CType[] parametersTypes;
	private CFG cfg;
	private ConfigManager config;
	// all branches to cover (except FlowEdges)
	private List<LabeledEdge> branches;

	public MOSABranchCoverageExperiment(CFG cfg, ConfigManager configManager, CType[] types) {
		super(configManager.getResultsFolder(), configManager.getExperimentRuns());

		this.cfg = cfg;
		this.config = configManager;
		this.parametersTypes = types;

		this.algorithmNameList_ = new String[] { "MOSA" };
		// this.branches = getTargetsFromCFG();
		this.branches = cfg.getBranchesFromCFG();
	}

	@Override
	public void algorithmSettings(Algorithm[] algorithm) {
		try {
			Range<Double>[] ranges = config.getTestRanges();

			MOSABranchCoverageProblem problem = null;
			if (ranges != null)
				problem = new MOSABranchCoverageProblem(this.cfg, this.parametersTypes, config.getTestArraysSize(), 
						ranges, branches);

			MOSABranchCoverageSettings settings = new MOSABranchCoverageSettings(problem, config);
			settings.setNumericConstants(this.cfg.getConstantNumbers());
			problem.setDebug(config.getDebug());
			algorithm[0] = settings.configure();
		} catch (Exception e) {
			System.err.println("An error occurred while instantiating problem: " + e.getMessage());
			return;
		}
	}

	public SolutionSet multiObjectiveRun() throws ClassNotFoundException, jmetal.util.JMException {
		this.algorithmSettings(this.problemList_[0], 0, new Algorithm[1]);
		SolutionSet solutionSet = this.algorithm.execute();
		// restore all branch as uncovered for next experiments
		for (LabeledEdge edge : this.branches)
			edge.setUncovered();
		return solutionSet;
	}

	public List<Integer> getNumberOfEvaluations() {
		return ((MOSA) this.algorithm).getEvaluations();
	}
}
