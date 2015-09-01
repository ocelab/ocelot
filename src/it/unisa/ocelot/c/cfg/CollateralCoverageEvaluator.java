package it.unisa.ocelot.c.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public class CollateralCoverageEvaluator {

	private CFG controlFlowGraph;
	private LabeledEdge target;

	private List<LabeledEdge> alreadyCoveredBranches; // M
	private List<LabeledEdge> coveredBranchByCurrentSolution; // P(x)

	private int potentiallyCoverableEdges; // L_e(n)

	private static Dominators<CFGNode, LabeledEdge> dominator;

	// control dependence graph
	private static SimpleDirectedGraph<CFGNode, DefaultEdge> controlDependenceGraph;

	public CollateralCoverageEvaluator(CFG controlFlowGraph, LabeledEdge target,
			List<LabeledEdge> branches, List<LabeledEdge> coveredBranchByCurrentSolution) {

		this.controlFlowGraph = controlFlowGraph;
		this.target = target;
		this.coveredBranchByCurrentSolution = coveredBranchByCurrentSolution;

		this.alreadyCoveredBranches = new ArrayList<>();
		for (LabeledEdge edge : branches)
			if (edge.isCovered())
				this.alreadyCoveredBranches.add(edge);

		if (dominator == null)
			CollateralCoverageEvaluator.dominator = new Dominators<CFGNode, LabeledEdge>(
					controlFlowGraph, controlFlowGraph.getStart());

		if (controlDependenceGraph == null)
			controlDependenceGraph = CollateralCoverageEvaluator.dominator.getDominatorTree();

		this.potentiallyCoverableEdges = calculatePotentiallyCoverableEdges(this.target, 0);
	}

	public double calculateCollateralCoverage() {
		if (Math.abs(coveredBranchByCurrentSolution.size() - alreadyCoveredBranches.size()) > potentiallyCoverableEdges)
			return 1.0;
		else {
			if (potentiallyCoverableEdges == 0) {
				return 0;
			}
			return (Math.abs(coveredBranchByCurrentSolution.size() - alreadyCoveredBranches.size()) / potentiallyCoverableEdges);
		}
	}

	private int calculatePotentiallyCoverableEdges(LabeledEdge edge, int potentiallyCoverableEdges) {
		CFGNode childNode = CollateralCoverageEvaluator.controlDependenceGraph.getEdgeTarget(edge);

		if (CollateralCoverageEvaluator.controlDependenceGraph.outDegreeOf(childNode) <= 1)
			return 0; // not a branching node

		Set<DefaultEdge> outgoingFromCDG = CollateralCoverageEvaluator.controlDependenceGraph
				.outgoingEdgesOf(childNode);
		Set<LabeledEdge> outgoingFromCFG = this.controlFlowGraph.outgoingEdgesOf(childNode);

		Set<LabeledEdge> commonOutgoings = new HashSet<>();
		for (LabeledEdge cfgEdge : outgoingFromCFG) {
			CFGNode cfgSouce = this.controlFlowGraph.getEdgeSource(cfgEdge);
			CFGNode cfgTarget = this.controlFlowGraph.getEdgeTarget(cfgEdge);
			for (DefaultEdge cdgEdge : outgoingFromCDG) {
				CFGNode cdgSource = CollateralCoverageEvaluator.controlDependenceGraph
						.getEdgeSource(cdgEdge);
				CFGNode cdgTarget = CollateralCoverageEvaluator.controlDependenceGraph
						.getEdgeTarget(cdgEdge);
				if (cfgSouce == cdgSource && cfgTarget == cdgTarget)
					commonOutgoings.add(cfgEdge);
			}
		}

		int max = Integer.MIN_VALUE;
		for (LabeledEdge outEdge : commonOutgoings) {
			int currentCollateral = calculatePotentiallyCoverableEdges(outEdge, potentiallyCoverableEdges);
			if (currentCollateral > max)
				max = currentCollateral;
		}
		
		potentiallyCoverableEdges += max;

		if (edge.isCovered()) {
			return potentiallyCoverableEdges;
		} else {
			return (1 + potentiallyCoverableEdges);
		}
	}

	public int getPotentiallyCoverableEdges() {
		return this.potentiallyCoverableEdges;
	}
}
