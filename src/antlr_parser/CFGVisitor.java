package antlr_parser;

import it.unisa.ocelot.cfg.LabeledEdge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.omg.IOP.CodecFactory;

import antlr_parser.CParser.BlockItemContext;
import antlr_parser.CParser.BlockItemListContext;
import antlr_parser.CParser.CompoundStatementContext;
import antlr_parser.CParser.ExpressionStatementContext;
import antlr_parser.CParser.DeclarationContext;
import antlr_parser.CParser.IterationStatementContext;
import antlr_parser.CParser.JumpStatementContext;
import antlr_parser.CParser.LabeledStatementContext;
import antlr_parser.CParser.SelectionStatementContext;

public class CFGVisitor extends CBaseVisitor<List<List<CodeFragment>>> {
	private static final int L_INPUT = 0;
	private static final int L_OUTPUT = 1;
	private static final int L_CASE = 2;
	private static final int L_BREAK = 3;
	private static final int L_CONTINUE = 4;
	
	private static final LabeledEdge TRUE_EDGE = new LabeledEdge(true);
	private static final LabeledEdge GOTO_EDGE = new LabeledEdge("goto");
	
	private DirectedGraph<CodeFragment, LabeledEdge> graph;
	private Map<String, CodeFragment> labels;
	private List<Entry<String, CodeFragment>> gotos;
	private List<CodeFragment> returns;
	private TokenStream source;
	
	public CFGVisitor(DirectedGraph<CodeFragment, LabeledEdge> pGraph, TokenStream pSource) {
		this.graph = pGraph;
		this.labels = new HashMap<String, CodeFragment>();
		this.gotos = new ArrayList<Entry<String, CodeFragment>>();
		this.returns = new ArrayList<CodeFragment>();
		this.source = pSource;
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
		for (Entry<String, CodeFragment> entry : this.gotos) {
			this.setOutput(entry.getValue(), this.labels.get(entry.getKey()), "goto");
		}
		
		for (CodeFragment node : this.graph.vertexSet()) {
			Set<LabeledEdge> edges = this.graph.outgoingEdgesOf(node);
			if (edges.contains(TRUE_EDGE)) {
				for (LabeledEdge edge : edges)
					if (edge.equals(GOTO_EDGE))
						edge.setLabel(false);
			}
		}
	}
	
	/**
	 * Visit of labeled statements:
	 * - Label 		[identifier]: [statement])
	 * - Case 		case [expr]: [statement]
	 * - Default	default: [statement]
	 */
	@Override
	public List<List<CodeFragment>> visitLabeledStatement(LabeledStatementContext ctx) {
		List<List<CodeFragment>> result = getEmptyResult();
		
		CodeFragment fragment;
		if (ctx.getChild(0).getText().equals("case")) {
			//Case
			fragment = this.getFragment(ctx, 0, 2);
			graph.addVertex(fragment);
			this.addCaseFragment(fragment, result);
		} else if (ctx.getChild(0).getText().equals("default")) {
			//Default
			fragment = this.getFragment(ctx, 0, 1);
			graph.addVertex(fragment);
			this.addCaseFragment(fragment, result);
		} else {
			//Label
			fragment = this.getFragment(ctx, 0, 1);
			graph.addVertex(fragment);
			this.labels.put(ctx.getChild(0).getText(), fragment);
		}
		
		this.addInputFragment(fragment, result);
		this.addOutputFragment(fragment, result);
		
		return result;
	}
	
	/**
	 * Visit of blocks of statements. Concatenates each subgraph.
	 */
	@Override
	public List<List<CodeFragment>> visitCompoundStatement(CompoundStatementContext ctx) {
		if (!ctx.getChild(1).getText().equals("{"))
			return ctx.getChild(1).accept(this);
		else
			return this.getEmptyResult();
	}
	
	@Override
	public List<List<CodeFragment>> visitBlockItemList(BlockItemListContext ctx) {
		List<List<CodeFragment>> result = getEmptyResult();
		
		List<List<CodeFragment>> statement = ctx.getChild(0).accept(this);
		
		if (ctx.getChildCount() == 2) {
			List<List<CodeFragment>> subBlock = ctx.getChild(1).accept(this);
			
			for (CodeFragment fragment : statement.get(L_OUTPUT)) {
				Object label;
				if (fragment.isBreak())
					label = "goto";
				else if (fragment.isIf() || fragment.isLoop())
					label = false;
				else
					label = "goto";
				
				this.setOutput(fragment, subBlock.get(L_INPUT), label);
			}
			
			this.addBreakFragments(subBlock.get(L_BREAK), result);
			this.addContinueFragments(subBlock.get(L_CONTINUE), result);
			this.addCaseFragments(subBlock.get(L_CASE), result);
			this.addOutputFragments(subBlock.get(L_OUTPUT), result);
		} else
			this.addOutputFragments(statement.get(L_OUTPUT), result);
		
		this.addInputFragments(statement.get(L_INPUT), result);
		this.addBreakFragments(statement.get(L_BREAK), result);
		this.addContinueFragments(statement.get(L_CONTINUE), result);
		this.addCaseFragments(statement.get(L_CASE), result);
		
		return result;
	}
	
