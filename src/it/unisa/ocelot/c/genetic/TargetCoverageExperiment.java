package it.unisa.ocelot.c.genetic;


import java.util.Properties;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;
import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;

public class TargetCoverageExperiment extends Experiment {
	private Class<Object>[] parametersTypes;
	private CFG cfg;
	private ConfigManager config;
	
	public TargetCoverageExperiment(CFG pCfg, ConfigManager pConfig, Class<Object>[] pTypes) {
		this.cfg = pCfg;
		this.config = pConfig;
		this.parametersTypes = pTypes;
	}
	
	@Override
	public void algorithmSettings(String problemName, int problemId,
			Algorithm[] algorithm) throws ClassNotFoundException {		
		try {
			TargetCoverageProblem problem = new TargetCoverageProblem(this.cfg, this.parametersTypes);
			TargetCoverageSettings settings = new TargetCoverageSettings(problem, config); 
			algorithm[0] = settings.configure();
		} catch (Exception e) {
			System.err.println("An error occurred while instantiating problem: " + e.getMessage());
			return;
		}
	}
	
	
}
