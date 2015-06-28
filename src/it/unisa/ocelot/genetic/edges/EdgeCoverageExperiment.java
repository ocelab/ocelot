package it.unisa.ocelot.genetic.edges;


import org.apache.commons.lang3.Range;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.OcelotExperiment;
import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;

public class EdgeCoverageExperiment extends OcelotExperiment {
	private Class<Object>[] parametersTypes;
	private CFG cfg;
	private ConfigManager config;
	private LabeledEdge target;
	
	public EdgeCoverageExperiment(CFG pCfg, ConfigManager pConfig, Class<Object>[] pTypes, LabeledEdge pTarget) {
		super(pConfig.getResultsFolder(), pConfig.getExperimentRuns());
		
		this.cfg = pCfg;
		this.config = pConfig;
		this.parametersTypes = pTypes;
		this.target = pTarget;
	}
	
	@Override
	public void algorithmSettings(Algorithm[] algorithm) {		
		try {
			Range<Double>[] ranges = config.getTestRanges();
			
			EdgeCoverageProblem problem;
			if (ranges != null)
				problem = new EdgeCoverageProblem(this.cfg, this.parametersTypes, ranges);
			else
				problem = new EdgeCoverageProblem(this.cfg, this.parametersTypes);
			
			problem.setDebug(config.getDebug());
			problem.setTarget(this.target);
			
			EdgeCoverageSettings settings = new EdgeCoverageSettings(problem, config);
			settings.setNumericConstants(this.cfg.getConstantNumbers());
			algorithm[0] = settings.configure();
		} catch (Exception e) {
			System.err.println("An error occurred while instantiating problem: " + e.getMessage());
			return;
		}
	}
}