	@Override
	public List<List<CodeFragment>> visitBlockItem(BlockItemContext ctx) {
		return ctx.getChild(0).accept(this);
	}
	
	
	@Override
	public List<List<CodeFragment>> visitDeclaration(DeclarationContext ctx) {
		List<List<CodeFragment>> result = getEmptyResult();
		CodeFragment fragment = this.getFragment(ctx, 0, ctx.getChildCount()-1);
		
		this.graph.addVertex(fragment);
		
		this.addOutputFragment(fragment, result);
		this.addInputFragment(fragment, result);
		
		return result;
	}
	/**
	 * Visits statements made of expressions
	 */
	@Override
	public List<List<CodeFragment>> visitExpressionStatement(ExpressionStatementContext ctx) {
		CodeFragment fragment = this.getFragment(ctx, 0, 0);
		this.graph.addVertex(fragment);
		
		List<List<CodeFragment>> result = getEmptyResult();
		
		this.addInputFragment(fragment, result);
		this.addOutputFragment(fragment, result);
		
		return result;
	}
	
	/**
	 * Visits statements made of control structures
	 */
	@Override
	public List<List<CodeFragment>> visitSelectionStatement(SelectionStatementContext ctx) {
		List<List<CodeFragment>> result = getEmptyResult();
		
		if (ctx.getChild(0).getText().equals("if")) {
			// If
			CodeFragment ifDeclaration = this.getFragment(ctx, 0, 3);
			this.graph.addVertex(ifDeclaration);
			
			//Gets the "then" statements, visits it and sets all the exiting nodes as exiting nodes of the "if" statement
			List<List<CodeFragment>> thenStatement = ctx.getChild(4).accept(this);
			this.setOutput(ifDeclaration, thenStatement.get(0), true);
			
			if (ctx.getChildCount() > 5 && ctx.getChild(5).getText().equals("else")) {
				//Gets the "then" statements, visits it and sets all the exiting nodes as exiting nodes of the "if" statement
				List<List<CodeFragment>> elseStatement = ctx.getChild(6).accept(this);
				this.setOutput(ifDeclaration, elseStatement.get(0), false);
				
				this.addBreakFragments(elseStatement.get(L_BREAK), result);
				this.addOutputFragments(elseStatement.get(L_OUTPUT), result);
				this.addContinueFragments(elseStatement.get(L_CONTINUE), result);
			} else {
				this.addOutputFragment(ifDeclaration, result);
			}
			
			this.addInputFragment(ifDeclaration, result);
			this.addOutputFragments(thenStatement.get(L_OUTPUT), result);
			this.addBreakFragments(thenStatement.get(L_BREAK), result);
			this.addContinueFragments(thenStatement.get(L_CONTINUE), result);
		} else {
			// Case
			CodeFragment switchDeclaration = this.getFragment(ctx, 0, 3);
			this.graph.addVertex(switchDeclaration);
			
			List<List<CodeFragment>> statement = ctx.getChild(4).accept(this);
			
			for (CodeFragment fragment : statement.get(L_CASE)) {
				this.setOutput(switchDeclaration, fragment, fragment.getCode().substring(5));
			}
			
			this.addInputFragment(switchDeclaration, result);
			this.addOutputFragments(statement.get(L_OUTPUT), result);
			this.addOutputFragments(statement.get(L_BREAK), result);
			this.addContinueFragments(statement.get(L_CONTINUE), result);
		}
		return result;
	}
	
