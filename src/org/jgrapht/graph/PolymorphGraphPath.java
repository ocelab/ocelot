package org.jgrapht.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graph;

public class PolymorphGraphPath<V, E extends Comparable<E>> {
	private List<E> edgePath;
	private List<V> nodePath;
	private Graph<V, E> graph;
	private double[] vectorPath;

	public PolymorphGraphPath(Graph<V, E> pGraph) {
		this.edgePath = new ArrayList<E>();
		this.nodePath = new ArrayList<V>();
		this.vectorPath = new double[0];
		
		this.graph = pGraph;
	}
	
	public void createFromEdges(List<E> path) {
		this.edgePath = path;
		
		this.createNodePath();
		
		this.createVectorPath();
	}
	
	public void createFromNodes(List<V> path) {
		this.nodePath = path;
		
		this.createEdgePath();
		
		this.createVectorPath();
	}
	
	public List<E> getEdgePath() {
		return this.edgePath;
	}
	
	public List<V> getNodePath() {
		return this.nodePath;
	}
	
	private void createNodePath() {
		this.nodePath = new ArrayList<V>();
		if (this.edgePath.size() == 0)
			return;
		
		V first = this.graph.getEdgeSource(this.edgePath.get(0));
		this.nodePath.add(first);
		for (E edge : this.edgePath) {
			V next = this.graph.getEdgeTarget(edge);
			this.nodePath.add(next);
		}
	}
	
	private void createEdgePath() {
		this.edgePath = new ArrayList<E>();
		for (int i = 0; i < this.nodePath.size() - 1; i++) {
			V startNode = this.nodePath.get(i);
			V endNode = this.nodePath.get(i + 1);
			this.edgePath.add(this.graph.getEdge(startNode, endNode));
		}
	}
	
	private void createVectorPath() {
		this.vectorPath = new double[this.graph.edgeSet().size()];
		List<E> orderedEdges = new ArrayList<E>(this.graph.edgeSet());
		if (orderedEdges.size() == 0)
			return;
		
		Collections.sort(orderedEdges);
		
		for (int i = 0; i < orderedEdges.size(); i++) {
			E edge = orderedEdges.get(i);
			
			this.vectorPath[i] = Collections.frequency(this.edgePath, edge);
		}
	}
}
