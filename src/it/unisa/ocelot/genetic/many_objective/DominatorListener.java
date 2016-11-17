package it.unisa.ocelot.genetic.many_objective;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.dominators.Dominators;
import it.unisa.ocelot.c.cfg.edges.FalseEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.edges.TrueEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.simulator.ExecutionEvent;
import it.unisa.ocelot.simulator.SimulatorListener;

/**
 * Listener needed to calculate normalized branches distance and approach level
 * of a target edge. This listener is deprecated. The unified EdgeDistanceListener
 * is used instead.
 * 
 * @author giograno
 *
 */
@Deprecated
public class DominatorListener implements SimulatorListener {

	private CFG cfg;
	private LabeledEdge target;
	private CFGNode parentNode;
	private static Dominators<CFGNode, LabeledEdge> dominatorTree;
	private Set<CFGNode> dominatorNodes;
	private CFGNode nearestNode;
	private LabeledEdge nearestEdge;
	private int shortestPath;
	private List<ExecutionEvent> nearestEvents;

	private boolean targetVisited;

	@SuppressWarnings("static-access")
	public DominatorListener(CFG cfg, LabeledEdge target) {
		this.cfg = cfg;
		this.target = target;
		this.parentNode = this.cfg.getEdgeSource(target);
		if (dominatorTree == null)
			this.dominatorTree = new Dominators<CFGNode, LabeledEdge>(cfg, cfg.getStart());
		this.dominatorNodes = this.dominatorTree.getStrictDominators(parentNode);
		this.nearestEvents = new ArrayList<ExecutionEvent>();
		this.shortestPath = Integer.MAX_VALUE;

		this.targetVisited = false;
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent) {

		if (!targetVisited) {
			CFGNode node = this.cfg.getEdgeSource(pEdge);

			if (node.equals(this.nearestNode)) {
				nearestEvents.clear();
				nearestEvents.add(pEvent);
			}
		}
		
		if (pEdge.equals(this.target))
			targetVisited = true;

	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent, List<ExecutionEvent> pCases) {
		CFGNode node = this.cfg.getEdgeSource(pEdge);

		if (node.equals(this.nearestNode)) {
			nearestEvents.clear();
			nearestEvents.addAll(pCases);
		}
	}

	@Override
	public void onNodeVisit(CFGNode pNode) {
		if (this.dominatorNodes.contains(pNode))
			dominatorNodes.remove(pNode);

		if (dominatorNodes.size() < this.shortestPath) {
			this.nearestNode = pNode;
			shortestPath = dominatorNodes.size();
			if (dominatorNodes.size() == 0) {
				List<LabeledEdge> outgoingEdges = new ArrayList<>(this.cfg.outgoingEdgesOf(pNode));
				this.nearestEdge = outgoingEdges.get(0);
			} else
				this.nearestEdge = this.target;
		}
	}

	public int getApproachLevel() {
		if (dominatorNodes.isEmpty())
			return 0;

		return dominatorNodes.size();
	}
	
	public double getBranchDistance() {
		double distance = 0.D;

		if (this.getApproachLevel() == 0.D) {
			// execution reach the parent node of edge target
			ExecutionEvent event = nearestEvents.get(0);

			if (target instanceof FalseEdge)
				distance = event.distanceFalse;
			if (target instanceof TrueEdge)
				distance = event.distanceTrue;
			return distance;
		}

		// approach level more than 0
		if (nearestEvents.size() == 1) {
			// single condition
			ExecutionEvent event = nearestEvents.get(0);
			distance = Math.max(event.distanceTrue, event.distanceFalse);
		} else {
			for (ExecutionEvent event : this.nearestEvents) {
				if (event.getEdge().equals(this.nearestEdge))
					distance = Math.max(event.distanceTrue, event.distanceFalse);
			}
		}
		return distance;
	}

	/**
	 * Calculates the branch distance normalized as proposed by Arcuri in
	 * "It does matter how you normalize the branch distance in search based software testing"
	 * 
	 * @return a double value of normalized branch distance
	 */
	public double getNormalizedBranchDistance() {
		double branchDistance = this.getBranchDistance();
		return branchDistance / (branchDistance + 1);
	}

	/**
	 * Return the value of the fitness function, scored by branch distance plus
	 * approach level
	 * 
	 * @return the fitness value
	 */
	public double getFitness() {
		return this.getApproachLevel() + this.getNormalizedBranchDistance();
	}
}
