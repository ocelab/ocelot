package it.unisa.ocelot.c.cfg;

public class CFGNodeNavigator {
	private CFG cfg;
	private CFGNode currentNode;
	
	public CFGNodeNavigator(CFG pCFG, CFGNode pNode) {
		this.cfg = pCFG;
		this.currentNode = pNode;
	}
	
	public CFGNodeNavigator goFlow() {
		for (LabeledEdge edge : this.cfg.outgoingEdgesOf(this.currentNode)) {
			this.currentNode = this.cfg.getEdgeTarget(edge);
		}
		
		return this;
	}
	
	public CFGNodeNavigator goTrue() {
		for (LabeledEdge edge : this.cfg.outgoingEdgesOf(this.currentNode)) {
			if (edge instanceof TrueEdge)
				this.currentNode = this.cfg.getEdgeTarget(edge);
		}
		
		return this;
	}
	
	public CFGNodeNavigator goFalse() {
		for (LabeledEdge edge : this.cfg.outgoingEdgesOf(this.currentNode)) {
			if (edge instanceof FalseEdge)
				this.currentNode = this.cfg.getEdgeTarget(edge);
		}
		
		return this;
	}
	
	public CFGNodeNavigator goCase(String pLabel) {
		for (LabeledEdge edge : this.cfg.outgoingEdgesOf(this.currentNode)) {
			if (edge instanceof CaseEdge) {
				CaseEdge caseEdge = (CaseEdge)edge;
				if (caseEdge.getLabel().toString().equals(pLabel))
					this.currentNode = this.cfg.getEdgeTarget(edge);
			}
		}
		
		return this;
	}
	
	public CFGNode node() {
		return this.currentNode;
	}
}
//cfg.getStart().goFlow().goFlow().goFalse().node()