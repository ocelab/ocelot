package it.unisa.ocelot.c.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.CycleDetector;

/**
 * Calculate all linearly independent McCabe paths for a given control flow
 * graph
 * 
 * @author giograno
 *
 */
public class McCabeCalculator {

	private CFG cfg;
	private CFGNode startNode;
	private CycleDetector<CFGNode, LabeledEdge> cycleGraph;
	// paths node by node
	private ArrayList<ArrayList<CFGNode>> mcCabePaths;
	// paths edge by edge
	private ArrayList<ArrayList<LabeledEdge>> mcCabeEdgePaths;

	/**
	 * Constructor of McCabeCalculator class
	 * 
	 * @param cfg
	 *            control flow graph to analyze
	 */
	public McCabeCalculator(CFG cfg) {
		this.cfg = cfg;
		this.cycleGraph = new CycleDetector<>(cfg);
		this.startNode = cfg.getStart();
		this.mcCabePaths = new ArrayList<ArrayList<CFGNode>>();
	}

	/**
	 * Performs calculation of McCabe linearly independent paths
	 */
	public void calculateMcCabePaths() {
		this.FindBasis(this.startNode, new ArrayList<CFGNode>());
	}

	/**
	 * Traverse recursively the control flow graph calculating all basis path
	 * 
	 * @param node
	 *            starting node
	 * @param path
	 *            empty path
	 */
	private void FindBasis(CFGNode node, ArrayList<CFGNode> path) {
		// add current node to McCabe path
		path.add(node);

		CFGNode defaultDestination = this.getDefaultDestination(node);
		Set<LabeledEdge> currentOutgoingEdges = cfg.outgoingEdgesOf(node);
		HashSet<LabeledEdge> outgoingEndges = new HashSet<>(
				currentOutgoingEdges);

		if (currentOutgoingEdges.isEmpty()) {
			// return path as a solution
			mcCabePaths.add(path);
		} else if (!node.isVisited()) {
			node.setVisited();

			this.FindBasis(defaultDestination, new ArrayList<>(path));

			// removing edge from edges to explore
			outgoingEndges.remove(cfg.getEdge(node, defaultDestination));

			for (LabeledEdge labeledEdge : outgoingEndges)
				this.FindBasis(cfg.getEdgeTarget(labeledEdge), new ArrayList<>(
						path));

		} else
			this.FindBasis(defaultDestination, new ArrayList<>(path));

	}

	/**
	 * It calculates the vertex targeting by the default edge. Default edge is
	 * any edge that is not a back edge or which later causes a node to have two
	 * incoming edges˙
	 * 
	 * @param node
	 *            node to compute
	 * @return the default edge for given node
	 */
	private CFGNode getDefaultDestination(CFGNode node) {
		Set<LabeledEdge> currentOutgoingEdges = cfg.outgoingEdgesOf(node);
		Set<CFGNode> vertexInCycle = cycleGraph
				.findCyclesContainingVertex(node);
		Set<CFGNode> neighborVertex = new HashSet<>();

		for (LabeledEdge labeledEdge : currentOutgoingEdges) {
			CFGNode targetEdge = cfg.getEdgeTarget(labeledEdge);
			neighborVertex.add(targetEdge);
		}

		for (CFGNode cfgNode : neighborVertex) {
			if (!vertexInCycle.contains(cfgNode))
				return cfgNode;
		}

		ArrayList<CFGNode> neighborList = new ArrayList<CFGNode>(neighborVertex);
		if (neighborList.isEmpty())
			return null;
		return new ArrayList<CFGNode>(neighborVertex).get(0);

	}

	/**
	 * Returns all McCabe linearly independent paths, when a single path is
	 * described by its nodes
	 * 
	 * @return a list of McCabe paths
	 */
	public ArrayList<ArrayList<CFGNode>> getMcCabeNodePaths() {
		return this.mcCabePaths;
	}

	/**
	 * Returns all McCabe linearly independent paths, when a single path is
	 * described by its edges
	 * 
	 * @return a list of McCabe paths
	 */
	public ArrayList<ArrayList<LabeledEdge>> getMcCabeEdgePaths() {
		if (mcCabeEdgePaths != null)
			return this.mcCabeEdgePaths;

		mcCabeEdgePaths = new ArrayList<ArrayList<LabeledEdge>>();
		for (ArrayList<CFGNode> nodePath : mcCabePaths) {
			ArrayList<LabeledEdge> currentEdgePath = new ArrayList<>();
			for (int i = 0; i < nodePath.size() - 1; i++) {
				CFGNode startNode = nodePath.get(i);
				CFGNode endNode = nodePath.get(i + 1);
				currentEdgePath.add(cfg.getEdge(startNode, endNode));
			}
			mcCabeEdgePaths.add(currentEdgePath);
		}
		return this.mcCabeEdgePaths;
	}

}
