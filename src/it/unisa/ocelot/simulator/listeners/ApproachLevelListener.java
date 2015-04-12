package it.unisa.ocelot.simulator.listeners;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.simulator.ExecutionEvent;
import it.unisa.ocelot.simulator.SimulatorListener;

import java.util.List;

import org.jgrapht.alg.DijkstraShortestPath;

public class ApproachLevelListener implements SimulatorListener {
	private CFG cfg;
	private CFGNode nearest;
	private int shortestPath;
	
	public ApproachLevelListener(CFG pCFG) {
		this.cfg = pCFG;
		this.shortestPath = Integer.MAX_VALUE;
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent) {
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent, List<ExecutionEvent> pCases) {
	}

	@Override
	public void onNodeVisit(CFGNode pNode) {
		List<LabeledEdge> path = DijkstraShortestPath.findPathBetween(this.cfg, pNode, this.cfg.getTarget());
		if (path != null && path.size() < this.shortestPath) {
			this.nearest = pNode;
			this.shortestPath = path.size();
		}
	}
	
	public int getApproachLevel() {
		return shortestPath;
	}
}
