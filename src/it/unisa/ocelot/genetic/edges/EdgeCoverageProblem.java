package it.unisa.ocelot.genetic.edges;

import java.util.List;
import java.util.Set;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.dominators.Dominators;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.cfg.nodes.CFGNodeNavigator;
import it.unisa.ocelot.genetic.SerendipitousProblem;
import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.simulator.*;

import org.apache.commons.lang3.Range;

import jmetal.core.Solution;
import jmetal.util.JMException;

public class EdgeCoverageProblem extends StandardProblem implements SerendipitousProblem<LabeledEdge> {
	private static final long serialVersionUID = 1930014794768729268L;

	private CFG cfg;
	private LabeledEdge target;
	private Set<CFGNode> dominators;
	private Set<LabeledEdge> serendipitousPotentials;
	private Set<LabeledEdge> serendipitousCovered;

	private boolean debug;

	public EdgeCoverageProblem(CFG pCfg, List<Graph> graphList, Range<Double>[] pRanges)
			throws Exception {
		super(graphList, pRanges);
		this.cfg = pCfg;
		problemName_ = "EdgeCoverageProblem";
	}
	
	public EdgeCoverageProblem(CFG pCfg, List<Graph> graphList) throws Exception {
		this(pCfg, graphList, null);
	}

	public CFG getCFG() {
		return cfg;
	}
	
	public void setSerendipitousPotentials(Set<LabeledEdge> serendipitousPotentials) {
		this.serendipitousPotentials = serendipitousPotentials;
		this.serendipitousPotentials.remove(this.target);
	}

	public CFGNodeNavigator navigate() {
		return cfg.getStart().navigate(cfg);
	}

	public void setTarget(LabeledEdge pEdge) {
		this.target = pEdge;
		
		CFGNode parent = this.cfg.getEdgeSource(pEdge);
		
		Dominators<CFGNode, LabeledEdge> dominators = new Dominators<CFGNode, LabeledEdge>(this.cfg, this.cfg.getStart());
		
		this.dominators = dominators.getStrictDominators(parent);
	}

	public double evaluateSolution(Solution solution) throws JMException, SimulationException {
		//Object[][][] arguments = this.getParameters(solution);
		Graph graph = this.getGraphFromSolution(solution);

		//CBridge bridge = getCurrentBridge();
		CBridge cBridge  = getCurrentBridge();

		EventsHandler handler = new EventsHandler();
		EdgeDistanceListener bdalListener = new EdgeDistanceListener(cfg, target, dominators);
		bdalListener.setSerendipitousPotentials(this.serendipitousPotentials);
		
		try {
			//bridge.getEvents(handler, arguments[0][0], arguments[1], arguments[2][0]);
			cBridge.getEvents(handler, graph);
		} catch (RuntimeException e) {
			this.onError(solution, e);
			return -1;
		}

		Simulator simulator = new Simulator(cfg, handler.getEvents());

		simulator.addListener(bdalListener);

		simulator.simulate();
		
		this.serendipitousCovered = bdalListener.getSerendipitousCovered();
		this.serendipitousPotentials.removeAll(this.serendipitousCovered);
		
		double objective = bdalListener.getNormalizedBranchDistance()
				+ bdalListener.getApproachLevel();
		
		solution.setObjective(0, objective);
		
		/*if (debug)
			System.out.println(Utils.printParameters(arguments) + "\nObjective: " + objective);*/
		
		return bdalListener.getBranchDistance();
	}
	
	public Set<LabeledEdge> getSerendipitousCovered() {
		return serendipitousCovered;
	}
}
