package it.unisa.ocelot.genetic.paths;

import java.util.Arrays;
import java.util.List;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.nodes.NodeDistanceListener;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.SimulationException;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.util.Utils;

import org.apache.commons.lang3.Range;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.variable.ArrayReal;
import jmetal.util.JMException;

public class PathCoverageProblem extends StandardProblem {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;
	private Class<Object>[] parameters;
	private List<LabeledEdge> target;

	@SuppressWarnings("rawtypes")
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
		this.target = pPath;
	}

	public void evaluateSolution(Solution solution) throws JMException, SimulationException {
		Object[][][] arguments = this.getParameters(solution);

		CBridge bridge = getCurrentBridge();

		EventsHandler handler = new EventsHandler();
		PathDistanceListener listener = new PathDistanceListener(this.cfg,
				this.target);

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
				listener.getPathDistance() + listener.getNormalizedBranchDistance());
		
		if (debug)
			System.out.println(Utils.printParameters(arguments) + "\nBD:" + listener.getBranchDistance());
		
		if (new Double(solution.getObjective(0)).isNaN())
			solution.setObjective(0, Double.POSITIVE_INFINITY);
	}
}
