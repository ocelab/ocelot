package it.unisa.ocelot;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.genetic.encoding.graph.Graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestCase {
	private int id;
	private Object[][][] parameters;
	private Graph graph;
	private Object oracle;
	private List<LabeledEdge> coveredPath;
	private Set<LabeledEdge> coveredEdges;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Set<LabeledEdge> getCoveredEdges() {
		return coveredEdges;
	}
	public List<LabeledEdge> getCoveredPath() {
		return coveredPath;
	}
	public void setCoveredPath(List<LabeledEdge> coveredEdges) {
		this.coveredPath = coveredEdges;
		this.coveredEdges = new HashSet<>(coveredEdges);
	}
	public Object[][][] getParameters() {
		return parameters;
	}
	public void setParameters(Object[][][] parameters) {
		this.parameters = parameters;
	}
	public Graph getGraph() {
		return graph;
	}
	public void setGraph(Graph graph) {
		this.graph = graph;
	}
	public Object getOracle() {
		return oracle;
	}
	public void setOracle(Object oracle) {
		this.oracle = oracle;
	}
}
