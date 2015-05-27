package it.unisa.ocelot.genetic.paths;

import java.util.ArrayList;

import org.apache.commons.lang3.Range;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;

public class PathCoverageExperiment extends Experiment {
	private Class<Object>[] parametersTypes;
	private CFG cfg;
	private ConfigManager config;
	private ArrayList<LabeledEdge> targetPath;

	public PathCoverageExperiment(CFG pCfg, ConfigManager pConfig,
			Class<Object>[] pTypes, ArrayList<LabeledEdge> targetPath) {
		this.cfg = pCfg;
		this.config = pConfig;
		this.parametersTypes = pTypes;
		this.targetPath = targetPath;
	}

	@Override
	public void algorithmSettings(String problemName, int problemId,
			Algorithm[] algorithm) throws ClassNotFoundException {
		try {
			Range<Double>[] ranges = config.getTestRanges();

			PathCoverageProblem problem;
			if (ranges != null)
				problem = new PathCoverageProblem(this.cfg,
						this.parametersTypes, ranges);
			else
				problem = new PathCoverageProblem(this.cfg,
						this.parametersTypes);

			problem.setDebug(config.getDebug());
			problem.setTarget(this.targetPath);

			PathCoverageSettings settings = new PathCoverageSettings(problem,
					config);
			algorithm[0] = settings.configure();
		} catch (Exception e) {
			System.err
					.println("An error occurred while instantiating problem: "
							+ e.getMessage());
			return;
		}
	}

}