	/**
	 * Visits statements made of loops:
	 * While		while ([expression]) [statement]
	 * Do-while		do [statement] while ([expression]);
	 * For			for ([expression?];[expression?];[expression?]) [statement]
	 * FOr			for ([declaration] [expression?]; [expression?]) [statement]
	 */
	@Override
	public List<List<CodeFragment>> visitIterationStatement(IterationStatementContext ctx) {
		List<List<CodeFragment>> result = getEmptyResult();
		String firstToken = ctx.getChild(0).getText();
		
		if (firstToken.equals("while")) {
			// While
			CodeFragment whileCondition = this.getFragment(ctx, 0, 3);
			this.graph.addVertex(whileCondition);
			List<List<CodeFragment>> statement = ctx.getChild(4).accept(this);
			
			this.setOutput(whileCondition, statement.get(L_INPUT), true);
			this.setOutput(statement.get(L_OUTPUT), whileCondition, "goto");
			
			this.setOutput(statement.get(L_CONTINUE), whileCondition, "goto");
			
			this.addInputFragment(whileCondition, result);
			this.addOutputFragment(whileCondition, result);
			this.addOutputFragments(statement.get(L_BREAK), result);
		} else if (firstToken.equals("for")) {
			// For
			int indexInit 		= -1;
			int indexCondition 	= -1;
			int indexIncrement 	= -1;
			int indexStatement 	= -1;
			
			int[] indexes = new int[4];
			int j = 0;
			for (int i = 1; i < ctx.getChildCount(); i++) {
				ParseTree child = ctx.getChild(i);
				
				if (child.getText().equals(";")) {
					j++;
				}
			}
			
			if (ctx.getChild(3).getText().equals(";")) {
				indexInit = 2;
				indexCondition = 4;
				indexIncrement = 6;
				indexStatement = 8;
			} else {
				indexInit = 2;
				indexCondition = 3;
				indexIncrement = 5;
				indexStatement = 7;
			}
			
			CodeFragment forInit = this.getFragment(ctx, indexInit, indexInit);
			CodeFragment forCondition = this.getFragment(ctx, indexCondition, indexCondition);
			CodeFragment forIncrement = this.getFragment(ctx, indexIncrement, indexIncrement);
			
			this.graph.addVertex(forInit);
			this.graph.addVertex(forCondition);
			this.graph.addVertex(forIncrement);
			
			List<List<CodeFragment>> statement = ctx.getChild(indexStatement).accept(this);
			
			this.setOutput(forInit, forCondition, "goto");
			this.setOutput(forCondition, statement.get(L_INPUT), true);
			this.setOutput(statement.get(L_OUTPUT), forIncrement, "goto");
			this.setOutput(forIncrement, forCondition, "goto");
			this.setOutput(statement.get(L_CONTINUE), forIncrement, "goto");
			
			this.addInputFragment(forInit, result);
			this.addOutputFragment(forCondition, result);
			this.addOutputFragments(statement.get(L_BREAK), result);
		} else if (firstToken.equals("do")) {
			// Do-while
			CodeFragment whileCondition = this.getFragment(ctx, 2, 5);
			this.graph.addVertex(whileCondition);
			List<List<CodeFragment>> statement = ctx.getChild(4).accept(this);
			
			this.setOutput(whileCondition, statement.get(L_INPUT), true);
			this.setOutput(statement.get(L_OUTPUT), whileCondition, "goto");
			this.setOutput(statement.get(L_CONTINUE), whileCondition, "goto");
			
			this.addInputFragments(statement.get(L_INPUT), result);
			this.addOutputFragment(whileCondition, result);
			this.addOutputFragments(statement.get(L_BREAK), result);
		}
		
		return result;
	}
	
	@Override
	public List<List<CodeFragment>> visitJumpStatement(JumpStatementContext ctx) {
		//Make sure that all jump statements have no output fragments!!
		List<List<CodeFragment>> result = this.getEmptyResult();
		
		if (ctx.getChild(0).getText().equals("goto")) {
			CodeFragment fragment = this.getFragment(ctx, 0, 1);
			this.graph.addVertex(fragment);
			
			String label = ctx.getChild(1).getText();
			this.gotos.add(new AbstractMap.SimpleEntry<String, CodeFragment>(label, fragment));
			
			this.addInputFragment(fragment, result);
		} else if (ctx.getChild(0).getText().equals("continue")) {
			CodeFragment fragment = this.getFragment(ctx, 0, 0);
			this.graph.addVertex(fragment);
			
			this.addContinueFragment(fragment, result);
			this.addInputFragment(fragment, result);
		} else if (ctx.getChild(0).getText().equals("break")) {
			CodeFragment fragment = this.getFragment(ctx, 0, 0);
			this.graph.addVertex(fragment);
			
			this.addInputFragment(fragment, result);
			this.addBreakFragment(fragment, result);
		} else if (ctx.getChild(0).getText().equals("return")) {
			CodeFragment fragment = this.getFragment(ctx, 0, 1);
			this.graph.addVertex(fragment);
			
			this.returns.add(fragment);
			
			this.addInputFragment(fragment, result);
		}
		
		return result;
	}
	
	private void debug(ParserRuleContext ctx) {
		for (int i = 0; i < ctx.getChildCount(); i++)
			System.out.println(ctx.getChild(i).getText());
	}
	
