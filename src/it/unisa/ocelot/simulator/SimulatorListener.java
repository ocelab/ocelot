package it.unisa.ocelot.simulator;

import java.util.List;

import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;

public interface SimulatorListener {
	public void onEdgeVisit(LabeledEdge pEdge);
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent);
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent, List<ExecutionEvent> pCases);
	public void onNodeVisit(CFGNode pNode);
}
