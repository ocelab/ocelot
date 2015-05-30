package it.unisa.ocelot.genetic.nodes;

import java.util.Arrays;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;

import org.apache.commons.lang3.Range;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.variable.ArrayReal;
import jmetal.util.JMException;

public class NodeCoverageProblem extends Problem {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;
	private Class<Object>[] parameters;
	private CFGNode target;

	private boolean debug;

	@SuppressWarnings("rawtypes")
	public NodeCoverageProblem(CFG pCfg, Class[] pParameters, Range<Double>[] pRanges)
			throws Exception {

		this.cfg = pCfg;
		
		numberOfVariables_ = pParameters.length;
		numberOfObjectives_ = 1;
		numberOfConstraints_ = 0;
		problemName_ = "NodeCoverageProblem";

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
	
	public NodeCoverageProblem(CFG pCfg,
			Class<Object>[] pParameters)
			throws Exception {
		this(pCfg, pParameters, null);
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
		Double[] variables = ((ArrayReal) solution.getDecisionVariables()[0]).array_;

		Object[] arguments = new Object[variables.length];
		for (int i = 0; i < variables.length; i++) {
			arguments[i] = this.getInstance(variables[i], this.parameters[i]);
		}

		CBridge bridge = new CBridge();

		EventsHandler handler = new EventsHandler();
		NodeDistanceListener bdalListener = new NodeDistanceListener(cfg, target);

		if (debug)
			System.out.println(Arrays.toString(variables));
		
		bridge.getEvents(handler, arguments);

		Simulator simulator = new Simulator(cfg, handler.getEvents());

		simulator.addListener(bdalListener);

		simulator.simulate();

		solution.setObjective(0, bdalListener.getNormalizedBranchDistance()
				+ bdalListener.getApproachLevel());
	}

	private Object getInstance(double pValue, Class<Object> pType) {
		if (pType.equals(Integer.class)) {
			return new Integer((int) pValue);
		} else if (pType.equals(Double.class)) {
			return new Double(pValue);
		}

		return new Double(pValue);
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
