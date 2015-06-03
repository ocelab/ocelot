package it.unisa.ocelot.c.cfg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;

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

	/**
	 * Creates a visitor of the C syntax tree able to generate its Control Flow
	 * Graph.
	 * 
	 * @param pGraph
	 *            Graph which will contain the result
	 */
	public ConstantsCheckerVisitor(CFG pGraph) {
		this.graph = pGraph;
		
		this.numericConstants = new ArrayList<Double>();
		this.stringConstants = new ArrayList<String>();

		this.shouldVisitExpressions = true;
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
