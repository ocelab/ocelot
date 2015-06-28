package it.unisa.ocelot.genetic.edges;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.Dominators;
import it.unisa.ocelot.c.cfg.FalseEdge;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.TrueEdge;
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
	// for approach level
	private Dominators<CFGNode, LabeledEdge> dominatorTree;
	private Set<CFGNode> dominatorNodes;
	private int shortestPath;
	private List<ExecutionEvent> nearestEvents;

	/**
	 * Constructor of EdgeDistanceListener class
	 * 
	 * @param cfg
	 *            the control flow graph
	 * @param target
	 *            the edge target
	 */
	public EdgeDistanceListener(CFG cfg, LabeledEdge target) {
		this.cfg = cfg;
		this.targetEdge = target;
		this.targetFather = this.cfg.getEdgeSource(targetEdge);
		this.dominatorTree = new Dominators<CFGNode, LabeledEdge>(cfg,
				cfg.getStart());
		this.dominatorNodes = this.dominatorTree
				.getStrictDominators(targetFather);
		this.nearestEvents = new ArrayList<ExecutionEvent>();
		this.shortestPath = Integer.MAX_VALUE;
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
		if (dominatorNodes.isEmpty())
			return 0;

		return dominatorNodes.size();
	}

	@Override
	public void onNodeVisit(CFGNode node) {
		if (this.dominatorNodes.contains(node))
			dominatorNodes.remove(node);

		List<LabeledEdge> path = DijkstraShortestPath.findPathBetween(this.cfg,
				node, this.targetFather);
		if (path != null && path.size() < this.shortestPath) {
			this.nearestNode = node;
			this.shortestPath = path.size();
		}
	}

	/**
	 * Calculates the branch distance normalized as proposed by Arcuri in
	 * "It does matter how you normalize the branch distance in search based software testing"
	 * 
	 * @return a double value of normalized branch distance
	 */
	public double getNormalizedBranchDistance() {
		double distance = 0;
		if (nearestEvents.size() == 1) {
			// single condition
			ExecutionEvent event = nearestEvents.get(0);
			if (targetEdge instanceof FalseEdge)
				distance = event.distanceFalse;
			if (targetEdge instanceof TrueEdge)
				distance = event.distanceTrue;
		}
		return distance / (distance + 1);
		// TODO switch-case distance no yet implemented
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
