package it.unisa.ocelot.genetic.edges;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.simulator.ExecutionEvent;
import it.unisa.ocelot.simulator.SimulatorListener;

/**
 * A listener class used to calculate distances about edges
 * 
 * @author giograno
 *
 */
public class EdgeDistanceListener implements SimulatorListener {

	private CFG cfg;
	private CFGNode nearestNode;
	// the edge target
	private LabeledEdge targetEdge;
	// the node targeted by edge target
	private CFGNode targetFather;
	private List<ExecutionEvent> nearestEvents;
	
	private Set<CFGNode> dominators;

	/**
	 * Constructor of EdgeDistanceListener class
	 * 
	 * @param cfg
	 *            the control flow graph
	 * @param target
	 *            the edge target
	 */
	public EdgeDistanceListener(CFG cfg, LabeledEdge target, Set<CFGNode> pDominators) {
		this.cfg = cfg;
		this.targetEdge = target;
		this.targetFather = this.cfg.getEdgeSource(targetEdge);

		this.nearestEvents = new ArrayList<ExecutionEvent>();
		
		this.dominators = new HashSet<CFGNode>(pDominators);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
		// pass
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

	/**
	 * Calculates the approach level
	 * 
	 * @return an approach level int value
	 */
	public int getApproachLevel() {
		return this.dominators.size();
	}

	@Override
	public void onNodeVisit(CFGNode node) {
		if (this.dominators.contains(node)) {
			dominators.remove(node);
			this.nearestNode = node;
		}
	}
	
	public double getBranchDistance() {
		if (this.getApproachLevel() == 0) {
			// execution has reached the parent node of edge target
			if (this.nearestEvents.size() == 1) {
				ExecutionEvent event = nearestEvents.get(0);
				
				if (event.getEdge().equals(this.targetEdge))
					return 0;
				else
					return Math.max(event.distanceTrue, event.distanceFalse);
			} else {
				for (ExecutionEvent event : this.nearestEvents) {
					if (event.getEdge().equals(this.targetEdge))
						return event.distanceTrue;
				}
			}
			
			assert false: "Something went wrong in EdgeDistanceListener...";
			return 10000.0;
		}

		// approach level more than 0
		if (nearestEvents.size() == 1) {
			// single condition
			ExecutionEvent event = nearestEvents.get(0);
			return Math.max(event.distanceTrue, event.distanceFalse);
		} else {
			List<LabeledEdge> path = DijkstraShortestPath.findPathBetween(this.cfg, this.nearestNode, this.targetFather);
			LabeledEdge nearestEdge = path.get(0);
			
			for (ExecutionEvent event : this.nearestEvents) {
				if (event.getEdge().equals(nearestEdge))
					return Math.max(event.distanceTrue, event.distanceFalse);
			}
			
			assert false: "Something went wrong in EdgeDistanceListener...";
			return 10000.0;
		}
	}

	/**
	 * Calculates the branch distance normalized as proposed by Arcuri in
	 * "It does matter how you normalize the branch distance in search based software testing"
	 * 
	 * @return a double value of normalized branch distance
	 */
	public double getNormalizedBranchDistance() {
		double distance = this.getBranchDistance();

		return distance / (distance + 1);
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
