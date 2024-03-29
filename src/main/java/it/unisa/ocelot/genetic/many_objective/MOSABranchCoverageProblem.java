package it.unisa.ocelot.genetic.many_objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Range;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.dominators.Dominators;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.edges.EdgeDistanceListener;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.SimulationException;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.util.Utils;
import jmetal.core.Solution;
import jmetal.util.JMException;

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
	
	private Map<LabeledEdge, Set<CFGNode>> dominators;

	private boolean debug;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MOSABranchCoverageProblem(CFG cfg, CType[] parameters, int pArraySize,
			Range<Double>[] ranges, List<LabeledEdge> branches) throws Exception {
		super(parameters, ranges, pArraySize);
		numberOfObjectives_ = branches.size();
		this.cfg = cfg;
		this.targetBranches = new ArrayList<>(branches);
		initTargetsDominators();
	}
	
	private void initTargetsDominators() {
		this.dominators = new HashMap<>();
		Dominators<CFGNode, LabeledEdge> dominators = new Dominators<CFGNode, LabeledEdge>(this.cfg, this.cfg.getStart());
		
		for (LabeledEdge edge : this.targetBranches) {	
			this.dominators.put(edge, dominators.getStrictDominators(this.cfg.getEdgeSource(edge)));
		}
	}

	/**
	 * Evaluate a solution
	 * 
	 * @param solution
	 *            The solution to evaluate
	 */
	@Override
	public double evaluateSolution(Solution solution) throws JMException, SimulationException {
		VariableTranslator translator = new VariableTranslator(solution);
		Object[][][] arguments = translator.translateArray(this.parameters);
		
		if (debug)
			System.out.println(Utils.printParameters(arguments));

		CBridge bridge = getCurrentBridge();
		EventsHandler handler = new EventsHandler();
		try {
			bridge.getEvents(handler, arguments[0][0], arguments[1], arguments[2][0]);
		} catch (RuntimeException e) {
			this.onError(solution, e);
			return -1;
		}

		Simulator simulator = new Simulator(cfg, handler.getEvents());
		EdgeDistanceListener edgeDistanceListener;
//		DominatorListener edgeDistanceListener;
		
		Map<LabeledEdge, EdgeDistanceListener> listeners = new HashMap<>();
//		Map<LabeledEdge, DominatorListener> listeners = new HashMap<>();
		for (LabeledEdge labeledEdge : targetBranches) {
			if (labeledEdge.isCovered())
				continue;
		
			// for each branch I have to add an objective to fitness function
//			edgeDistanceListener = new EdgeDistanceListener(cfg, labeledEdge);
//			edgeDistanceListener = new DominatorListener(cfg, labeledEdge);

			edgeDistanceListener = new EdgeDistanceListener(cfg, labeledEdge, this.dominators.get(labeledEdge));

			listeners.put(labeledEdge, edgeDistanceListener);
			
			simulator.addListener(edgeDistanceListener);
		}
		
		simulator.simulate();
		
		for (LabeledEdge labeledEdge : targetBranches) {
			if (labeledEdge.isCovered())
				continue;
			edgeDistanceListener = listeners.get(labeledEdge);
			double fitness = edgeDistanceListener.getNormalizedBranchDistance() + edgeDistanceListener.getApproachLevel();

			solution.setObjective(labeledEdge.getObjectiveID(), fitness);
		}
		
		//Not important, MOSA uses his own algorithm
		return 0;
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
