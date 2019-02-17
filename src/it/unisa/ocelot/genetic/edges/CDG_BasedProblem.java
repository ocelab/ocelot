package it.unisa.ocelot.genetic.edges;

import java.util.List;
import java.util.Set;

import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.simulator.*;
import org.apache.commons.lang3.Range;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CollateralCoverageEvaluator;
import it.unisa.ocelot.c.cfg.dominators.Dominators;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.simulator.listeners.CoverageCalculatorListener;
import jmetal.core.Solution;
import jmetal.util.JMException;

public class CDG_BasedProblem extends StandardProblem {

	private CFG controlFlowGraph;
	private List<LabeledEdge> branches;

	private LabeledEdge target;
	
	private Set<CFGNode> dominators;

	private boolean debug;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CDG_BasedProblem(CFG controlFlowGraph, List<Graph> graphList, Range<Double>[] ranges,
							List<LabeledEdge> branches, LabeledEdge target) throws Exception {
		super(graphList, ranges);
		this.controlFlowGraph = controlFlowGraph;
		this.branches = branches;
		this.target = target;
		
		CFGNode parent = this.controlFlowGraph.getEdgeSource(this.target);
		
		Dominators<CFGNode, LabeledEdge> dominators = new Dominators<CFGNode, LabeledEdge>(this.controlFlowGraph, this.controlFlowGraph.getStart());
		
		this.dominators = dominators.getStrictDominators(parent);
	}

	@Override
	public double evaluateSolution(Solution solution) throws JMException, SimulationException {
		VariableTranslator translator = new VariableTranslator(solution);
		//Object[][][] arguments = translator.translateArray(this.parameters);
		Graph graph = translator.getGraphFromSolution(this.graphList);

		/*if (debug)
			System.out.println(Utils.printParameters(arguments));*/

		CBridge cBridge = getCurrentBridge();
		EventsHandler handler = new EventsHandler();
		cBridge.getEvents(handler, graph);

		// listener
		EdgeDistanceListener dominatorListener = new EdgeDistanceListener(this.controlFlowGraph, target, this.dominators);
		//DominatorListener dominatorListener = new DominatorListener(controlFlowGraph, target);
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
		
		return dominatorListener.getBranchDistance();
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
