package it.unisa.ocelot.genetic.many_nodes;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.simulator.ExecutionEvent;
import it.unisa.ocelot.simulator.SimulatorListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;

public class ManyNodesDistanceListener implements SimulatorListener {
	private CFG cfg;
	private List<CFGNode> targetNodes;
	
	private Map<CFGNode, Integer> fixedApproachLevels;
	private int targetNodeIndex;
	
	private Map<CFGNode, Set<CFGNode>> dominators;
	
	private CFGNode nearestNode;
	
	private List<ExecutionEvent> nearestEvents;
	private CFGNode currentTarget;
	
	public ManyNodesDistanceListener(
			CFG pCFG, List<CFGNode> pTargetNodes, 
			Map<CFGNode, Integer> pFixedDistances, Map<CFGNode, Set<CFGNode>> pDominators) {
		this.cfg = pCFG;
		
		this.targetNodes = pTargetNodes;
		this.fixedApproachLevels = pFixedDistances;
		
		this.targetNodeIndex = 0;
		this.currentTarget = this.targetNodes.get(this.targetNodeIndex);
		
		this.nearestEvents = new ArrayList<ExecutionEvent>();
		
		this.dominators = new HashMap<CFGNode, Set<CFGNode>>(pDominators);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent) {
		CFGNode node = this.cfg.getEdgeSource(pEdge);

		if (node.equals(this.nearestNode)) {
			nearestEvents.clear();
			nearestEvents.add(pEvent);
		}

	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent,
			List<ExecutionEvent> pCases) {

		CFGNode node = this.cfg.getEdgeSource(pEdge);

		if (node.equals(this.nearestNode)) {
			nearestEvents.clear();
			nearestEvents.addAll(pCases);
		}
	}

	@Override
	public void onNodeVisit(CFGNode pNode) {
		if (pNode.equals(currentTarget)) {
			this.targetNodeIndex++;
			
			try {
				currentTarget = this.targetNodes.get(this.targetNodeIndex);
			} catch (IndexOutOfBoundsException e) {
				currentTarget = null;
			}
		}
		
		if (currentTarget == null)
			return;
		
		Set<CFGNode> dominators = this.dominators.get(currentTarget);
		if (dominators.contains(pNode)) {
			dominators.remove(pNode);
			this.nearestNode = pNode;
		}
	}
	
	//TODO fix here
	public int getApproachLevel() {
		//If the last target node is reached, return 0
		if (this.currentTarget == null)
			return 0;
		
		int additionalAL = this.fixedApproachLevels.get(this.currentTarget);
		
		int al = this.dominators.get(this.currentTarget).size();
		
		return additionalAL + al;
	}
	
	public void debug() {
	}
	
	public double getNormalizedBranchDistance() {
		double distance = getBranchDistance();
		
		return distance / (distance + 1);
	}
	
	public double getBranchDistance() {
		double distance = 0;

		if (this.getApproachLevel() == 0) {
			return 0D;
		}
		
		LabeledEdge nearestEdge;
		
		List<LabeledEdge> path = DijkstraShortestPath.findPathBetween(this.cfg, this.nearestNode, this.currentTarget);
		nearestEdge = path.get(0);

		// approach level more than 0
		if (nearestEvents.size() == 1) {
			// single condition
			ExecutionEvent event = nearestEvents.get(0);
			distance = Math.max(event.distanceTrue, event.distanceFalse);
		} else {
			for (ExecutionEvent event : this.nearestEvents) {
				if (event.getEdge().equals(nearestEdge))
					distance = Math.max(event.distanceTrue, event.distanceFalse);
			}
			assert distance != 0 : "non-positive distance error!";
		}
		
		return distance;
	}
}
