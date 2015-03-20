package parser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.jgrapht.DirectedGraph;

import util.LabeledEdge;

public class CDT_CFGVisitor extends ASTVisitor {
	private static final LabeledEdge TRUE_EDGE = new LabeledEdge(true);
	private static final LabeledEdge GOTO_EDGE = new LabeledEdge("goto");
	
	private DirectedGraph<CFGNode, LabeledEdge> graph;
	private Map<String, CFGNode> labels;
	private List<Entry<String, CFGNode>> gotos;
	private List<CFGNode> returns;
	private Stack<SubGraph> ioHandlers;
	
	public CDT_CFGVisitor(DirectedGraph<CFGNode, LabeledEdge> pGraph) {
		this.graph = pGraph;
		this.labels = new HashMap<String, CFGNode>();
		this.gotos = new ArrayList<Entry<String, CFGNode>>();
		this.returns = new ArrayList<CFGNode>();
		this.ioHandlers = new Stack<SubGraph>();
	}
	
	/**
	 * Prepares for the visit
	 */
	public void prepare() {
		
	}
	
	/**
	 * Everything visited, makes final things
	 */
	public void doFinalThings() {
		for (Entry<String, CFGNode> entry : this.gotos) {
			this.setOutput(entry.getValue(), this.labels.get(entry.getKey()), "goto");
		}
		
		for (CFGNode node : this.graph.vertexSet()) {
			Set<LabeledEdge> edges = this.graph.outgoingEdgesOf(node);
			if (edges.contains(TRUE_EDGE)) {
				for (LabeledEdge edge : edges)
					if (edge.equals(GOTO_EDGE))
						edge.setLabel(false);
			}
		}
	}
	
	
	//#############################################
	//# Normal statements					  	  #
	//#############################################
	public void visit(IASTCompoundStatement pStatement) {
		SubGraph result = new SubGraph();
		
		if (pStatement.getStatements().length != 0) {
			pStatement.getStatements()[0].accept(this);
			SubGraph lastNode = this.ioHandlers.pop();
			
			result.addInput(lastNode.getInput());
			result.inheritOthers(lastNode);
			
			for (int i = 1; i < pStatement.getStatements().length; i++) {
				pStatement.getStatements()[i].accept(this);
				SubGraph currentNode = this.ioHandlers.pop();
				
				this.setOutput(lastNode.getOutput(), currentNode.getInput(), "goto");
				
				result.inheritOthers(currentNode);
				
				lastNode = currentNode;
			}
			
			result.inheritOutput(lastNode);
		}
			
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTDeclarationStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode thisNode = new CFGNode(pStatement);
		
		this.graph.addVertex(thisNode);
		
		result.addInput(thisNode);
		result.addOutput(thisNode);
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTExpressionStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode thisNode = new CFGNode(pStatement);
		
		this.graph.addVertex(thisNode);
		
		result.addInput(thisNode);
		result.addOutput(thisNode);
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTNullStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode thisNode = new CFGNode(pStatement);
		
		this.graph.addVertex(thisNode);
		
		result.addInput(thisNode);
		result.addOutput(thisNode);
		
		this.ioHandlers.push(result);
	}
	
	//#############################################
	//# Conditional statements					  #
	//#############################################
	public void visit(IASTIfStatement pStatement) {
		SubGraph result = new SubGraph();
		SubGraph thenSubGraph;
		SubGraph elseSubGraph;
		
		boolean elsePresent = pStatement.getElseClause() != null;
		
		CFGNode condition = new CFGNode(pStatement.getConditionExpression());
		
		this.graph.addVertex(condition);
		
		pStatement.getThenClause().accept(this);
		thenSubGraph = this.ioHandlers.pop();
		
		result.inheritOutput(thenSubGraph);
		result.inheritOthers(thenSubGraph);
		
		this.setOutput(condition, thenSubGraph.getInput(), true);
		if (elsePresent) {
			pStatement.getElseClause().accept(this);
			elseSubGraph = this.ioHandlers.pop();
			
			this.setOutput(condition, elseSubGraph.getInput(), false);
			
			result.inheritOutput(elseSubGraph);
			result.inheritOthers(elseSubGraph);
		} else {
			result.addOutput(condition);
		}
		
		result.addInput(condition);

		this.ioHandlers.push(result);
	}
	
