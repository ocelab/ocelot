package parser;

import org.antlr.v4.runtime.ParserRuleContext;

import parser.CParser.CompoundStatementContext;
import parser.CParser.ExpressionStatementContext;
import parser.CParser.IterationStatementContext;
import parser.CParser.JumpStatementContext;
import parser.CParser.LabeledStatementContext;
import parser.CParser.SelectionStatementContext;

public class InstrumentatorVisitor extends CBaseVisitor<CodeFragment> {
	@Override
	public CodeFragment visitLabeledStatement(LabeledStatementContext ctx) {
		for (int i = 0; i < ctx.getChildCount() - 1; i++)
			System.out.print(ctx.getChild(i).getText());
		System.out.println("");
		return super.visitLabeledStatement(ctx);
	}
	
	@Override
	public CodeFragment visitCompoundStatement(CompoundStatementContext ctx) {
		CodeFragment result =  super.visitCompoundStatement(ctx);
		return result;
	}
	
	@Override
	public CodeFragment visitExpressionStatement(ExpressionStatementContext ctx) {
		System.out.println(ctx.getText());
		return super.visitExpressionStatement(ctx);
	}
	
	@Override
	public CodeFragment visitSelectionStatement(SelectionStatementContext ctx) {
		for (int i = 0; i < 4; i++)
			System.out.print(ctx.getChild(i).getText());
		System.out.println("");
		return super.visitSelectionStatement(ctx);
	}
	
	@Override
	public CodeFragment visitIterationStatement(IterationStatementContext ctx) {
		CodeFragment result = null;
		if (ctx.getChild(0).getText().equals("do")) {
			System.out.println("do");
			result = super.visitIterationStatement(ctx);
			for (int i = 2; i < ctx.getChildCount(); i++)
				System.out.print(ctx.getChild(i).getText());
		} else {
			for (int i = 0; i < ctx.getChildCount() - 1; i++)
				System.out.print(ctx.getChild(i).getText());
			System.out.println("");
			result = super.visitIterationStatement(ctx);
		}
		return result;
	}
	
	@Override
	public CodeFragment visitJumpStatement(JumpStatementContext ctx) {
		System.out.println(ctx.getText());
		return super.visitJumpStatement(ctx);
	}
	
	private void debug(ParserRuleContext ctx) {
		for (int i = 0; i < ctx.getChildCount(); i++)
			System.out.println(ctx.getChild(i).getText());
	}
}
