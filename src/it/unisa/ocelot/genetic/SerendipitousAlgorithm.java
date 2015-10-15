package it.unisa.ocelot.genetic;

import java.util.Set;

import jmetal.core.Solution;

public interface SerendipitousAlgorithm<E> {
	public Set<Solution> getSerendipitousSolutions();
	public void setSerendipitousPotentials(Set<E> pPotentials);
}
