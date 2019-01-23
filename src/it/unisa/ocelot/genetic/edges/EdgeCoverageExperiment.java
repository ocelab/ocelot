package it.unisa.ocelot.genetic.edges;


import it.unisa.ocelot.genetic.encoding.graph.Graph;
import org.apache.commons.lang3.Range;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.OcelotExperiment;
import it.unisa.ocelot.genetic.StandardSettings;
import it.unisa.ocelot.genetic.settings.SettingsFactory;
import jmetal.core.Algorithm;

import java.util.List;

public class EdgeCoverageExperiment extends OcelotExperiment {
	private CFG cfg;
	private ConfigManager config;
	private LabeledEdge target;
	
	public EdgeCoverageExperiment(CFG pCfg, ConfigManager pConfig, List<Graph> pGraphList, LabeledEdge pTarget) {
		super(pConfig.getResultsFolder(), 1, pGraphList);
		
		this.cfg = pCfg;
		this.config = pConfig;
		this.target = pTarget;
	}
	
	@Override
	public void algorithmSettings(Algorithm[] algorithm) {		
		try {
			Range<Double>[] ranges = config.getTestRanges();
			
			EdgeCoverageProblem problem;
			if (ranges != null)
				problem = new EdgeCoverageProblem(this.cfg, this.getGraphList(), ranges);
			else
				problem = new EdgeCoverageProblem(this.cfg, this.getGraphList());
			
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
