package it.unisa.ocelot.c.cfg.dominators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import it.unisa.ocelot.c.edge_graph.EdgeGraph;
import it.unisa.ocelot.c.edge_graph.EdgeWrapper;

/**
 * Compute dominator of a graph according to:
 * "A Simple, Fast Dominance Algorithm" Cooper et. al.
 * 
 * @author giograno
 *
 * @param <V>
 *            Vertex of graph
 * @param <E>
 *            Edges of graph
 */
public class EdgeDominators<V, E> implements IDominators<E> {
	private EdgeGraph<V, E> edgeGraph;
	private Dominators<EdgeWrapper<E>, DefaultEdge> dominators;

	public EdgeDominators(DirectedGraph<V, E> pGraph, V pStart, V pEnd) {
		this.edgeGraph = new EdgeGraph<V, E>(pGraph, pStart, pEnd);
		this.dominators = new Dominators<EdgeWrapper<E>, DefaultEdge>(this.edgeGraph, this.edgeGraph.getStart());
		this.dominators.computeDominators();
	}
	
	@Override
	public boolean dominates(E dominator, E dominated) {
		return this.dominators.dominates(
				this.edgeGraph.getWrapper(dominator), 
				this.edgeGraph.getWrapper(dominated));
	}
	
	@Override
	public List<E> getNonDominators() {
		List<EdgeWrapper<E>> wrappers = this.dominators.getNonDominators();
		
		List<E> nonDominators = new ArrayList<>();
		for (EdgeWrapper<E> wrapper : wrappers) {
			nonDominators.add(wrapper.getWrappedEdge());
		}
		
		nonDominators.remove(null);
		
		return nonDominators;
	}
	
	@Override
	public Set<E> getStrictDominators(E edge) {
		Set<EdgeWrapper<E>> strictDominatorsWrappers = this.dominators.getStrictDominators(this.edgeGraph.getWrapper(edge));
		Set<E> strictDominators = new HashSet<>();
		
		for (EdgeWrapper<E> wrapper : strictDominatorsWrappers) {
			strictDominators.add(wrapper.getWrappedEdge());
		}
		
		strictDominators.remove(null);
		
		return strictDominators;
	}
}