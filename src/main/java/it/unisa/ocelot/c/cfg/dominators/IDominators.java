package it.unisa.ocelot.c.cfg.dominators;

import java.util.List;
import java.util.Set;

public interface IDominators<V> {
	public boolean dominates(V dominator, V dominated);
	
	public Set<V> getStrictDominators(V node);
		
	public List<V> getNonDominators();
}