	public void visit(IASTSwitchStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode expression = new CFGNode(pStatement.getControllerExpression());
		this.graph.addVertex(expression);
		
		pStatement.getBody().accept(this);
		SubGraph statementSubGraph = this.ioHandlers.pop();
		
		for (CFGNode caseNode : statementSubGraph.getCases()) {
			String label;
			if (caseNode.getLeadingNode() instanceof IASTCaseStatement){
				IASTCaseStatement caseStatement = (IASTCaseStatement)caseNode.getLeadingNode();
				label = caseStatement.getExpression().getRawSignature();
			} else
				label = "default";
			
			this.setOutput(expression, caseNode, label);
		}
		
		result.addInput(expression);
		result.addOutput(statementSubGraph.getOutput());
		result.addOutput(statementSubGraph.getBreaks());
		result.inheritContinue(statementSubGraph);
		
		this.ioHandlers.push(result);
	}
	
	//#############################################
	//# Loop statements							  #
	//#############################################
	public void visit(IASTWhileStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode condition = new CFGNode(pStatement.getCondition());
		this.graph.addVertex(condition);
		
		pStatement.getBody().accept(this);
		SubGraph bodySubGraph = this.ioHandlers.pop();
		
		this.setOutput(condition, bodySubGraph.getInput(), true);
		this.setOutput(bodySubGraph.getOutput(), condition, "goto");
		this.setOutput(bodySubGraph.getContinues(), condition, "goto");
		
		result.addInput(condition);
		result.addOutput(condition);
		result.addOutput(bodySubGraph.getBreaks());
		result.inheritCase(bodySubGraph);
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTDoStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode condition = new CFGNode(pStatement.getCondition());
		this.graph.addVertex(condition);
		
		pStatement.getBody().accept(this);
		SubGraph bodySubGraph = this.ioHandlers.pop();
		
		this.setOutput(condition, bodySubGraph.getInput(), true);
		this.setOutput(bodySubGraph.getOutput(), condition, "goto");
		this.setOutput(bodySubGraph.getContinues(), condition, "goto");
		
		result.inheritInput(bodySubGraph);
		result.addOutput(condition);
		result.addOutput(bodySubGraph.getBreaks());
		result.inheritCase(bodySubGraph);
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTForStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode condition = new CFGNode(pStatement.getConditionExpression());
		CFGNode incr = new CFGNode(pStatement.getIterationExpression());
		this.graph.addVertex(condition);
		this.graph.addVertex(incr);
		
		pStatement.getInitializerStatement().accept(this);
		SubGraph initSubGraph = this.ioHandlers.pop();
		pStatement.getBody().accept(this);
		SubGraph bodySubGraph = this.ioHandlers.pop();
		
		this.setOutput(initSubGraph.getOutput(), condition, "goto");
		this.setOutput(condition, bodySubGraph.getInput(), true);
		this.setOutput(bodySubGraph.getOutput(), incr, "goto");
		this.setOutput(incr, condition, "goto");
		this.setOutput(bodySubGraph.getContinues(), incr, "goto");
		
		result.inheritInput(initSubGraph);
		result.inheritCase(bodySubGraph);
		result.addOutput(condition);
		result.addOutput(bodySubGraph.getBreaks());
		
		this.ioHandlers.push(result);
	}
	
	//#############################################
	//# Jump statements							  #
	//#############################################
	public void visit(IASTGotoStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode node = new CFGNode(pStatement);
		this.graph.addVertex(node);
		String label = pStatement.getName().getRawSignature();
		
		this.gotos.add(new AbstractMap.SimpleEntry<String, CFGNode>(label, node));
		
		result.addInput(node);
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTBreakStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode node = new CFGNode(pStatement);
		this.graph.addVertex(node);
		
		result.addInput(node);
		result.addBreak(node);
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTContinueStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode node = new CFGNode(pStatement);
		this.graph.addVertex(node);
		
		result.addInput(node);
		result.addContinue(node);
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTReturnStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode node = new CFGNode(pStatement);
		this.graph.addVertex(node);
		
		result.addInput(node);
		
		this.returns.add(node);
		
		this.ioHandlers.push(result);
	}
	
	//#############################################
	//# Label statements						  #
	//#############################################
	public void visit(IASTLabelStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode node = new CFGNode(pStatement);
		graph.addVertex(node);
		this.labels.put(pStatement.getName().getRawSignature(), node);
		
		result.addInput(node);
		result.addOutput(node);
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTCaseStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode node = new CFGNode(pStatement);
		graph.addVertex(node);
		
		result.addInput(node);
		result.addOutput(node);
		result.addCase(node);
		
		this.ioHandlers.push(result);
	}
	
