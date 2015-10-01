package it.unisa.ocelot.genetic.edges;

import java.util.Set;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.dominators.Dominators;
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

import org.apache.commons.lang3.Range;

import jmetal.core.Solution;
import jmetal.util.JMException;

public class EdgeCoverageProblem extends StandardProblem {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;
	private LabeledEdge target;
	private Set<CFGNode> dominators;

	private boolean debug;

	public EdgeCoverageProblem(CFG pCfg, CType[] pParameters, Range<Double>[] pRanges, int pArraySize)
			throws Exception {
		super(pParameters, pRanges, pArraySize);
		this.cfg = pCfg;
		problemName_ = "EdgeCoverageProblem";
	}
	
	public EdgeCoverageProblem(CFG pCfg, CType[] pParameters, int pArraySize) throws Exception {
		this(pCfg, pParameters, null, pArraySize);
	}

	public CFG getCFG() {
		return cfg;
	}

	public CFGNodeNavigator navigate() {
		return cfg.getStart().navigate(cfg);
	}

	public void setTarget(LabeledEdge pEdge) {
		this.target = pEdge;
		
		CFGNode parent = this.cfg.getEdgeSource(pEdge);
		
		Dominators<CFGNode, LabeledEdge> dominators = new Dominators<CFGNode, LabeledEdge>(this.cfg, this.cfg.getStart());
		
		this.dominators = dominators.getStrictDominators(parent);
	}

	public void evaluateSolution(Solution solution) throws JMException, SimulationException {
		Object[][][] arguments = this.getParameters(solution);

		CBridge bridge = getCurrentBridge();

		EventsHandler handler = new EventsHandler();
		EdgeDistanceListener bdalListener = new EdgeDistanceListener(cfg, target, dominators);
		
		try {
			bridge.getEvents(handler, arguments[0][0], arguments[1], arguments[2][0]);
		} catch (RuntimeException e) {
			this.onError(solution, e);
			return;
		}

		Simulator simulator = new Simulator(cfg, handler.getEvents());

		simulator.addListener(bdalListener);

		simulator.simulate();
		
		double objective = bdalListener.getNormalizedBranchDistance()
				+ bdalListener.getApproachLevel();
		
		solution.setObjective(0, objective);
		
		if (debug)
			System.out.println(Utils.printParameters(arguments) + "\nObjective: " + objective);
	}
}
