package it.unisa.ocelot.c.cfg;

import java.io.IOException;

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

		Graph typeGraph = GraphGenerator.generateGraphFromFunction(graph.getParameterTypes());
		graph.setTypeGraph(typeGraph);

		return graph;
	}
}
