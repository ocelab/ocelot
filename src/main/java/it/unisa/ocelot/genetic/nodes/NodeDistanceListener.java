package it.unisa.ocelot.genetic.nodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.simulator.ExecutionEvent;
import it.unisa.ocelot.simulator.SimulatorListener;

/**
 * Listener that calculates branch distance and approach level. The targets are the nodes. 
 * @author simone
 *
 */
public class NodeDistanceListener implements SimulatorListener {
	private CFG cfg;
	private CFGNode nearest;
	private List<ExecutionEvent> nearestEvents;
	private Set<CFGNode> dominators;
	private CFGNode target;
	
	public NodeDistanceListener(CFG pCFG, CFGNode pTarget, Set<CFGNode> pDominators) {
		this.cfg = pCFG;
		this.nearestEvents = new ArrayList<ExecutionEvent>();
		this.target = pTarget;
		
		this.dominators = new HashSet<CFGNode>(pDominators);
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
		if (this.dominators.contains(pNode)) {
			this.dominators.remove(pNode);
			this.nearest = pNode;
		}
	}
	
	public int getApproachLevel() {
		return this.dominators.size();
	}
	
	public double getBranchDistance() {
		if (getApproachLevel() == 0)
			return 0D;
		
		if (nearestEvents.size() == 1) {
			ExecutionEvent event = nearestEvents.get(0);
			
			//One of the two is 0, the other is our branch distance, because it is the
			//distance from the nearest node.
			return Math.max(event.distanceFalse, event.distanceTrue);
		} else {
			
			List<LabeledEdge> path = DijkstraShortestPath.findPathBetween(this.cfg, this.nearest, this.target);
			LabeledEdge nearestEdge = path.get(0);
			
			for (ExecutionEvent event : nearestEvents) {
				if (event.getEdge().equals(nearestEdge))
					return Math.max(event.distanceTrue, event.distanceFalse);
			}
			
			assert false: "An error occurred in NodeDistanceListener...";
			return 10000.0;
		}
	}
	
	public double getNormalizedBranchDistance() {
		double distance = this.getBranchDistance();
		
		return distance/(distance+1);
	}
}
