package it.unisa.ocelot.genetic.edges;

import java.util.List;

import org.apache.commons.lang3.Range;

import jmetal.core.Algorithm;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.OcelotExperiment;
import it.unisa.ocelot.genetic.StandardSettings;
import it.unisa.ocelot.genetic.settings.GASettings;

public class CDG_BasedExperiment extends OcelotExperiment {
	private CType[] parametersTypes;
	private CFG cfg;
	private ConfigManager config;
	private List<LabeledEdge> branches;
	private LabeledEdge target;

	public CDG_BasedExperiment(CFG cfg, ConfigManager configManager, CType[] types,
			List<LabeledEdge> branches, LabeledEdge target) {
		super(configManager.getResultsFolder(), configManager.getExperimentRuns());

		this.cfg = cfg;
		this.config = configManager;
		this.branches = branches;
		this.parametersTypes = types;
		this.target = target;

		this.algorithmNameList_ = new String[] { "Simple CDG-Based" };
	}

	@Override
	public void algorithmSettings(Algorithm[] algorithm) {
		try {
			Range<Double>[] ranges = config.getTestRanges();

			CDG_BasedProblem problem = null;
			if (ranges != null)
				problem = new CDG_BasedProblem(cfg, parametersTypes, config.getTestArraysSize(), ranges, branches, target);

			StandardSettings settings = new GASettings(problem, config);
			settings.useMetaMutator();
			
			settings.setNumericConstants(this.cfg.getConstantNumbers());
			problem.setDebug(config.getDebug());
			algorithm[0] = settings.configure();
		} catch (Exception e) {
			System.err.println("An error occurred while instantiating problem: " + e.getMessage());
			return;
		}

	}

}
