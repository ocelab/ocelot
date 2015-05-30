package it.unisa.ocelot.genetic.paths;

import java.util.Arrays;
import java.util.List;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.nodes.NodeDistanceListener;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;

import org.apache.commons.lang3.Range;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.variable.ArrayReal;
import jmetal.util.JMException;

public class PathCoverageProblem extends Problem {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;
	private Class<Object>[] parameters;
	private List<LabeledEdge> target;

	private boolean debug;

	@SuppressWarnings("rawtypes")
	public PathCoverageProblem(CFG pCfg, Class[] pParameters,
			Range<Double>[] pRanges) throws Exception {

		this.cfg = pCfg;

		numberOfVariables_ = pParameters.length;
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;
		problemName_ = "TestingPrioritizationProblem";

		solutionType_ = new ArrayRealSolutionType(this);

		length_ = new int[numberOfVariables_];
		lowerLimit_ = new double[numberOfVariables_];
		upperLimit_ = new double[numberOfVariables_];
		for (int i = 0; i < numberOfVariables_; i++) {
			if (pRanges != null && pRanges.length > i && pRanges[i] != null) {
				lowerLimit_[i] = pRanges[i].getMinimum();
				upperLimit_[i] = pRanges[i].getMaximum();
			} else {
				if (pParameters[i] == Integer.class) {
					lowerLimit_[i] = Integer.MIN_VALUE;
					upperLimit_[i] = Integer.MAX_VALUE;
				} else {
					lowerLimit_[i] = Double.MIN_VALUE;
					upperLimit_[i] = Double.MAX_VALUE;
				}
			}
		}

		this.parameters = pParameters;
	}

	public PathCoverageProblem(CFG pCfg, Class<Object>[] pParameters)
			throws Exception {
		this(pCfg, pParameters, null);
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

	public void evaluate(Solution solution) throws JMException {
		VariableTranslator translator = new VariableTranslator(solution.getDecisionVariables()[0]);

		Object[] arguments = translator.translateArray(this.parameters);

		CBridge bridge = new CBridge();

		EventsHandler handler = new EventsHandler();
		PathDistanceListener listener = new PathDistanceListener(this.cfg,
				this.target);

		if (debug)
			System.out.println(Arrays.toString(arguments));

		bridge.getEvents(handler, arguments);

		Simulator simulator = new Simulator(cfg, handler.getEvents());

		simulator.addListener(listener);

		simulator.simulate();

		solution.setObjective(
				0,
				listener.getPathDistance()
						+ listener.getNormalizedBranchDistance());
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
