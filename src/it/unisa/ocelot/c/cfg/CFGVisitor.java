package it.unisa.ocelot.c.cfg;

import java.util.*;
import java.util.Map.Entry;

import it.unisa.ocelot.c.instrumentor.StructNode;
import it.unisa.ocelot.c.instrumentor.VarStructTree;
import it.unisa.ocelot.c.types.*;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.manager.GraphGenerator;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.c.*;

import it.unisa.ocelot.c.cfg.edges.CaseEdge;
import it.unisa.ocelot.c.cfg.edges.FalseEdge;
import it.unisa.ocelot.c.cfg.edges.FlowEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.edges.TrueEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.instrumentor.ExternalReferencesVisitor;
import it.unisa.ocelot.c.instrumentor.MacroDefinerVisitor;

public class CFGVisitor extends ASTVisitor {
	private CFG graph;
	private Map<String, CFGNode> labels;
	private List<Entry<String, CFGNode>> gotos;
	private List<CFGNode> returns;
	private Stack<SubGraph> ioHandlers;
	private String functionName;

	/**
	 * Creates a visitor of the C syntax tree able to generate its Control Flow
	 * Graph.
	 * 
	 * @param pGraph
	 *            Graph which will contain the result
	 */
	public CFGVisitor(CFG pGraph, String pFunctionName) {
		this.graph = pGraph;
		this.functionName = pFunctionName;

		this.labels = new HashMap<String, CFGNode>();
		this.gotos = new ArrayList<Entry<String, CFGNode>>();
		this.returns = new ArrayList<CFGNode>();
		this.ioHandlers = new Stack<SubGraph>();
		
		this.shouldVisitDeclarations = true;
		this.shouldVisitStatements = true;
		this.shouldVisitTranslationUnit = true;
	}
	
	@Override
	public int visit(IASTTranslationUnit tu) {
		ExternalReferencesVisitor referencesVisitor = new ExternalReferencesVisitor(this.functionName);
		
		tu.accept(referencesVisitor);
		
		MacroDefinerVisitor typesDefiner = new MacroDefinerVisitor(this.functionName, referencesVisitor.getExternalReferences());
		tu.accept(typesDefiner);

		CType[] parameterTypes = typesDefiner.getFunctionParametersFromMacroDefinerVisitor
				(typesDefiner.getFunctionParametersMap(), null, null);

		this.graph.setParameterTypes(parameterTypes);

		return super.visit(tu);
	}


	private IType getType(IType type) {
		while (type instanceof CTypedef || type instanceof CQualifierType) {
			if (type instanceof CTypedef) {
				CTypedef tdef = (CTypedef)type;

				type = tdef.getType();
			} else if (type instanceof CQualifierType) {
				CQualifierType qual = (CQualifierType)type;

				type = qual.getType();
			}
		}

		return type;
	}

	protected void onExit() {
		for (Entry<String, CFGNode> entry : this.gotos) {
			this.setOutput(entry.getValue(), this.labels.get(entry.getKey()),
					FlowEdge.class);
		}

		this.setOutput(this.returns, this.graph.getEnd(), FlowEdge.class);

		List<CFGNode> toRemove = new ArrayList<CFGNode>();
		for (CFGNode node : this.graph.vertexSet()) {
			Set<LabeledEdge> edges = this.graph.outgoingEdgesOf(node);
			boolean hasTrueEdge = false;
			for (LabeledEdge edge : edges) {
				if (edge instanceof TrueEdge)
					hasTrueEdge = true;
			}

			if (hasTrueEdge) {
				for (LabeledEdge edge : edges) {
					if (edge instanceof FlowEdge) {
						CFGNode source = this.graph.getEdgeSource(edge);
						CFGNode dest = this.graph.getEdgeTarget(edge);
						this.graph.removeEdge(edge);
						this.setOutput(source, dest, FalseEdge.class);
					}
				}
			}

			if (node.isContinue() || node.isBreak() || node.isGoto()) {
				Set<LabeledEdge> incomingEdges = this.graph
						.incomingEdgesOf(node);
				Set<LabeledEdge> outgoingEdges = this.graph
						.outgoingEdgesOf(node);

				for (LabeledEdge incoming : incomingEdges)
					for (LabeledEdge outgoing : outgoingEdges) {
						CFGNode source = this.graph.getEdgeSource(incoming);
						CFGNode dest = this.graph.getEdgeTarget(outgoing);
						this.setOutput(source, dest,
								(LabeledEdge) incoming.clone());
						toRemove.add(node);
					}
			}
		}

		for (CFGNode dead : toRemove)
			this.graph.removeVertex(dead);
	}