	private List<List<CodeFragment>> getEmptyResult() {
		List<List<CodeFragment>> result = new ArrayList<List<CodeFragment>>();
		result.add(null);
		result.add(null);
		result.add(null);
		result.add(null);
		result.add(null);
		
		result.set(L_INPUT, new ArrayList<CodeFragment>());		//Setup the INPUT stream
		result.set(L_OUTPUT, new ArrayList<CodeFragment>());	//Setup the OUTPUT stream
		result.set(L_CASE, new ArrayList<CodeFragment>());		//Setup the CASE stream
		result.set(L_BREAK, new ArrayList<CodeFragment>());		//Setup the BREAK stream
		result.set(L_CONTINUE, new ArrayList<CodeFragment>());	//Setup the CONTINUE stream
		
		return result;
	}
	
	/**
	 * Adds a set of output fragments to the result stream
	 * @param pFragment Fragment to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addOutputFragment(CodeFragment pFragment, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_OUTPUT).add(pFragment);
	}
	
	/**
	 * Adds a set of output fragments to the result stream
	 * @param pFragments Fragments to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addOutputFragments(List<CodeFragment> pFragments, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_OUTPUT).addAll(pFragments);
	}
	
	/**
	 * Adds a set of input fragments to the result stream
	 * @param pFragment Fragment to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addInputFragment(CodeFragment pFragment, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_INPUT).add(pFragment);
	}
	
	/**
	 * Adds a set of input fragments to the result stream
	 * @param pFragments Fragments to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addInputFragments(List<CodeFragment> pFragments, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_INPUT).addAll(pFragments);
	}
	
	/**
	 * Adds a set of case fragments to the result stream
	 * @param pFragment Fragment to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addCaseFragment(CodeFragment pFragment, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_CASE).add(pFragment);
	}
	
	/**
	 * Adds a set of case fragments to the result stream
	 * @param pFragments Fragments to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addCaseFragments(List<CodeFragment> pFragments, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_CASE).addAll(pFragments);
	}
	
	/**
	 * Adds a set of break fragments to the result stream
	 * @param pFragment Fragment to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addBreakFragment(CodeFragment pFragment, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_BREAK).add(pFragment);
	}
	
	/**
	 * Adds a set of break fragments to the result stream
	 * @param pFragments Fragments to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addBreakFragments(List<CodeFragment> pFragments, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_BREAK).addAll(pFragments);
	}
	
	/**
	 * Adds a set of continue fragments to the result stream
	 * @param pFragment Fragment to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addContinueFragment(CodeFragment pFragment, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_CONTINUE).add(pFragment);
	}
	
	/**
	 * Adds a set of continue fragments to the result stream
	 * @param pFragments Fragments to be added
	 * @param pResultHandler Result in which is needed to add the fragments
	 */
	private void addContinueFragments(List<CodeFragment> pFragments, List<List<CodeFragment>> pResultHandler) {
		pResultHandler.get(L_CONTINUE).addAll(pFragments);
	}
	
	/**
	 * Links the given output to the given input in the CFG
	 * @param pOut From which nodes start the edges
	 * @param pIn In which nodes arrive the edges
	 */
	private void setOutput(List<CodeFragment> pOut, List<CodeFragment> pIn, Object pLabel) {
		for (CodeFragment out : pOut) 
			for (CodeFragment in : pIn) {
				LabeledEdge edge = this.graph.addEdge(out, in);
				edge.setLabel(pLabel);
			}
	}
	
	/**
	 * Links the given output to the given input in the CFG
	 * @param pOut From which node start the edges
	 * @param pIn In which nodes arrive the edges
	 */
	private void setOutput(CodeFragment pOut, List<CodeFragment> pIn, Object pLabel) {
		for (CodeFragment in: pIn) {
			LabeledEdge edge = this.graph.addEdge(pOut, in);
			edge.setLabel(pLabel);
		}
	}
	
	/**
	 * Links the given output to the given input in the CFG
	 * @param pOut From which nodes start the edges
	 * @param pIn In which node arrive the edges
	 */
	private void setOutput(List<CodeFragment> pOut, CodeFragment pIn, Object pLabel) {
		for (CodeFragment out: pOut) {
			LabeledEdge edge = this.graph.addEdge(out, pIn);
			edge.setLabel(pLabel);
		}
	}
	
	/**
	 * Links the given output to the given input in the CFG
	 * @param pOut From which node start the edges
	 * @param pIn In which node arrive the edges
	 */
	private void setOutput(CodeFragment pOut, CodeFragment pIn, Object pLabel) {
			LabeledEdge edge = this.graph.addEdge(pOut, pIn);
			edge.setLabel(pLabel);
	}
	
	/**
	 * Returns the code fragment from child pFrom to child pTo (both included). 
	 * @param ctx Current context (from which take the children)
	 * @param pFrom ID of starting child
	 * @param pTo ID of ending child
	 * @return
	 */
	private CodeFragment getFragment(ParserRuleContext ctx, int pFrom, int pTo) {
		return new CodeFragment(ctx.getChild(pFrom).getSourceInterval().a, ctx.getChild(pTo).getSourceInterval().b, this.source);
	}
}
