package it.unisa.ocelot.genetic.many_edges;

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
import java.util.Map.Entry;

import org.jgrapht.alg.DijkstraShortestPath;

public class ManyEdgesDistanceListener implements SimulatorListener {
	private CFG cfg;
	private List<LabeledEdge> targetEdges;
	
	private Map<LabeledEdge, Integer> fixedApproachLevels;
	private int targetEdgeIndex;
	
	private Map<LabeledEdge, Set<LabeledEdge>> dominators;
	
	private LabeledEdge nearestEdge;
	
	private List<ExecutionEvent> nearestEvents;
	private LabeledEdge currentTarget;
	
	private List<LabeledEdge> DEBUGTRACE;
	
	public ManyEdgesDistanceListener(
			CFG pCFG, List<LabeledEdge> pTargetEdges, 
			Map<LabeledEdge, Integer> pFixedDistances, Map<LabeledEdge, Set<LabeledEdge>> pDominators) {
		this.cfg = pCFG;
		
		this.targetEdges = pTargetEdges;
		this.fixedApproachLevels = pFixedDistances;
		
		this.targetEdgeIndex = 0;
		this.currentTarget = this.targetEdges.get(this.targetEdgeIndex);
		
		this.nearestEvents = new ArrayList<ExecutionEvent>();
		
		this.dominators = new HashMap<LabeledEdge, Set<LabeledEdge>>();
		
		//Hard cloning dominators
		for (Entry<LabeledEdge, Set<LabeledEdge>> entry : pDominators.entrySet()) {
			this.dominators.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}
		
		this.DEBUGTRACE = new ArrayList<>();
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
		this.DEBUGTRACE.add(pEdge);
		
		this.onEdgeVisitGeneric(pEdge);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent) {
		this.DEBUGTRACE.add(pEdge);
		
		this.onEdgeVisitGeneric(pEdge);

		if (nearestEdge == null)
			return;
		
		CFGNode node = this.cfg.getEdgeTarget(this.nearestEdge);
		if (this.nearestEdge.equals(pEdge) || this.cfg.outgoingEdgesOf(node).contains(pEdge)) {
			nearestEvents.clear();
			nearestEvents.add(pEvent);
		}

	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent,
			List<ExecutionEvent> pCases) {
		this.DEBUGTRACE.add(pEdge);
		
		this.onEdgeVisitGeneric(pEdge);
		
		if (nearestEdge == null)
			return;

		CFGNode node = this.cfg.getEdgeTarget(this.nearestEdge);
		if (this.nearestEdge.equals(pEdge) || this.cfg.outgoingEdgesOf(node).contains(pEdge)) {
			nearestEvents.clear();
			nearestEvents.addAll(pCases);
		}
	}
	
	private void onEdgeVisitGeneric(LabeledEdge pEdge) {
		if (pEdge.equals(currentTarget)) {
			this.targetEdgeIndex++;
			
			try {
				currentTarget = this.targetEdges.get(this.targetEdgeIndex);
			} catch (IndexOutOfBoundsException e) {
				currentTarget = null;
			}
		}
		
		if (currentTarget == null)
			return;
		
		Set<LabeledEdge> dominators = this.dominators.get(currentTarget);
		if (dominators.contains(pEdge)) {
			dominators.remove(pEdge);
			this.nearestEdge = pEdge;
		}
	}

	@Override
	public void onNodeVisit(CFGNode pNode) {
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
		
		List<LabeledEdge> shortestPath = DijkstraShortestPath.findPathBetween(
				this.cfg,
				this.cfg.getEdgeTarget(this.nearestEdge), 
				this.cfg.getEdgeTarget(this.currentTarget));
		
		LabeledEdge eventEdge = shortestPath.get(0);
		// approach level more than 0
		if (nearestEvents.size() == 1) {
			// single condition
			ExecutionEvent event = nearestEvents.get(0);
			distance = Math.max(event.distanceTrue, event.distanceFalse);
		} else {
			for (ExecutionEvent event : this.nearestEvents) {
				if (event.getEdge().equals(eventEdge))
					distance = Math.max(event.distanceTrue, event.distanceFalse);
			}
			assert distance != 0 : "non-positive distance error!";
		}
		
		return distance;
	}
}