	public void visit(IASTDefaultStatement pStatement) {
		SubGraph result = new SubGraph();
		
		CFGNode node = new CFGNode(pStatement);
		graph.addVertex(node);
		
		result.addInput(node);
		result.addOutput(node);
		result.addCase(node);
		
		this.ioHandlers.push(result);
	}
	
	
	/**
	 * Dispatcher method that sends the call to each statement type method
	 */
	@Override
	public int visit(IASTStatement statement) {
		if (statement instanceof IASTCompoundStatement)
			this.visit((IASTCompoundStatement)statement);
		else if (statement instanceof IASTDeclarationStatement)
			this.visit((IASTDeclarationStatement)statement);
		else if (statement instanceof IASTExpressionStatement)
			this.visit((IASTExpressionStatement)statement);
		else if (statement instanceof IASTNullStatement)
			this.visit((IASTNullStatement)statement);
		else if (statement instanceof IASTIfStatement)
			this.visit((IASTIfStatement)statement);
		else if (statement instanceof IASTSwitchStatement)
			this.visit((IASTSwitchStatement)statement);
		else if (statement instanceof IASTWhileStatement)
			this.visit((IASTWhileStatement)statement);
		else if (statement instanceof IASTDoStatement)
			this.visit((IASTDoStatement)statement);
		else if (statement instanceof IASTForStatement)
			this.visit((IASTForStatement)statement);
		else if (statement instanceof IASTGotoStatement)
			this.visit((IASTGotoStatement)statement);
		else if (statement instanceof IASTBreakStatement)
			this.visit((IASTBreakStatement)statement);
		else if (statement instanceof IASTContinueStatement)
			this.visit((IASTContinueStatement)statement);
		else if (statement instanceof IASTReturnStatement)
			this.visit((IASTReturnStatement)statement);
		else if (statement instanceof IASTLabelStatement)
			this.visit((IASTLabelStatement)statement);
		else if (statement instanceof IASTCaseStatement)
			this.visit((IASTCaseStatement)statement);
		else if (statement instanceof IASTDefaultStatement)
			this.visit((IASTDefaultStatement)statement);
		else
			System.err.println("Hey, this is a BUG! " + statement.getClass().toString());
		
		return PROCESS_SKIP;
	}
	
	private boolean isSimpleStatement(IASTStatement pStatement) {
		return  (pStatement instanceof IASTExpressionStatement) || 
				(pStatement instanceof IASTNullStatement)		||
				(pStatement instanceof IASTDeclarationStatement);
	}
	
	
	/**
	 * Links the given output to the given input in the CFG
	 * @param pOut From which nodes start the edges
	 * @param pIn In which nodes arrive the edges
	 */
	private void setOutput(List<CFGNode> pOut, List<CFGNode> pIn, Object pLabel) {
		for (CFGNode out : pOut) 
			for (CFGNode in : pIn) {
				LabeledEdge edge = this.graph.addEdge(out, in);
				edge.setLabel(pLabel);
			}
	}
	
	/**
	 * Links the given output to the given input in the CFG
	 * @param pOut From which node start the edges
	 * @param pIn In which nodes arrive the edges
	 */
	private void setOutput(CFGNode pOut, List<CFGNode> pIn, Object pLabel) {
		for (CFGNode in: pIn) {
			LabeledEdge edge = this.graph.addEdge(pOut, in);
			edge.setLabel(pLabel);
		}
	}
	
	/**
	 * Links the given output to the given input in the CFG
	 * @param pOut From which nodes start the edges
	 * @param pIn In which node arrive the edges
	 */
	private void setOutput(List<CFGNode> pOut, CFGNode pIn, Object pLabel) {
		for (CFGNode out: pOut) {
			LabeledEdge edge = this.graph.addEdge(out, pIn);
			edge.setLabel(pLabel);
		}
	}
	
	/**
	 * Links the given output to the given input in the CFG
	 * @param pOut From which node start the edges
	 * @param pIn In which node arrive the edges
	 */
	private void setOutput(CFGNode pOut, CFGNode pIn, Object pLabel) {
			LabeledEdge edge = this.graph.addEdge(pOut, pIn);
			edge.setLabel(pLabel);
	}
}
