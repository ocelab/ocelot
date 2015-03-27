package it.unisa.ocelot.simulator;

import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;

public interface SimulatorListener {
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent);
	public void onNodeVisit(CFGNode pNode);
}
