package it.unisa.ocelot.genetic.edges;


import org.apache.commons.lang3.Range;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.OcelotExperiment;
import it.unisa.ocelot.genetic.StandardSettings;
import it.unisa.ocelot.genetic.settings.SettingsFactory;
import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;

public class EdgeCoverageExperiment extends OcelotExperiment {
	private CType[] parametersTypes;
	private CFG cfg;
	private ConfigManager config;
	private LabeledEdge target;
	
	public EdgeCoverageExperiment(CFG pCfg, ConfigManager pConfig, CType[] pTypes, LabeledEdge pTarget) {
		super(pConfig.getResultsFolder(), 1);
		
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
				problem = new EdgeCoverageProblem(this.cfg, this.parametersTypes, ranges, this.config.getTestArraysSize());
			else
				problem = new EdgeCoverageProblem(this.cfg, this.parametersTypes, this.config.getTestArraysSize());
			
			problem.setDebug(config.getDebug());
			problem.setTarget(this.target);
			
			StandardSettings settings = SettingsFactory.getSettings(config.getAlgorithm(), problem, config);
			if (config.isMetaMutatorEnabled())
				settings.useMetaMutator();
			settings.setNumericConstants(this.cfg.getConstantNumbers());
			algorithm[0] = settings.configure();
		} catch (Exception e) {
			System.err.println("An error occurred while instantiating problem: " + e.getMessage());
			return;
		}
	}
}