	@Override
	public int visit(IASTDeclaration name) {

		if (name instanceof CASTFunctionDefinition) {
			IASTFunctionDefinition function = (CASTFunctionDefinition) name;

			if (!function.getDeclarator().getName().getRawSignature()
					.equals(this.functionName))
				return PROCESS_SKIP;

			CFGNode.reset();
			CFGNode startingNode = new CFGNode("Start");
			this.graph.addVertex(startingNode);
			this.graph.setStart(startingNode);

			function.getBody().accept(this);
			SubGraph body = this.ioHandlers.pop();

			CFGNode endingNode = new CFGNode("End");
			this.graph.addVertex(endingNode);
			this.graph.setEnd(endingNode);

			this.setOutput(startingNode, body.getInput(), FlowEdge.class);
			this.setOutput(body.getOutput(), endingNode, FlowEdge.class);
			this.setOutput(body.getBreaks(), endingNode, FlowEdge.class);

			this.onExit();
		}

		return PROCESS_SKIP;
	}

	// #############################################
	// # Normal statements #
	// #############################################
	/**
	 * Visits a compound statement, so a block of instructions. Creates an edge
	 * from each sub-graph generated by the visit of each statement to the next
	 * one.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTCompoundStatement pStatement) {
		SubGraph result = new SubGraph();
		boolean keepItSimple = false;

		if (pStatement.getStatements().length != 0) {
			pStatement.getStatements()[0].accept(this);
			SubGraph lastSubGraph = this.ioHandlers.pop();

			result.inheritInput(lastSubGraph);
			result.inheritOthers(lastSubGraph);

			if (this.isSimpleStatement(pStatement.getStatements()[0])
					|| this.isStartingSimpleStatement(pStatement
							.getStatements()[0]))
				keepItSimple = true;

			for (int i = 1; i < pStatement.getStatements().length; i++) {
				IASTStatement currentStatement = pStatement.getStatements()[i];
				currentStatement.accept(this);
				SubGraph currentSubGraph = this.ioHandlers.pop();

				if (this.isSimpleStatement(currentStatement)
						|| this.isEndingSimpleStatement(currentStatement)) {
					if (keepItSimple) {
						CFGNode lastVertex = lastSubGraph.getInput().get(0);
						CFGNode newVertex = currentSubGraph.getInput().get(0);

						lastVertex.join(newVertex);
						lastSubGraph.inheritBreak(currentSubGraph);
						lastSubGraph.inheritContinue(currentSubGraph);
						if (currentSubGraph.getOutput().size() == 0)
							lastSubGraph.resetOutput();
						currentSubGraph = lastSubGraph;

						currentSubGraph.joinVertices(newVertex, lastVertex);
						this.joinVertices(newVertex, lastVertex);
					} else {
						keepItSimple = true;
						this.setOutput(lastSubGraph.getOutput(),
								currentSubGraph.getInput(), FlowEdge.class);
					}

					if (this.isEndingSimpleStatement(currentStatement))
						keepItSimple = false;
				} else {
					keepItSimple = false;
					if (currentSubGraph.getInput().get(0).isCase()) {
						this.setOutput(lastSubGraph.getOutput(),
								currentSubGraph.getInput(), FlowEdge.class);
						//Here there was a bug once. No more. Maybe...
					} else
						this.setOutput(lastSubGraph.getOutput(),
								currentSubGraph.getInput(), FlowEdge.class);
				}

				if (this.isStartingSimpleStatement(currentStatement))
					keepItSimple = true;

				result.inheritOthers(currentSubGraph);

				lastSubGraph = currentSubGraph;
			}

			result.inheritOutput(lastSubGraph);
		} else {
			CFGNode voidBlock = new CFGNode(pStatement);
			this.graph.addVertex(voidBlock);
			result.addInput(voidBlock);
			result.addOutput(voidBlock);
		}

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a declaration. Creates a vertex in the graph.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTDeclarationStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode thisNode = new CFGNode(pStatement);

		this.graph.addVertex(thisNode);

		result.addInput(thisNode);
		result.addOutput(thisNode);

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a single statement. Creates a vertex in the graph.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTExpressionStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode thisNode = new CFGNode(pStatement);

		this.graph.addVertex(thisNode);

		result.addInput(thisNode);
		result.addOutput(thisNode);

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a null statement (";"). Creates a vertex in the graph.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTNullStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode thisNode = new CFGNode(pStatement);

		this.graph.addVertex(thisNode);

		result.addInput(thisNode);
		result.addOutput(thisNode);

		this.ioHandlers.push(result);
	}

	// #############################################
	// # Conditional statements #
	// #############################################
	/**
	 * Visits an "if" statement. Visits both then and else clauses, linking the
	 * vertexes in the graph with true and false edges.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
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

		this.setOutput(condition, thenSubGraph.getInput(), TrueEdge.class);
		if (elsePresent) {
			pStatement.getElseClause().accept(this);
			elseSubGraph = this.ioHandlers.pop();

			this.setOutput(condition, elseSubGraph.getInput(), FalseEdge.class);

			result.inheritOutput(elseSubGraph);
			result.inheritOthers(elseSubGraph);
		} else {
			result.addOutput(condition);
		}

		result.addInput(condition);

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a switch statement. Links the expression to each "case"
	 * instruction in the statement. Each edge has the expression of the "case"
	 * statement as a label. Links "break"s to the next statement.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTSwitchStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode expression = new CFGNode(pStatement.getControllerExpression());
		expression.setSwitch(true);
		this.graph.addVertex(expression);

		pStatement.getBody().accept(this);
		SubGraph statementSubGraph = this.ioHandlers.pop();

		boolean defaultTaken = false;
		for (CFGNode caseNode : statementSubGraph.getCases()) {
			String label;
			if (caseNode.getLeadingNode() instanceof IASTCaseStatement) {
				IASTCaseStatement caseStatement = (IASTCaseStatement) caseNode
						.getLeadingNode();
				label = caseStatement.getExpression().getRawSignature();
			} else {
				label = "default";
				defaultTaken = true;
			}

			this.setOutput(expression, caseNode, new CaseEdge(label));
		}
		
		//If there is no default in switch...
		if (!defaultTaken) {
			CFGNode voidDefaultNode = new CFGNode("Noop");
			this.graph.addVertex(voidDefaultNode);
			
			this.setOutput(expression, voidDefaultNode, new CaseEdge("default"));
			
			result.addOutput(voidDefaultNode);
		}

		result.addInput(expression);
		result.addOutput(statementSubGraph.getOutput());
		result.addOutput(statementSubGraph.getBreaks());
		result.inheritContinue(statementSubGraph);

		this.ioHandlers.push(result);
	}

	// #############################################
	// # Loop statements #
	// #############################################
	/**
	 * Visits a while loop. Links the condition to both the statement and the
	 * next node; links also the statement back to the condition. Links "break"s
	 * to the next vertex and "continue"s to the condition.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTWhileStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode condition = new CFGNode(pStatement.getCondition());
		this.graph.addVertex(condition);

		pStatement.getBody().accept(this);
		SubGraph bodySubGraph = this.ioHandlers.pop();

		this.setOutput(condition, bodySubGraph.getInput(), TrueEdge.class);
		this.setOutput(bodySubGraph.getOutput(), condition, FlowEdge.class);
		this.setOutput(bodySubGraph.getContinues(), condition, FlowEdge.class);

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

		this.setOutput(condition, bodySubGraph.getInput(), TrueEdge.class);
		this.setOutput(bodySubGraph.getOutput(), condition, FlowEdge.class);
		this.setOutput(bodySubGraph.getContinues(), condition, FlowEdge.class);

		result.inheritInput(bodySubGraph);
		result.addOutput(condition);
		result.addOutput(bodySubGraph.getBreaks());
		result.inheritCase(bodySubGraph);

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a for statement. Creates a vertex for each part of the declaration
	 * (initialization, condition and iteration) and links all the parts,
	 * including the body. Links also the "break"s to the next vertex and the
	 * "continue"s to the iteration expression.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
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

		this.setOutput(initSubGraph.getOutput(), condition, FlowEdge.class);
		this.setOutput(condition, bodySubGraph.getInput(), TrueEdge.class);
		this.setOutput(bodySubGraph.getOutput(), incr, FlowEdge.class);
		this.setOutput(incr, condition, FlowEdge.class);
		this.setOutput(bodySubGraph.getContinues(), incr, FlowEdge.class);

		result.inheritInput(initSubGraph);
		result.inheritCase(bodySubGraph);
		result.addOutput(condition);
		result.addOutput(bodySubGraph.getBreaks());

		this.ioHandlers.push(result);
	}

	// #############################################
	// # Jump statements #
	// #############################################
	/**
	 * Visits a goto statement. Adds the statement to the list of gotos to be
	 * linked.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTGotoStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode node = new CFGNode(pStatement);
		this.graph.addVertex(node);
		String label = pStatement.getName().getRawSignature();

		this.gotos
				.add(new AbstractMap.SimpleEntry<String, CFGNode>(label, node));

		result.addInput(node);

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a break statement. Will be linked by a father to another vertex
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTBreakStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode node = new CFGNode(pStatement);
		this.graph.addVertex(node);

		result.addInput(node);
		result.addBreak(node);

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a continue statement. Will be linked by a father to another
	 * vertex.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTContinueStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode node = new CFGNode(pStatement);
		this.graph.addVertex(node);

		result.addInput(node);
		result.addContinue(node);

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a return statement. Will be linked when the visit finishes to the
	 * "end" node.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTReturnStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode node = new CFGNode(pStatement);
		this.graph.addVertex(node);

		result.addInput(node);

		this.returns.add(node);

		this.ioHandlers.push(result);
	}

	// #############################################
	// # Label statements #
	// #############################################
	/**
	 * Visits a label statements. Adds it to the list of labels that will be
	 * linked when the visit finishes.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTLabelStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode node = new CFGNode(pStatement);
		graph.addVertex(node);
		this.labels.put(pStatement.getName().getRawSignature(), node);

		result.addInput(node);
		result.addOutput(node);

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a case statement that will be linked when the father switch
	 * statement will be visited.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
	public void visit(IASTCaseStatement pStatement) {
		SubGraph result = new SubGraph();

		CFGNode node = new CFGNode(pStatement);
		graph.addVertex(node);

		result.addInput(node);
		result.addOutput(node);
		result.addCase(node);

		this.ioHandlers.push(result);
	}

	/**
	 * Visits a default statement that will be linked when the father switch
	 * statement will be visited.
	 * 
	 * @param pStatement
	 *            Node visited
	 */
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
			this.visit((IASTCompoundStatement) statement);
		else if (statement instanceof IASTDeclarationStatement)
			this.visit((IASTDeclarationStatement) statement);
		else if (statement instanceof IASTExpressionStatement)
			this.visit((IASTExpressionStatement) statement);
		else if (statement instanceof IASTNullStatement)
			this.visit((IASTNullStatement) statement);
		else if (statement instanceof IASTIfStatement)
			this.visit((IASTIfStatement) statement);
		else if (statement instanceof IASTSwitchStatement)
			this.visit((IASTSwitchStatement) statement);
		else if (statement instanceof IASTWhileStatement)
			this.visit((IASTWhileStatement) statement);
		else if (statement instanceof IASTDoStatement)
			this.visit((IASTDoStatement) statement);
		else if (statement instanceof IASTForStatement)
			this.visit((IASTForStatement) statement);
		else if (statement instanceof IASTGotoStatement)
			this.visit((IASTGotoStatement) statement);
		else if (statement instanceof IASTBreakStatement)
			this.visit((IASTBreakStatement) statement);
		else if (statement instanceof IASTContinueStatement)
			this.visit((IASTContinueStatement) statement);
		else if (statement instanceof IASTReturnStatement)
			this.visit((IASTReturnStatement) statement);
		else if (statement instanceof IASTLabelStatement)
			this.visit((IASTLabelStatement) statement);
		else if (statement instanceof IASTCaseStatement)
			this.visit((IASTCaseStatement) statement);
		else if (statement instanceof IASTDefaultStatement)
			this.visit((IASTDefaultStatement) statement);
		else
			System.err.println("Hey, this is a BUG! "
					+ statement.getClass().toString());

