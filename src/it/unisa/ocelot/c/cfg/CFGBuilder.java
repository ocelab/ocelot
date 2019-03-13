package it.unisa.ocelot.c.cfg;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unisa.ocelot.c.cfg.dominators.Dominators;
import it.unisa.ocelot.c.cfg.edges.FlowEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.edges.TrueEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.manager.GraphGenerator;
import it.unisa.ocelot.util.Utils;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;

public class CFGBuilder {
	public static CFG build(String pSourceFile, String pFunctionName) 
			throws IOException, CoreException {
		String code = Utils.readFile(pSourceFile);
		CFG graph = new CFG();

		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(
                pSourceFile);
		CFGVisitor cfgBuilder = new CFGVisitor(graph, pFunctionName);
		ConstantsCheckerVisitor constantsChecker = new ConstantsCheckerVisitor(graph, pFunctionName);

		translationUnit.accept(cfgBuilder);
		translationUnit.accept(constantsChecker);

		//Remove code that will never be coveraged
		removeInfeasiblePathFromCFG(graph);

		Graph typeGraph = GraphGenerator.generateGraphFromFunction(graph.getParameterTypes());
		graph.setTypeGraph(typeGraph);

		return graph;
	}

	public static void removeInfeasiblePathFromCFG (CFG graph) {
		Dominators<CFGNode, LabeledEdge> dominator = new Dominators<CFGNode, LabeledEdge>(graph, graph.getStart());

		if (dominator.getIDoms() == null) {
			dominator.computeDominators();
		}

		for (CFGNode node : graph.vertexSet()) {
			boolean isInIdom = false;
			for (Map.Entry<CFGNode, CFGNode> entry: dominator.getIDoms().entrySet()) {
				if (node.getId() == entry.getKey().getId()) {
					isInIdom = true;
					break;
				}
			}

			//Remove all edges of infeasible path
			if (!isInIdom) {
				Set<LabeledEdge> edgeSetClone = new HashSet<>(graph.edgeSet());
				for (LabeledEdge cfgEdge : edgeSetClone) {
					CFGNode source = graph.getEdgeSource(cfgEdge);
					CFGNode target = graph.getEdgeTarget(cfgEdge);

					if (source.getId() == node.getId() || target.getId() == node.getId()) {
						graph.removeEdge(cfgEdge);
					}
				}
			}
		}
	}
}
