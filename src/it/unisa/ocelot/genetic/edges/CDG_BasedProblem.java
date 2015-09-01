package it.unisa.ocelot.genetic.edges;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Range;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CollateralCoverageEvaluator;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.many_objective.DominatorListener;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.CoverageCalculatorListener;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.util.JMException;

public class CDG_BasedProblem extends StandardProblem {

	private CFG controlFlowGraph;
	private List<LabeledEdge> branches;

	private LabeledEdge target;

	private boolean debug;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public CDG_BasedProblem(CFG controlFlowGraph, CType[] parameters, int pArraySize,
			Range<Double>[] ranges, List<LabeledEdge> branches, LabeledEdge target) throws Exception {
		super(parameters, ranges, pArraySize);
		this.controlFlowGraph = controlFlowGraph;
		this.branches = branches;
		this.target = target;
	}

	@Override
	public void evaluate(Solution solution) throws JMException {
		VariableTranslator translator = new VariableTranslator(solution);
		Object[][][] arguments = translator.translateArray(this.parameters);

		if (debug)
			System.out.println(Arrays.toString(arguments));

		CBridge bridge = new CBridge();
		EventsHandler handler = new EventsHandler();
		bridge.getEvents(handler, arguments[0], arguments[1], arguments[2]);

		// listener
		DominatorListener dominatorListener = new DominatorListener(controlFlowGraph, target);
		CoverageCalculatorListener coverageCalculatorListener = new CoverageCalculatorListener(
				controlFlowGraph);

		// simulation
		Simulator simulator = new Simulator(controlFlowGraph, handler.getEvents());
		simulator.addListener(dominatorListener);
		simulator.addListener(coverageCalculatorListener);
		simulator.simulate();

		CollateralCoverageEvaluator collateralCoverageEvaluator = new CollateralCoverageEvaluator(
				controlFlowGraph, target, branches, coverageCalculatorListener.getCoveredBranches());

		solution.setObjective(0, dominatorListener.getFitness());
//		double l_e = collateralCoverageEvaluator.calculateCollateralCoverage();
//		System.out.println(l_e);
//		solution.setFitness(1-l_e);
		solution.setFitness(1 - collateralCoverageEvaluator.calculateCollateralCoverage());
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