		return PROCESS_SKIP;
	}

	/**
	 * Returns true if the statement is an expression, a NullStatement or a declaration
	 */
	private boolean isSimpleStatement(IASTStatement pStatement) {
		return (pStatement instanceof IASTExpressionStatement)
				|| (pStatement instanceof IASTNullStatement)
				|| (pStatement instanceof IASTDeclarationStatement);
	}

	/**
	 * Returns true if the statement is a label, a case or a default
	 * @param pStatement
	 * @return
	 */
	private boolean isStartingSimpleStatement(IASTStatement pStatement) {
		return (pStatement instanceof IASTLabelStatement)
				|| (pStatement instanceof IASTCaseStatement)
				|| (pStatement instanceof IASTDefaultStatement);
	}

	/**
	 * Returns true if the statement is a break, a return a goto or a continue
	 * @param pStatement
	 * @return
	 */
	private boolean isEndingSimpleStatement(IASTStatement pStatement) {
		return (pStatement instanceof IASTBreakStatement)
				|| (pStatement instanceof IASTReturnStatement)
				|| (pStatement instanceof IASTGotoStatement)
				|| (pStatement instanceof IASTContinueStatement);
	}

	private void joinVertices(CFGNode pToRemove, CFGNode pNewNode) {
		if (this.returns.contains(pToRemove)) {
			this.returns.remove(pToRemove);
			this.returns.add(pNewNode);
		}

		for (Entry<String, CFGNode> node : this.gotos) {
			if (node.getValue().equals(pToRemove))
				node.setValue(pNewNode);
		}

		this.graph.removeVertex(pToRemove);
	}

	/**
	 * Links the given output to the given input in the CFG
	 * 
	 * @param pOut
	 *            From which nodes start the edges
	 * @param pIn
	 *            In which nodes arrive the edges
	 */
	private void setOutput(List<CFGNode> pOut, List<CFGNode> pIn,
			Class<? extends LabeledEdge> pLabel) {
		for (CFGNode out : pOut)
			for (CFGNode in : pIn)
				this.setOutput(out, in, pLabel);
	}

	/**
	 * Links the given output to the given input in the CFG
	 * 
	 * @param pOut
	 *            From which node start the edges
	 * @param pIn
	 *            In which nodes arrive the edges
	 */
	private void setOutput(CFGNode pOut, List<CFGNode> pIn,
			Class<? extends LabeledEdge> pLabel) {
		for (CFGNode in : pIn)
			this.setOutput(pOut, in, pLabel);
	}

	/**
	 * Links the given output to the given input in the CFG
	 * 
	 * @param pOut
	 *            From which nodes start the edges
	 * @param pIn
	 *            In which node arrive the edges
	 */
	private void setOutput(List<CFGNode> pOut, CFGNode pIn,
			Class<? extends LabeledEdge> pLabel) {
		for (CFGNode out : pOut)
			this.setOutput(out, pIn, pLabel);
	}

	/**
	 * Links the given output to the given input in the CFG
	 * 
	 * @param pOut
	 *            From which node start the edges
	 * @param pIn
	 *            In which node arrive the edges
	 */
	private void setOutput(CFGNode pOut, CFGNode pIn,
			Class<? extends LabeledEdge> pLabel) {
		try {
			LabeledEdge edge = pLabel.newInstance();
			this.graph.addEdge(pOut, pIn, edge);
		} catch (IllegalAccessException e) {
		} catch (InstantiationException e) {
		}
	}

	/**
	 * Links the given output to the given input in the CFG
	 * 
	 * @param pOut
	 *            From which node start the edges
	 * @param pIn
	 *            In which node arrive the edges
	 */
	private void setOutput(CFGNode pOut, CFGNode pIn, LabeledEdge pEdge) {
		this.graph.addEdge(pOut, pIn, pEdge);
	}
}
