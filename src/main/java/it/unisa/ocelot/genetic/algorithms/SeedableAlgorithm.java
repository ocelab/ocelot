package it.unisa.ocelot.genetic.algorithms;

import jmetal.core.SolutionSet;

public interface SeedableAlgorithm {
	public void seedStartingPopulation(SolutionSet set, int keepNumber);
	
	public SolutionSet getLastPopulation();
}
