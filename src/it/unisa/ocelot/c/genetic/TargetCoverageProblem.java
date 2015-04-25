package it.unisa.ocelot.c.genetic;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.BDALListener;

import org.apache.commons.lang3.Range;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.variable.ArrayReal;
import jmetal.util.JMException;

public class TargetCoverageProblem extends Problem {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;
	private Class<Object>[] parameters;

	public TargetCoverageProblem(CFG pCfg, Class<Object>[] pParameters, Range<Double>[] pRanges)
			throws Exception {

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
			if (pRanges != null && pRanges[i] != null) {
				lowerLimit_[i] = pRanges[i].getMinimum();
				upperLimit_[i] = pRanges[i].getMaximum();
			} else {
				lowerLimit_[i] = Double.MIN_VALUE;
				upperLimit_[i] = Double.MAX_VALUE;
			}
		}

		this.parameters = pParameters;
	}
	
	public TargetCoverageProblem(CFG pCfg,
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
		this.cfg.setTarget(pNode);
	}

	public void evaluate(Solution solution) throws JMException {
		Double[] variables = ((ArrayReal) solution.getDecisionVariables()[0]).array_;

		Object[] arguments = new Object[variables.length];
		for (int i = 0; i < variables.length; i++) {
			arguments[i] = this.getInstance(variables[i], this.parameters[i]);
		}

		CBridge bridge = new CBridge();

		EventsHandler handler = new EventsHandler();
		BDALListener bdalListener = new BDALListener(cfg);

		bridge.getEvents(handler, arguments);

		Simulator simulator = new Simulator(cfg, handler.getEvents());

		// System.out.println("Simulating with " + StringUtils.join(arguments,
		// " "));

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
}
