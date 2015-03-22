package it.unisa.ocelot.simulator;

import it.unisa.ocelot.cfg.CFGNode;
import it.unisa.ocelot.cfg.LabeledEdge;

public interface SimulatorListener {
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent);
	public void onNodeVisit(CFGNode pNode);
}
