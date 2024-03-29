package it.unisa.ocelot.genetic.paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.simulator.CaseExecutionEvent;
import it.unisa.ocelot.simulator.ExecutionEvent;
import it.unisa.ocelot.simulator.SimulatorListener;

public class PathDistanceListener implements SimulatorListener {
	@SuppressWarnings("unused")
	private CFG cfg;
	private List<LabeledEdge> targetPath;
	private List<LabeledEdge> executionPath;
	private List<ExecutionEvent> events;
	private List<List<ExecutionEvent>> caseEvents;
	private Map<LabeledEdge, Boolean> visitedEdge;
	
	private int current;
	
	@SuppressWarnings("unused")
	private int pathDistance;
	private boolean onPath;
	private boolean terminated;
	private double branchDistance;
	
	public PathDistanceListener(CFG pCFG, List<LabeledEdge> pPath) {
		this.cfg = pCFG;
		this.targetPath = pPath;
		this.executionPath = new ArrayList<LabeledEdge>();
		this.events = new ArrayList<ExecutionEvent>();
		this.caseEvents = new ArrayList<List<ExecutionEvent>>();
		
		this.pathDistance = 0;
		this.branchDistance = 0;
		
		//The index of the target path that we are analysing
		this.current = 0;
		this.onPath = true;
		this.terminated = false;
		
		this.visitedEdge = new HashMap<LabeledEdge, Boolean>();
		
		for (LabeledEdge edge : pPath)
			this.visitedEdge.put(edge, false);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
		this.onEdgeVisit(pEdge, null, null);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent) {
		this.onEdgeVisit(pEdge, pEvent, null);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent, List<ExecutionEvent> pCases) {
		this.executionPath.add(pEdge);
		this.events.add(pEvent);
		this.caseEvents.add(pCases);
		
		if (this.terminated)
			return;
		
		if (this.targetPath.contains(pEdge))
			this.visitedEdge.put(pEdge, true);
		
		if (this.onPath) {
			//If the current index is out of bound, stops the procedure
			if (current >= this.targetPath.size()) {
				this.terminated = true;
				return;
			}
			LabeledEdge target = this.targetPath.get(current);
			if (!target.equals(pEdge)) {
				this.onPath = false;
				if (pCases != null) {
					for (ExecutionEvent event : pCases) {
						if (target.matchesExecution(event))
							branchDistance += ((CaseExecutionEvent)event).distanceTrue;
					}
				} else {
					branchDistance += pEvent.distanceFalse + pEvent.distanceTrue; //only one is non-zero, so it works
				}
				
				this.pathDistance++;
			} else {
				this.visitedEdge.put(pEdge, true);
			}
		} else {
			if (this.targetPath.contains(pEdge)) {
				current = this.targetPath.indexOf(pEdge);
				this.onPath = true;
				this.visitedEdge.put(pEdge, true);
			} else
				this.pathDistance++;
		}
		
		this.current++;
	}

	@Override
	public void onNodeVisit(CFGNode pNode) {
	}
	
	public int getPathDistance() {
		int distance = 0;
		for (Entry<LabeledEdge, Boolean> entry : this.visitedEdge.entrySet()) {
			if (!entry.getValue())
				distance++;
		}
		
		return distance;//this.pathDistance;
	}
	
	public void debug() {
		for (Entry<LabeledEdge, Boolean> entry : this.visitedEdge.entrySet())
			System.out.println(entry.getKey().toString() + " : " + entry.getValue().toString());
	}
	
	public double getNormalizedBranchDistance() {
		return (this.branchDistance / (this.branchDistance + 1));
	}
	
	public double getBranchDistance() {
		return branchDistance;
	}
}
