package it.unisa.ocelot.genetic.edges;

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
	
	private boolean forceCovered;
	private Set<LabeledEdge> serendipitousPotentials;
	private Set<LabeledEdge> serendipitousCovered;
	
	private Set<List<ExecutionEvent>> pairEventSets;

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
		this.forceCovered = false;
		
		this.serendipitousCovered = new HashSet<>();
		this.serendipitousPotentials = new HashSet<>();
				
		this.pairEventSets = new HashSet<>();
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
		if (pEdge.equals(targetEdge))
			forceCovered = true;
		
		if (this.serendipitousPotentials.contains(pEdge))
			this.serendipitousCovered.add(pEdge);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent) {
		if (pEdge.equals(targetEdge))
			forceCovered = true;
		
		if (this.serendipitousPotentials.contains(pEdge))
			this.serendipitousCovered.add(pEdge);
		
		if (forceCovered)
			return;
		
		CFGNode node = this.cfg.getEdgeSource(pEdge);

		if (node.equals(this.nearestNode)) {
			nearestEvents.clear();
			nearestEvents.add(pEvent);
			this.pairEventSets.add(new ArrayList<>(nearestEvents));
		}

	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent,
			List<ExecutionEvent> pCases) {
		if (pEdge.equals(targetEdge)) {
			forceCovered = true;
		}
		
		if (this.serendipitousPotentials.contains(pEdge))
			this.serendipitousCovered.add(pEdge);
		
		if (forceCovered)
			return;

		CFGNode node = this.cfg.getEdgeSource(pEdge);

		if (node.equals(this.nearestNode)) {
			nearestEvents.clear();
			nearestEvents.addAll(pCases);
			this.pairEventSets.add(new ArrayList<>(nearestEvents));
		}
	}

	/**
	 * Calculates the approach level
	 * 
	 * @return an approach level int value
	 */
	public int getApproachLevel() {
		if (forceCovered)
			return 0;
		
		return this.dominators.size();
	}

	@Override
	public void onNodeVisit(CFGNode node) {
		if (this.dominators.contains(node)) {
			dominators.remove(node);
			this.nearestNode = node;
			this.pairEventSets.clear();
		}
	}
	
	public double getBranchDistance() {
		if (forceCovered)
			return 0;
		
		double minDistance = Double.MAX_VALUE;
		if (this.getApproachLevel() == 0) {
			for (List<ExecutionEvent> nearestEvents : this.pairEventSets) {
				// execution has reached the parent node of edge target
				if (nearestEvents.size() == 1) {
					ExecutionEvent event = nearestEvents.get(0);
					
					if (event.getEdge().equals(this.targetEdge))
						return 0;
					else {
						double distance = Math.max(event.distanceTrue, event.distanceFalse); 
						if (distance < minDistance)
							minDistance = distance;
					}
				} else {
					for (ExecutionEvent event : nearestEvents) {
						if (event.getEdge().equals(this.targetEdge)) {
							double distance = event.distanceTrue; 
							if (distance < minDistance)
								minDistance = distance;
						}
					}
				}
				
//				System.err.println("Something went wrong in EdgeDistanceListener...");
//				assert false: "Something went wrong in EdgeDistanceListener...";
//				return 10000.0;
			}
		}

		for (List<ExecutionEvent> nearestEvents : this.pairEventSets) {
			// approach level more than 0
			if (nearestEvents.size() == 1) {
				// single condition
				ExecutionEvent event = nearestEvents.get(0);
				double distance = Math.max(event.distanceTrue, event.distanceFalse); 
				if (distance < minDistance)
					minDistance = distance;
			} else {
				List<LabeledEdge> path = DijkstraShortestPath.findPathBetween(this.cfg, nearestNode, this.targetFather);
				LabeledEdge nearestEdge; 
				
				if (path.size() > 0)
					nearestEdge = path.get(0);
				else
					nearestEdge = this.targetEdge;
				
				for (ExecutionEvent event : nearestEvents) {
					if (event.getEdge().equals(nearestEdge)) {
						double distance = Math.max(event.distanceTrue, event.distanceFalse);
						if (distance < minDistance)
							minDistance = distance;
					}
				}
				
//				System.err.println("Something went wrong in EdgeDistanceListener...");
//				assert false: "Something went wrong in EdgeDistanceListener...";
//				return 10000.0;
			}
		}
		
		return minDistance;
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

	public void setSerendipitousPotentials(
			Set<LabeledEdge> serendipitousPotentials) {
		this.serendipitousPotentials = serendipitousPotentials;
	}
	
	public Set<LabeledEdge> getSerendipitousCovered() {
		return serendipitousCovered;
	}
}
