package it.unisa.ocelot.c.edge_graph;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

public class EdgeGraph<V, E> extends ListenableDirectedGraph<EdgeWrapper<E>, DefaultEdge> {
	private static final long serialVersionUID = 1483545535478173500L;
	private EdgeWrapper<E> start;
	private EdgeWrapper<E> end;

	public EdgeGraph(DirectedGraph<V, E> pGraph, V pStart, V pEnd) {
		super(DefaultEdge.class);
		
		this.generate(pGraph, pStart, pEnd);
	}
	
	public void setStart(EdgeWrapper<E> pNode) {
		this.start = pNode;
	}
	
	public EdgeWrapper<E> getStart() {
		return start;
	}
	
	public void setEnd(EdgeWrapper<E> pNode) {
		this.end = pNode;
	}
	
	public EdgeWrapper<E> getEnd() {
		return end;
	}
	
	public EdgeWrapper<E> getWrapper(E pEdge) {
		for (EdgeWrapper<E> wrapper : this.vertexSet()) {
			if (wrapper.getWrappedEdge() == null) {
				if (pEdge == null)
					return wrapper;
			} else if (wrapper.getWrappedEdge().equals(pEdge))
				return wrapper;
		}
		
		return null;
	}
	
	private void generate(DirectedGraph<V, E> pGraph, V pStart, V pEnd) {
		EdgeWrapper<E> start = new EdgeWrapper<E>(null);
		this.addVertex(start);
		this.setStart(start);
		
		EdgeWrapper<E> end = new EdgeWrapper<E>(null);
		this.addVertex(end);
		this.setEnd(end);
		
		//Creates a vertex for each node in the original graph
		Map<E, EdgeWrapper<E>> map = new HashMap<E, EdgeWrapper<E>>();
		for (E edge : pGraph.edgeSet()) {
			EdgeWrapper<E> wrapper = new EdgeWrapper<E>(edge);
			this.addVertex(wrapper);
			map.put(edge, wrapper);
		}
		
		//Sets up the start
		for (E edge : pGraph.outgoingEdgesOf(pStart)) {
			this.addEdge(start,  map.get(edge));
		}
		
		//Sets up all the edges
		for (E edge : pGraph.edgeSet()) {
			V targetNode = pGraph.getEdgeTarget(edge);
			
			for (E linkedEdge : pGraph.outgoingEdgesOf(targetNode)) {
				this.addEdge(map.get(edge), map.get(linkedEdge));
			}
		}
		
		//Sets up the end
		for (E edge : pGraph.incomingEdgesOf(pEnd)) {
			this.addEdge(map.get(edge),  end);
		}
	}
}
