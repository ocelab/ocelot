package it.unisa.ocelot.genetic.many_nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.cfg.Dominators;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.SimulationException;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.util.Utils;

import org.apache.commons.lang3.Range;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.PolymorphGraphPath;

import jmetal.core.Solution;
import jmetal.util.JMException;

public class PathCoverageProblem extends StandardProblem {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;

	private ArrayList<CFGNode> targetNodes;

	private Map<CFGNode, Integer> fixedApproachLevels;

	private Map<CFGNode, Set<CFGNode>> dominators;

	public PathCoverageProblem(CFG pCfg, CType[] pParameters, Range<Double>[] pRanges, int pArraySize) throws Exception {
		super(pParameters, pRanges, pArraySize);
		this.cfg = pCfg;
		problemName_ = "PathCoverageProblem";
	}

	public PathCoverageProblem(CFG pCfg, CType[] pParameters, int pArraySize) throws Exception {
		this(pCfg, pParameters, null, pArraySize);
	}

	public CFG getCFG() {
		return cfg;
	}

	public CFGNodeNavigator navigate() {
		return cfg.getStart().navigate(cfg);
	}

	public void setTarget(List<LabeledEdge> pPath) {		
		this.targetNodes = new ArrayList<CFGNode>();
		
		Dominators<CFGNode, LabeledEdge> dominators = new Dominators<CFGNode, LabeledEdge>(this.cfg, this.cfg.getStart());
		PolymorphGraphPath<CFGNode, LabeledEdge> path = new PolymorphGraphPath<>(this.cfg);
		
		path.createFromEdges(pPath);
		
		//Keeps only the nodes in the path that are non-dominators
		this.targetNodes.addAll(path.getNodePath());
		this.targetNodes.retainAll(dominators.getNonDominators());
		this.targetNodes.remove(this.cfg.getEnd());
		
		this.fixedApproachLevels = new HashMap<CFGNode, Integer>();
		
		this.dominators = new HashMap<CFGNode, Set<CFGNode>>();
		
		Set<CFGNode> toRemoveDominators = new HashSet<CFGNode>();
		for (int i = 0; i < this.targetNodes.size(); i++) {
			CFGNode targetNode = this.targetNodes.get(i);
			
			Set<CFGNode> currentDominators = dominators.getStrictDominators(targetNode);
			currentDominators.removeAll(toRemoveDominators);
			toRemoveDominators.addAll(currentDominators);
			
			this.dominators.put(targetNode, currentDominators);
		}
		
		Map<CFGNode, Integer> singleBranchDistances = new HashMap<CFGNode, Integer>();
		
		//Calculates single distances between each pair of near target nodes
		CFGNode current = this.targetNodes.get(0);
		for (int i = 1; i < this.targetNodes.size(); i++) {
			DijkstraShortestPath<CFGNode, LabeledEdge> shortestPath = 
					new DijkstraShortestPath<CFGNode, LabeledEdge>(this.cfg, current, this.targetNodes.get(i));
			
			singleBranchDistances.put(current, (int)shortestPath.getPathLength());
			
			current = this.targetNodes.get(i);
		}
		singleBranchDistances.put(current, 0);
		
		//Sets up the total distance between each node in the target nodes and the last one
		this.fixedApproachLevels.put(this.targetNodes.get(this.targetNodes.size()-1), 0);
		for (int i = this.targetNodes.size()-2; i >= 0; i--) {
			CFGNode node = this.targetNodes.get(i);
			CFGNode nextNode = this.targetNodes.get(i+1);
			
			this.fixedApproachLevels.put(node, this.fixedApproachLevels.get(nextNode) + singleBranchDistances.get(node));
		}
		
		return;
	}

	public void evaluateSolution(Solution solution) throws JMException, SimulationException {
		Object[][][] arguments = this.getParameters(solution);

		CBridge bridge = getCurrentBridge();

		EventsHandler handler = new EventsHandler();
		ManyNodesDistanceListenerDomTree listener = 
				new ManyNodesDistanceListenerDomTree(this.cfg, this.targetNodes, this.fixedApproachLevels, this.dominators);

		try {
			bridge.getEvents(handler, arguments[0][0], arguments[1], arguments[2][0]);
		} catch (RuntimeException e) {
			this.onError(solution, e);
			return;
		}

		Simulator simulator = new Simulator(cfg, handler.getEvents());

		simulator.addListener(listener);

		simulator.simulate();

		solution.setObjective(0,
				listener.getApproachLevel() + listener.getNormalizedBranchDistance());
		
		if (debug)
			System.out.println(Utils.printParameters(arguments) + "\nBD:" + listener.getBranchDistance());
		
		if (new Double(solution.getObjective(0)).isNaN())
			solution.setObjective(0, Double.POSITIVE_INFINITY);
	}
}
