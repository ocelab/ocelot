package it.unisa.ocelot.genetic.nodes;

import java.util.Arrays;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.NodePrinterListener;
import it.unisa.ocelot.util.Utils;

import org.apache.commons.lang3.Range;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.variable.ArrayReal;
import jmetal.util.JMException;

public class NodeCoverageProblem extends StandardProblem {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;
	private CFGNode target;

	private boolean debug;

	public NodeCoverageProblem(CFG pCfg, CType[] pParameters, Range<Double>[] pRanges, int pArraySize)
			throws Exception {
		super(pParameters, pRanges, pArraySize);
		this.cfg = pCfg;
		problemName_ = "NodeCoverageProblem";
	}
	
	public NodeCoverageProblem(CFG pCfg, CType[] pParameters, int pArraySize) throws Exception {
		this(pCfg, pParameters, null, pArraySize);
	}

	public CFG getCFG() {
		return cfg;
	}

	public CFGNodeNavigator navigate() {
		return cfg.getStart().navigate(cfg);
	}

	public void setTarget(CFGNode pNode) {
		this.target = pNode;
	}

	public void evaluate(Solution solution) throws JMException {
		Object[][][] arguments = this.getParameters(solution);

		CBridge.initialize(arguments);
		CBridge bridge = new CBridge();

		EventsHandler handler = new EventsHandler();
		NodeDistanceListener bdalListener = new NodeDistanceListener(cfg, target);
		
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
