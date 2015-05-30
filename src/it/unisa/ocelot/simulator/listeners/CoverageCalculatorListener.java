package it.unisa.ocelot.simulator.listeners;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.simulator.ExecutionEvent;
import it.unisa.ocelot.simulator.SimulatorListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoverageCalculatorListener implements SimulatorListener {
	private CFG cfg;
	private Set<LabeledEdge> coveredEdges;
	private List<CFGNode> coveredNodes;
	public CoverageCalculatorListener(CFG pCFG) {
		this.cfg = pCFG;
		this.coveredEdges = new HashSet<LabeledEdge>();
		this.coveredNodes = new ArrayList<CFGNode>();
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
		if (!this.coveredEdges.contains(pEdge))
			this.coveredEdges.add(pEdge);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent) {
		this.onEdgeVisit(pEdge);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent, List<ExecutionEvent> pCases) {
		this.onEdgeVisit(pEdge);
	}

	@Override
	public void onNodeVisit(CFGNode pNode) {
		if (!this.coveredNodes.contains(pNode))
			this.coveredNodes.add(pNode);
	}
	
	public Set<LabeledEdge> getCoveredEdges() {
		return new HashSet<LabeledEdge>(this.coveredEdges);
	}
	
	public Set<LabeledEdge> getUncoveredEdges() {
		Set<LabeledEdge> allEdges = new HashSet<LabeledEdge>(this.cfg.edgeSet());
		allEdges.removeAll(this.coveredEdges);
		return allEdges;
	}
	
	public double getBranchCoverage() {
		Set<LabeledEdge> edges = cfg.edgeSet();
		return this.coveredEdges.size() / (double)edges.size(); 
	}
	
	public double getBlockCoverage() {
		Set<CFGNode> nodes = cfg.vertexSet();
		return (this.coveredNodes.size()+1) / (double)nodes.size(); 
	}

}
