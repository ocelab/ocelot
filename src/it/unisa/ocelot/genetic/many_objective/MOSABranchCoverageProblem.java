package it.unisa.ocelot.genetic.many_objective;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.nodes.NodeDistanceListener;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.util.JMException;

import org.apache.commons.lang3.Range;

/**
 * Class representing a many-objective optimization branch coverage problem
 * 
 * @author giograno
 *
 */
public class MOSABranchCoverageProblem extends StandardProblem {

	private CFG cfg;
	// we consider all edges excepts Flow
	private final List<LabeledEdge> targetBranches;

	private boolean debug;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	public MOSABranchCoverageProblem(CFG cfg, CType[] parameters, int pArraySize,
			Range<Double>[] ranges, List<LabeledEdge> branches) throws Exception {
		super(parameters, ranges, pArraySize);
		this.cfg = cfg;
		this.targetBranches = new ArrayList<>(branches);
	}

	/**
	 * Evaluate a solution
	 * 
	 * @param solution
	 *            The solution to evaluate
	 */
	@Override
	public void evaluate(Solution solution) throws JMException {
		VariableTranslator translator = new VariableTranslator(solution);
		Object[][][] arguments = translator.translateArray(this.parameters);
		
		CBridge.initialize(arguments);
		if (debug)
			System.out.println(Arrays.toString(arguments));

		CBridge bridge = new CBridge();
		EventsHandler handler = new EventsHandler();
		bridge.getEvents(handler, arguments[0][0], arguments[1], arguments[2][0]);

		Simulator simulator = new Simulator(cfg, handler.getEvents());
//		EdgeDistanceListener edgeDistanceListener;
		DominatorListener edgeDistanceListener;
		
		for (LabeledEdge labeledEdge : targetBranches) {
			if (labeledEdge.isCovered())
				continue;
		
			// for each branch I have to add an objective to fitness function
//			edgeDistanceListener = new EdgeDistanceListener(cfg, labeledEdge);
			edgeDistanceListener = new DominatorListener(cfg, labeledEdge);


			simulator.addListener(edgeDistanceListener);
			simulator.simulate();
			
			double fitness = edgeDistanceListener.getNormalizedBranchDistance() + edgeDistanceListener.getApproachLevel();

			solution.setObjective(labeledEdge.getObjectiveID(), fitness);
		}
	}

	/**
	 * Returns the set of branches representing the target of our coverage
	 * problem
	 * 
	 * @return a List of Labeled Edges
	 */
	public List<LabeledEdge> getTargetBranches() {
		return this.targetBranches;
	}

	/**
	 * Returns the control flow graph of the function under test
	 * 
	 * @return a CFG
	 */
	public CFG getControlFlowGraph() {
		return this.cfg;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
