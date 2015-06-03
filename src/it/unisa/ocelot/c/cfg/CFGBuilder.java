package it.unisa.ocelot.c.cfg;

import java.io.IOException;

import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.util.Utils;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;

public class CFGBuilder {
	public static CFG build(String pSourceFile, String pFunctionName) 
			throws IOException, CoreException {
		String code = Utils.readFile(pSourceFile);
		CFG graph = new CFG();

		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(
				code.toCharArray(), pSourceFile);
		CFGVisitor cfgBuilder = new CFGVisitor(graph, pFunctionName);
		ConstantsCheckerVisitor constantsChecker = new ConstantsCheckerVisitor(graph); 

		translationUnit.accept(cfgBuilder);
		translationUnit.accept(constantsChecker);

		return graph;
	}
}
