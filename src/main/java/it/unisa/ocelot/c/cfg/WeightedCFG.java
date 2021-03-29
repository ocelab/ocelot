package it.unisa.ocelot.c.cfg;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;

import org.jgrapht.graph.ListenableDirectedWeightedGraph;

public class WeightedCFG extends ListenableDirectedWeightedGraph<CFGNode, LabeledEdge> {
	private static final long serialVersionUID = 1L;
	
	private CFG cfg;
	
	public WeightedCFG(CFG pCFG) {
		super(LabeledEdge.class);
		
		for (CFGNode node : pCFG.vertexSet())
			this.addVertex(node);
		
		for (LabeledEdge edge : pCFG.edgeSet()) {
			this.addEdge(pCFG.getEdgeSource(edge), pCFG.getEdgeTarget(edge), edge);
			this.setEdgeWeight(edge, 0);
		}
		
		this.cfg = pCFG;
	}
	
	public CFGNode getStart() {
		return this.cfg.getStart();
	}
	
	public CFGNode getEnd() {
		return this.cfg.getEnd();
	}
}
