package it.unisa.ocelot.genetic.many_edges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Range;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.dominators.EdgeDominators;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.cfg.nodes.CFGNodeNavigator;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.SimulationException;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.util.Utils;
import jmetal.core.Solution;
import jmetal.util.JMException;

public class ManyEdgesCoverageProblem extends StandardProblem {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;

	private List<LabeledEdge> targetEdges;

	private Map<LabeledEdge, Integer> fixedApproachLevels;

	private Map<LabeledEdge, Set<LabeledEdge>> dominators;

	public ManyEdgesCoverageProblem(CFG pCfg, CType[] pParameters, Range<Double>[] pRanges, int pArraySize) throws Exception {
		super(pParameters, pRanges, pArraySize);
		this.cfg = pCfg;
		problemName_ = "PathCoverageProblem";
	}

	public ManyEdgesCoverageProblem(CFG pCfg, CType[] pParameters, int pArraySize) throws Exception {
		this(pCfg, pParameters, null, pArraySize);
	}

	public CFG getCFG() {
		return cfg;
	}

	public CFGNodeNavigator navigate() {
		return cfg.getStart().navigate(cfg);
	}

	@SuppressWarnings("unused")
	public void setTarget(List<LabeledEdge> pTargetEdges) {		
		this.targetEdges = pTargetEdges;
		
		EdgeDominators<CFGNode, LabeledEdge> dominators = new EdgeDominators<CFGNode, LabeledEdge>(this.cfg, this.cfg.getStart(), this.cfg.getEnd());
		
		this.fixedApproachLevels = new HashMap<LabeledEdge, Integer>();
		
		pTargetEdges.retainAll(dominators.getNonDominators());
		if (1 < 2) //Always!
			throw new RuntimeException("Hey, this has to be fixed. Check carefully the status of this class!");
		
		this.dominators = new HashMap<LabeledEdge, Set<LabeledEdge>>();
		
		Set<LabeledEdge> toRemoveDominators = new HashSet<LabeledEdge>();
		for (int i = 0; i < this.targetEdges.size(); i++) {
			LabeledEdge targetEdge = this.targetEdges.get(i);
			
			Set<LabeledEdge> currentDominators = dominators.getStrictDominators(targetEdge);
			currentDominators.removeAll(toRemoveDominators);
			toRemoveDominators.addAll(currentDominators);
			
			this.dominators.put(targetEdge, currentDominators);
		}
		
		Map<LabeledEdge, Integer> singleBranchDistances = new HashMap<LabeledEdge, Integer>();
		
		for (int i = 1; i < this.targetEdges.size(); i++) {
			LabeledEdge source = this.targetEdges.get(i-1);
			LabeledEdge target = this.targetEdges.get(i);
			
			singleBranchDistances.put(source, this.dominators.get(target).size());
		}
		singleBranchDistances.put(this.targetEdges.get(this.targetEdges.size()-1), 0);
		
		//Sets up the total distance between each node in the target nodes and the last one
		this.fixedApproachLevels.put(this.targetEdges.get(this.targetEdges.size()-1), 0);
		for (int i = this.targetEdges.size()-2; i >= 0; i--) {
			LabeledEdge edge = this.targetEdges.get(i);
			LabeledEdge nextEdge = this.targetEdges.get(i+1);
			
			this.fixedApproachLevels.put(edge, this.fixedApproachLevels.get(nextEdge) + singleBranchDistances.get(edge));
		}
		
		return;
	}

	public double evaluateSolution(Solution solution) throws JMException, SimulationException {
		Object[][][] arguments = this.getParameters(solution);

		CBridge bridge = getCurrentBridge();

		EventsHandler handler = new EventsHandler();
		ManyEdgesDistanceListener listener = 
				new ManyEdgesDistanceListener(this.cfg, this.targetEdges, this.fixedApproachLevels, this.dominators);

		try {
			bridge.getEvents(handler, arguments[0][0], arguments[1], arguments[2][0]);
		} catch (RuntimeException e) {
			this.onError(solution, e);
			return -1;
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
		
		return listener.getBranchDistance();
	}
}
