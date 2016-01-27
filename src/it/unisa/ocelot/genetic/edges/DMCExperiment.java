package it.unisa.ocelot.genetic.edges;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.algorithms.SeedableAlgorithm;
import jmetal.core.Algorithm;
import jmetal.core.SolutionSet;

public class DMCExperiment extends EdgeCoverageExperiment {
	private SolutionSet seedPopulation;
	private int keepOfSeedPopulation;
	
	public DMCExperiment(CFG pCfg, ConfigManager pConfig, CType[] pTypes, LabeledEdge pTarget, 
			SolutionSet seedPopulation, int keepOfSeedPopulation) {
		super(pCfg, pConfig, pTypes, pTarget);
		
		this.seedPopulation = seedPopulation;
		this.keepOfSeedPopulation = keepOfSeedPopulation;
	}
	
	@Override
	public void algorithmSettings(Algorithm[] algorithm) {
		super.algorithmSettings(algorithm);
		
		if (algorithm[0] instanceof SeedableAlgorithm) {
			((SeedableAlgorithm)algorithm[0]).seedStartingPopulation(this.seedPopulation, this.keepOfSeedPopulation);
		}
	}
	
	public SolutionSet getLastPopulation() {
		if (this.algorithm instanceof SeedableAlgorithm) {
			return ((SeedableAlgorithm)this.algorithm).getLastPopulation();
		} else
			return null;
	}
}
