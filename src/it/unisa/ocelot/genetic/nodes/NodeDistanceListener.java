package it.unisa.ocelot.genetic.nodes;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.simulator.CaseExecutionEvent;
import it.unisa.ocelot.simulator.ExecutionEvent;
import it.unisa.ocelot.simulator.SimulatorListener;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.alg.DijkstraShortestPath;

public class NodeDistanceListener implements SimulatorListener {
	private CFG cfg;
	private CFGNode nearest;
	private List<ExecutionEvent> nearestEvents;
	private int shortestPath;
	private CFGNode target;
	
	public NodeDistanceListener(CFG pCFG, CFGNode pTarget) {
		this.cfg = pCFG;
		this.shortestPath = Integer.MAX_VALUE;
		this.nearestEvents = new ArrayList<ExecutionEvent>();
		this.target = pTarget;
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent) {
		CFGNode node = this.cfg.getEdgeSource(pEdge);
		
		if (node.equals(this.nearest)) {
			nearestEvents.clear();
			nearestEvents.add(pEvent);
		}
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent, List<ExecutionEvent> pCases) {
		CFGNode node = this.cfg.getEdgeSource(pEdge);
		
		if (node.equals(this.nearest)) {
			nearestEvents.clear();
			nearestEvents.addAll(pCases);
		}
	}

	@Override
	public void onNodeVisit(CFGNode pNode) {
		List<LabeledEdge> path = DijkstraShortestPath.findPathBetween(this.cfg, pNode, this.target);
		if (path != null && path.size() < this.shortestPath) {
			this.nearest = pNode;
			this.shortestPath = path.size();
		}
	}
	
	public int getApproachLevel() {
		return shortestPath;
	}
	
	public double getNormalizedBranchDistance() {
		if (getApproachLevel() == 0)
			return 0D;
		
		double distance;
		if (nearestEvents.size() == 1) {
			ExecutionEvent event = nearestEvents.get(0);
			
			distance = Math.max(event.distanceFalse, event.distanceTrue);
		} else {
			double minDistance = Double.MAX_VALUE;
			
			for (ExecutionEvent event : nearestEvents) {
				CaseExecutionEvent caseEvent = (CaseExecutionEvent)event;
				if (!caseEvent.chosen && caseEvent.distanceTrue < minDistance)
					minDistance = caseEvent.distanceTrue;
			}
			
			distance =  minDistance;
		}
		
		return distance/(distance+1);
	}
}
