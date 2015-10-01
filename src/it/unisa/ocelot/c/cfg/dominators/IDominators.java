package it.unisa.ocelot.c.cfg.dominators;

import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public interface IDominators<V> {
	public boolean dominates(V dominator, V dominated);
	
	public Set<V> getStrictDominators(V node);
		
	public List<V> getNonDominators();
}
