package it.unisa.ocelot.genetic.edges;

import java.util.Arrays;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.cfg.LabeledEdge;
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

public class EdgeCoverageProblem extends StandardProblem {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;
	private LabeledEdge target;

	private boolean debug;

	@SuppressWarnings("rawtypes")
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

	public void setTarget(LabeledEdge pNode) {
		this.target = pNode;
	}

	public void evaluate(Solution solution) throws JMException {
		Object[][][] arguments = this.getParameters(solution);

		CBridge bridge = new CBridge();

		EventsHandler handler = new EventsHandler();
		EdgeDistanceListener bdalListener = new EdgeDistanceListener(cfg, target);
		
		bridge.getEvents(handler, arguments[0][0], arguments[1], arguments[2][0]);

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
