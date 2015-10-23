package it.unisa.ocelot.c.cfg;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;

public class DynamicMcCabeCalculator {
	private CFG cfg;
	private Map<LabeledEdge, Boolean> covered;
	private Stack<CFGNode> partiallyCoveredNodes;
	private List<List<LabeledEdge>> mcCabePaths;

	public DynamicMcCabeCalculator(CFG pCFG) {
		this.cfg = pCFG;
		this.covered = new HashMap<>();
		this.partiallyCoveredNodes = new Stack<>();
		this.mcCabePaths = new ArrayList<>();
		
		for (LabeledEdge edge : this.cfg.edgeSet()) {
			this.covered.put(edge, false);
		}
	}
	
	public void addPath(List<LabeledEdge> pPath) {
		this.mcCabePaths.add(pPath);
		
		for (LabeledEdge edge : pPath) {
			covered.put(edge, true);
			
			CFGNode origin = this.cfg.getEdgeSource(edge);
			
			if (partiallyCoveredNodes.contains(origin)) {
				if (isCompletelyCovered(origin))
					partiallyCoveredNodes.remove(origin);
			} else
				if (!isCompletelyCovered(origin)) {
					partiallyCoveredNodes.push(origin);
				}
		}
	}
	
	public LabeledEdge getNextTarget() {
		try {
			CFGNode node = partiallyCoveredNodes.peek();
			for (LabeledEdge edge : this.cfg.outgoingEdgesOf(node)) {
				if (!covered.get(edge)) {
					covered.put(edge, true);
					return edge;
				}
			}
			
			partiallyCoveredNodes.pop();
			return getNextTarget();
		} catch (EmptyStackException e) {
			return null;
		}
	}
	
	private boolean isCompletelyCovered(CFGNode pNode) {
		for (LabeledEdge edge : this.cfg.outgoingEdgesOf(pNode)) {
			if (!covered.get(edge))
				return false;
		}
		
		return true;
	}

	public int extimateMissingTargets() {
		int extimate = 0;
		for (CFGNode node : this.cfg.vertexSet()) {
			int totalEdges = 0;
			int coveredEdges = 0;
			for (LabeledEdge edge : this.cfg.outgoingEdgesOf(node)) {
				totalEdges++;
				if (covered.get(edge))
					coveredEdges++;
			}
			
			if (totalEdges > 1)
				extimate += (totalEdges - coveredEdges);
		}
		
		return extimate+1;
	}
	
	public int currenltyMissingTargets() {
		int missing = 0;
		
		for (CFGNode node : this.partiallyCoveredNodes) {
			for (LabeledEdge edge : this.cfg.outgoingEdgesOf(node)) {
				if (!covered.get(edge))
					missing++;
			}
		}
		
		return missing;
	}
}
