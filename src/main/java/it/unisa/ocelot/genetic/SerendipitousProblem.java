package it.unisa.ocelot.genetic;

import java.util.Set;

public interface SerendipitousProblem<E> {
	public Set<E> getSerendipitousCovered();
	public void setSerendipitousPotentials(Set<E> pPotentials);
}
