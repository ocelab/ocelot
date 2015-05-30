package it.unisa.ocelot.genetic.nodes;


import org.apache.commons.lang3.Range;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.OcelotExperiment;
import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;

public class TargetCoverageExperiment extends OcelotExperiment {
	private Class<Object>[] parametersTypes;
	private CFG cfg;
	private ConfigManager config;
	private CFGNode target;
	
	public TargetCoverageExperiment(CFG pCfg, ConfigManager pConfig, Class<Object>[] pTypes, CFGNode pTarget) {
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
			
			TargetCoverageProblem problem;
			if (ranges != null)
				problem = new TargetCoverageProblem(this.cfg, this.parametersTypes, ranges);
			else
				problem = new TargetCoverageProblem(this.cfg, this.parametersTypes);
			
			problem.setDebug(config.getDebug());
			problem.setTarget(this.target);
			
			TargetCoverageSettings settings = new TargetCoverageSettings(problem, config); 
			algorithm[0] = settings.configure();
		} catch (Exception e) {
			System.err.println("An error occurred while instantiating problem: " + e.getMessage());
			return;
		}
	}
}
