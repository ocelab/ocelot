package it.unisa.ocelot.c.cfg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;

/**
 * This visitor saves all the literals in two arrays: numbers and strings. Besides, it updates the
 * graph with these constants arrays.
 * @author simone
 *
 */
public class ConstantsCheckerVisitor extends ASTVisitor {
	private CFG graph;
	
	private List<Double> numericConstants;
	private List<String> stringConstants;

	private String functionName;

	/**
	 * Creates a visitor of the C syntax tree able to generate its Control Flow
	 * Graph.
	 * 
	 * @param pGraph
	 *            Graph which will contain the result
	 */
	public ConstantsCheckerVisitor(CFG pGraph, String pFunctionName) {
		this.graph = pGraph;
		
		this.functionName = pFunctionName;
		
		this.numericConstants = new ArrayList<Double>();
		this.stringConstants = new ArrayList<String>();

		this.shouldVisitExpressions = true;
		this.shouldVisitDeclarations = true;
	}
	
	public int visit(IASTDeclaration name) {

		if (name instanceof CASTFunctionDefinition) {
			IASTFunctionDefinition function = (CASTFunctionDefinition) name;

			if (function.getDeclarator().getName().getRawSignature().equals(this.functionName))
				return PROCESS_CONTINUE;
		}

		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTExpression expression) {
		if (expression instanceof IASTLiteralExpression) {
			String source = expression.getRawSignature();
			try {
				this.numericConstants.add(Double.parseDouble(source));
				this.graph.setConstantNumbers(this.numericConstants);
			} catch (NumberFormatException e) {
				this.stringConstants.add(source);
				this.graph.setConstantStrings(this.stringConstants);
			}
		}
		
		return super.visit(expression);
	}
}
