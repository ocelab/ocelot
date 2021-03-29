package it.unisa.ocelot.c.cfg.nodes;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.CaseEdge;
import it.unisa.ocelot.c.cfg.edges.FalseEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.edges.TrueEdge;

import java.util.ArrayList;
import java.util.List;

public class CFGNodeNavigator {
	private CFG cfg;
	private CFGNode currentNode;
	private List<LabeledEdge> edges;
	
	public CFGNodeNavigator(CFG pCFG, CFGNode pNode) {
		this.cfg = pCFG;
		this.currentNode = pNode;
		this.edges = new ArrayList<LabeledEdge>();
	}
	
	public CFGNodeNavigator goFlow() {
		LabeledEdge flowEdge = null;
		for (LabeledEdge edge : this.cfg.outgoingEdgesOf(this.currentNode)) {
			flowEdge = edge;
		}
		
		if (flowEdge != null) {
			this.currentNode = this.cfg.getEdgeTarget(flowEdge);
			this.edges.add(flowEdge);
		}
		
		return this;
	}
	
	public CFGNodeNavigator goTrue() {
		for (LabeledEdge edge : this.cfg.outgoingEdgesOf(this.currentNode)) {
			if (edge instanceof TrueEdge) {
				this.currentNode = this.cfg.getEdgeTarget(edge);
				this.edges.add(edge);
			}
		}
		
		return this;
	}
	
	public CFGNodeNavigator goFalse() {
		for (LabeledEdge edge : this.cfg.outgoingEdgesOf(this.currentNode)) {
			if (edge instanceof FalseEdge) {
				this.currentNode = this.cfg.getEdgeTarget(edge);
				this.edges.add(edge);
			}
		}
		
		return this;
	}
	
	public CFGNodeNavigator goCase(String pLabel) {
		for (LabeledEdge edge : this.cfg.outgoingEdgesOf(this.currentNode)) {
			if (edge instanceof CaseEdge) {
				CaseEdge caseEdge = (CaseEdge)edge;
				if (caseEdge.getLabel().toString().equals(pLabel)) {
					this.currentNode = this.cfg.getEdgeTarget(edge);
					this.edges.add(caseEdge);
				}
			}
		}
		
		return this;
	}
	
	/**
	 * Returns the node navigated
	 * @return
	 */
	public CFGNode node() {
		return this.currentNode;
	}
	
	public LabeledEdge edge() {
		return this.edges.get(this.edges.size()-1);
	}
	
	/**
	 * Returns the path navigated
	 * @return
	 */
	public List<LabeledEdge> path() {
		return this.edges;
	}

	@Override
	public String toString() {
		return "CFGNodeNavigator [currentNode=" + currentNode + ", edges="
				+ edges + "]";
	}
}
//cfg.getStart().goFlow().goFlow().goFalse().node()