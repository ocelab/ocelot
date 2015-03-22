package it.unisa.ocelot.instrumentator;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;

public class InstrumentatorVisitor extends ASTVisitor {
	private IASTExpression lastExpression;
	public InstrumentatorVisitor() {
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		
	}
		
	public IASTExpression transformEquals(IASTBinaryExpression pExpression, boolean pNegation) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "eq", false, IASTBinaryExpression.op_equals);
		else
			return this.transformNotEquals(pExpression, false);
	}
	
	public IASTExpression transformGreaterThan(IASTBinaryExpression pExpression, boolean pNegation) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "gt", false, IASTBinaryExpression.op_greaterThan);
		else
			return this.transformLessEquals(pExpression, false);
	}
	
	public IASTExpression transformGreaterEquals(IASTBinaryExpression pExpression, boolean pNegation) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "ge", false, IASTBinaryExpression.op_greaterEqual);
		else
			return this.transformLessThan(pExpression, false);
	}
	
	public IASTExpression transformLessThan(IASTBinaryExpression pExpression, boolean pNegation) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "ge", true, IASTBinaryExpression.op_lessThan);
		else
			return this.transformGreaterEquals(pExpression, false);
	}
	
	public IASTExpression transformLessEquals(IASTBinaryExpression pExpression, boolean pNegation) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "gt", true, IASTBinaryExpression.op_lessEqual);
		else
			return this.transformGreaterThan(pExpression, false);
	}
	
	public IASTExpression transformNotEquals(IASTBinaryExpression pExpression, boolean pNegation) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "neq", false, IASTBinaryExpression.op_notequals);
		else
			return this.transformEquals(pExpression, false);
	}
	
	public IASTExpression transformAnd(IASTBinaryExpression pExpression, boolean pNegation) {
		if (!pNegation) {
			return this.transformLogicalExpression(pExpression, false, "and", IASTBinaryExpression.op_logicalAnd);
		} else
			return this.transformLogicalExpression(pExpression, true, "or", IASTBinaryExpression.op_logicalOr);
	}
	
	public IASTExpression transformOr(IASTBinaryExpression pExpression, boolean pNegation) {
		if (!pNegation) {
			return this.transformLogicalExpression(pExpression, false, "or", IASTBinaryExpression.op_logicalOr);
		} else
			return this.transformLogicalExpression(pExpression, true, "and", IASTBinaryExpression.op_logicalAnd);
	}
	
	public IASTExpression transformNot(IASTUnaryExpression pExpression, boolean pNegation) {
		IASTExpression operand = pExpression.getOperand();
		return this.transformExpression(operand, !pNegation);
	}
	
	public IASTExpression transformExpression(IASTExpression expression, boolean pNegation) {
		if (expression instanceof IASTBinaryExpression) {
			IASTBinaryExpression realExpression = (IASTBinaryExpression)expression;
			if (realExpression.getOperator() == IASTBinaryExpression.op_equals)
				return this.transformEquals(realExpression, pNegation);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_greaterThan)
				return this.transformGreaterThan(realExpression, pNegation);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_greaterEqual)
				return this.transformGreaterEquals(realExpression, pNegation);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_lessThan)
				return this.transformLessThan(realExpression, pNegation);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_lessEqual)
				return this.transformLessEquals(realExpression, pNegation);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_notequals)
				return this.transformNotEquals(realExpression, pNegation);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_logicalAnd)
				return this.transformAnd(realExpression, pNegation);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_logicalOr)
				return this.transformOr(realExpression, pNegation);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_assign || 
					realExpression.getOperator() == IASTBinaryExpression.op_plusAssign ||
					realExpression.getOperator() == IASTBinaryExpression.op_minusAssign ||
					realExpression.getOperator() == IASTBinaryExpression.op_binaryAndAssign ||
					realExpression.getOperator() == IASTBinaryExpression.op_shiftLeftAssign ||
					realExpression.getOperator() == IASTBinaryExpression.op_binaryOrAssign ||
					realExpression.getOperator() == IASTBinaryExpression.op_binaryXorAssign ||
					realExpression.getOperator() == IASTBinaryExpression.op_divideAssign ||
					realExpression.getOperator() == IASTBinaryExpression.op_moduloAssign ||
					realExpression.getOperator() == IASTBinaryExpression.op_multiplyAssign ||
					realExpression.getOperator() == IASTBinaryExpression.op_shiftRightAssign) {
				IASTExpression operand2 = realExpression.getOperand2();
				return this.transformExpression(operand2, pNegation);
			} else {
				IASTExpression operand1 = realExpression.getOperand1();
				IASTExpression operand2 = realExpression.getOperand2();
				
				realExpression.setOperand1(this.transformExpression(operand1, pNegation));
				realExpression.setOperand2(this.transformExpression(operand2, pNegation));
				
				return realExpression;
			}
				
			
		} else if (expression instanceof IASTUnaryExpression) {
			IASTUnaryExpression realExpression = (IASTUnaryExpression)expression;
			if (realExpression.getOperator() == IASTUnaryExpression.op_not) {
				return this.transformNot(realExpression, pNegation);
			} else {
				IASTExpression operand = realExpression.getOperand();
				return this.transformExpression(operand, pNegation);
			}
		}
		// TODO Auto-generated method stub
		return expression;
	}
	
	@Override
	public int visit(IASTExpression expression) {
		this.lastExpression = this.transformExpression(expression, false);
		return PROCESS_SKIP;
	}
	
	public void visit(IASTIfStatement statement) {
		IASTExpression[] instrArgs = new IASTExpression[2];
		instrArgs[0] = statement.getConditionExpression().copy();
		statement.getConditionExpression().accept(this);
		instrArgs[1] = this.lastExpression;
		
		IASTFunctionCallExpression instrFunction = makeFunctionCall("_f_ocelot_trace", instrArgs);
		statement.setConditionExpression(instrFunction);
	}
	
	public void visit(IASTSwitchStatement statement) {
	}
	
	public void visit(IASTWhileStatement statement) {
		statement.getCondition().accept(this);
		statement.setCondition(this.lastExpression);
	}
	
	public void visit(IASTDoStatement statement) {
		statement.getCondition().accept(this);
		statement.setCondition(this.lastExpression);
	}
	
	public void visit(IASTForStatement statement) {
		statement.getConditionExpression().accept(this);
		statement.setConditionExpression(this.lastExpression);
	}
	
	public void visit(IASTCaseStatement statement) {
	}
	
	public void visit(IASTDefaultStatement statement) {
	}
	
	public int visit(IASTStatement statement) {
		
		System.out.println(statement.getClass().toString());
		if (statement instanceof IASTIfStatement)
			this.visit((IASTIfStatement)statement);
		else if (statement instanceof IASTSwitchStatement)
			this.visit((IASTSwitchStatement)statement);
		else if (statement instanceof IASTWhileStatement)
			this.visit((IASTWhileStatement)statement);
		else if (statement instanceof IASTDoStatement)
			this.visit((IASTDoStatement)statement);
		else if (statement instanceof IASTForStatement)
			this.visit((IASTForStatement)statement);
		else if (statement instanceof IASTCaseStatement)
			this.visit((IASTCaseStatement)statement);
		else if (statement instanceof IASTDefaultStatement)
			this.visit((IASTDefaultStatement)statement);
		
		return PROCESS_CONTINUE;
	}
	
	
	private IASTFunctionCallExpression makeFunctionCall(String pName, IASTExpression[] pArguments) {
		IASTFunctionCallExpression call = new CASTFunctionCallExpression();
		IASTIdExpression name = new CASTIdExpression(new CASTName(pName.toCharArray()));
		
		call.setFunctionNameExpression(name);
		call.setArguments(pArguments);
		
		return call;
	}
	
	private IASTExpression transformLogicalExpression(IASTBinaryExpression pExpression, boolean pNegation, String pOperator, int pRealOperator) {
		pExpression.setOperator(pRealOperator);
		IASTExpression op1 = pExpression.getOperand1();
		IASTExpression op2 = pExpression.getOperand2();
		
		IASTExpression instrumentedOp1 = this.transformExpression(op1, pNegation);
		IASTExpression instrumentedOp2 = this.transformExpression(op2, pNegation);
		
		IASTExpression[] operationArgs = new IASTExpression[2];
		operationArgs[0] = instrumentedOp1;
		operationArgs[1] = instrumentedOp2;
		
		IASTFunctionCallExpression operationFunction = makeFunctionCall("_f_ocelot_" + pOperator, operationArgs);
		
		return operationFunction;
	}
	
	private IASTExpression transformComparisonExpression(IASTBinaryExpression pExpression, String pOperator, boolean invert, int pRealOperator) {
		pExpression.setOperator(pRealOperator);
		IASTExpression operand1 = pExpression.getOperand1();
		IASTExpression operand2 = pExpression.getOperand2();
		
		if (invert) {
			IASTExpression temp = operand1;
			operand1 = operand2;
			operand2 = temp;
		}
		
		IASTExpression[] operationArgs = new IASTExpression[2];
		operationArgs[0] = operand1;
		operationArgs[1] = operand2;
		
		IType op1Type = operand1.getExpressionType();
		IType op2Type = operand2.getExpressionType();
		
		IASTFunctionCallExpression operationFunction;
		if (op1Type instanceof IBasicType && op2Type instanceof IBasicType) {
			operationFunction = makeFunctionCall("_f_ocelot_" + pOperator+ "_numeric", operationArgs);
		} else if (op1Type instanceof ICPointerType && op2Type instanceof ICPointerType) {
			operationFunction = makeFunctionCall("_f_ocelot_" + pOperator+ "_pointer", operationArgs);
		} else {
			System.err.println("I can't handle this type situation: " + op1Type.toString() + "-" + op2Type.toString());
			return pExpression;
		}
		
		return operationFunction;
	}
}
