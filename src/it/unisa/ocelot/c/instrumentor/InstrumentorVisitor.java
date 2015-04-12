package it.unisa.ocelot.c.instrumentor;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.cfg.CaseEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CStructure;
import org.eclipse.cdt.internal.core.dom.parser.c.CTypedef;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;

public class InstrumentorVisitor extends ASTVisitor {
	private IASTExpression lastExpression;
	private Stack<List<IASTStatement>> switchExpressions;
	private String callMacro;
	
	public InstrumentorVisitor() {
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitDeclarators = true;
		this.shouldVisitTranslationUnit = true;
		
		this.callMacro = "";
		
		this.switchExpressions = new Stack<List<IASTStatement>>();
	}
	
	@Override
	public int visit(IASTTranslationUnit tu) {
		/* Simulates the generation of a CFG in order to correctly retrieve the "case" unique ids.
		 * The resulting CFG will never be used.
		 * TODO define a lightweight visitor able to initialize the "case" unique ids only correctly.
		 */
		tu.accept(new CFGVisitor(new CFG()));
		
		return super.visit(tu);
	}
		
	public IASTExpression transformEquals(IASTBinaryExpression pExpression, boolean pNegation, boolean pTransPerformed) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "eq", IASTBinaryExpression.op_equals);
		else
			return this.transformNotEquals(pExpression, false, pTransPerformed);
	}
	
	public IASTExpression transformGreaterThan(IASTBinaryExpression pExpression, boolean pNegation, boolean pTransPerformed) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "gt", IASTBinaryExpression.op_greaterThan);
		else
			return this.transformLessEquals(pExpression, false, pTransPerformed);
	}
	
	public IASTExpression transformGreaterEquals(IASTBinaryExpression pExpression, boolean pNegation, boolean pTransPerformed) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "ge", IASTBinaryExpression.op_greaterEqual);
		else
			return this.transformLessThan(pExpression, false, pTransPerformed);
	}
	
	public IASTExpression transformLessThan(IASTBinaryExpression pExpression, boolean pNegation, boolean pTransPerformed) {
		if (!pNegation) {
			IASTExpression temp = pExpression.getOperand1();
			pExpression.setOperand1(pExpression.getOperand2());
			pExpression.setOperand2(temp);
			
			return this.transformComparisonExpression(pExpression, "gt", IASTBinaryExpression.op_lessThan);
		} else
			return this.transformGreaterEquals(pExpression, false, pTransPerformed);
	}
	
	public IASTExpression transformLessEquals(IASTBinaryExpression pExpression, boolean pNegation, boolean pTransPerformed) {
		if (!pNegation) {
			IASTExpression temp = pExpression.getOperand1();
			pExpression.setOperand1(pExpression.getOperand2());
			pExpression.setOperand2(temp);
			
			return this.transformComparisonExpression(pExpression, "ge", IASTBinaryExpression.op_lessEqual);
		} else
			return this.transformGreaterThan(pExpression, false, pTransPerformed);
	}
	
	public IASTExpression transformNotEquals(IASTBinaryExpression pExpression, boolean pNegation, boolean pTransPerformed) {
		if (!pNegation)
			return this.transformComparisonExpression(pExpression, "neq", IASTBinaryExpression.op_notequals);
		else
			return this.transformEquals(pExpression, false, pTransPerformed);
	}
	
	public IASTExpression transformAnd(IASTBinaryExpression pExpression, boolean pNegation, boolean pTransPerformed) {
		if (!pNegation) {
			return this.transformLogicalExpression(pExpression, false, "and", IASTBinaryExpression.op_logicalAnd);
		} else
			return this.transformLogicalExpression(pExpression, true, "or", IASTBinaryExpression.op_logicalOr);
	}
	
	public IASTExpression transformOr(IASTBinaryExpression pExpression, boolean pNegation, boolean pTransPerformed) {
		if (!pNegation) {
			return this.transformLogicalExpression(pExpression, false, "or", IASTBinaryExpression.op_logicalOr);
		} else
			return this.transformLogicalExpression(pExpression, true, "and", IASTBinaryExpression.op_logicalAnd);
	}
	
	public IASTExpression transformNot(IASTUnaryExpression pExpression, boolean pNegation, boolean pTransPerformed) {
		IASTExpression operand = pExpression.getOperand();
		return this.transformDistanceExpression(operand, !pNegation, pTransPerformed);
	}
	
	public IASTExpression transformDistanceExpression(IASTExpression expression, boolean pNegation, boolean pTransPerformed) {
		if (expression instanceof IASTBinaryExpression) {
			IASTBinaryExpression realExpression = (IASTBinaryExpression)expression;
			if (realExpression.getOperator() == IASTBinaryExpression.op_equals)
				return this.transformEquals(realExpression, pNegation, pTransPerformed);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_greaterThan)
				return this.transformGreaterThan(realExpression, pNegation, pTransPerformed);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_greaterEqual)
				return this.transformGreaterEquals(realExpression, pNegation, pTransPerformed);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_lessThan)
				return this.transformLessThan(realExpression, pNegation, pTransPerformed);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_lessEqual)
				return this.transformLessEquals(realExpression, pNegation, pTransPerformed);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_notequals)
				return this.transformNotEquals(realExpression, pNegation, pTransPerformed);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_logicalAnd)
				return this.transformAnd(realExpression, pNegation, pTransPerformed);
			else if (realExpression.getOperator() == IASTBinaryExpression.op_logicalOr)
				return this.transformOr(realExpression, pNegation, pTransPerformed);
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
				IASTExpression operand = realExpression.getOperand1();
				return this.transformDistanceExpression(operand, pNegation, pTransPerformed);
			} else {
				IASTExpression[] arguments = new IASTExpression[1];
				arguments[0] = realExpression;
				
				IASTExpression operand1 = realExpression.getOperand1();
				IASTExpression operand2 = realExpression.getOperand2();
				
				realExpression.setOperand1(this.transformDistanceExpression(operand1, pNegation, pTransPerformed));
				realExpression.setOperand2(this.transformDistanceExpression(operand2, pNegation, pTransPerformed));
				
				if (!pTransPerformed)
					return makeFunctionCall("_f_ocelot_istrue", arguments);
				else
					return realExpression;
			}
				
			
		} else if (expression instanceof IASTUnaryExpression) {
			IASTUnaryExpression realExpression = (IASTUnaryExpression)expression;
			if (realExpression.getOperator() == IASTUnaryExpression.op_not) {
				return this.transformNot(realExpression, pNegation, pTransPerformed);
			} else if (realExpression.getOperator() == IASTUnaryExpression.op_star ||
					realExpression.getOperator() == IASTUnaryExpression.op_amper) {
				return realExpression;
			} else {
				IASTExpression operand = realExpression.getOperand();
				return this.transformDistanceExpression(operand, pNegation, pTransPerformed);
			}
		} else if (expression instanceof IASTIdExpression ||
				expression instanceof IASTLiteralExpression) {
			if (!pTransPerformed) {
				IASTExpression[] arguments = new IASTExpression[1];
				arguments[0] = expression; 
						
				IASTExpression result;
				if (!pNegation)
					result = makeFunctionCall("_f_ocelot_istrue", arguments);
				else
					result = makeFunctionCall("_f_ocelot_isfalse", arguments);
				return result;
			}
		} else if (expression instanceof IASTFunctionCallExpression) { 
			return makeFunctionCall("_f_ocelot_get_fcall", new IASTExpression[0]);
		}

		return expression;
	}
	
	public IASTExpression transformOriginalExpression(IASTExpression expression) {
		if (expression instanceof IASTBinaryExpression) {
			IASTBinaryExpression realExpression = (IASTBinaryExpression)expression;
			
			realExpression.setOperand1(this.transformOriginalExpression(realExpression.getOperand1()));
			realExpression.setOperand2(this.transformOriginalExpression(realExpression.getOperand2()));
			
			return realExpression;
		} else if (expression instanceof IASTUnaryExpression) {
			IASTUnaryExpression realExpression = (IASTUnaryExpression)expression;
			realExpression.setOperand(this.transformOriginalExpression(realExpression.getOperand()));
			
			return realExpression;
		} else if (expression instanceof IASTFunctionCallExpression) {
			IASTExpression[] arguments = new IASTExpression[1];
			arguments[0] = expression; 
			
			IType type = getType(expression);
			if (type instanceof IBasicType)
				return makeFunctionCall("_f_ocelot_reg_fcall_numeric", arguments);
			else if (type instanceof ICPointerType)
				return makeFunctionCall("_f_ocelot_reg_fcall_pointer", arguments);
			else {
				System.err.println("I can't handle this type situation: " + type.toString());
				return makeFunctionCall("_f_ocelot_reg_fcall_numeric", arguments);
			}
		}
		return expression;
	}
	
	@Override
	public int visit(IASTExpression expression) {
		this.lastExpression = this.transformDistanceExpression(expression, false, false);
		return PROCESS_SKIP;
	}
	
	public void visit(IASTIfStatement statement) {
		IASTExpression[] instrArgs = new IASTExpression[3];
		instrArgs[0] = this.transformOriginalExpression(statement.getConditionExpression().copy());
		instrArgs[1] = this.transformDistanceExpression(this.cloneExpression(statement.getConditionExpression()), false, false);
		instrArgs[2] = this.transformDistanceExpression(this.cloneExpression(statement.getConditionExpression()), true, false);
		
		IASTFunctionCallExpression instrFunction = makeFunctionCall("_f_ocelot_trace", instrArgs);
		statement.setConditionExpression(instrFunction);
	}
	
	public void visit(IASTSwitchStatement statement) {
		//OK, but handle types!! 
		CASTLiteralExpression cTrue = new CASTLiteralExpression(CASTLiteralExpression.lk_integer_constant, new char[] {'1'});
		CASTCompoundStatement substitute = new CASTCompoundStatement();
		switchExpressions.push(new ArrayList<IASTStatement>());
		statement.getBody().accept(this);
		System.out.println(statement.getControllerExpression().getExpressionType());
		List<IASTStatement> caseStatements = switchExpressions.pop();
		CASTBinaryExpression defaultExpression = new CASTBinaryExpression(
				CASTBinaryExpression.op_logicalAnd, 
				cTrue.copy(), 
				cTrue.copy()
		);
		
		CASTBinaryExpression currentDefaultExpression = defaultExpression;
		
		int identifier = 0;
		for (IASTStatement aCase : caseStatements) {
			IASTExpression distanceCalculation;
			String label;
			
			if (aCase instanceof IASTCaseStatement) {
				IASTCaseStatement realCase = (IASTCaseStatement)aCase;
				
				label = new ASTWriter().write(realCase.getExpression());
				
				distanceCalculation = new CASTBinaryExpression(
						CASTBinaryExpression.op_equals,
						this.cloneExpression(statement.getControllerExpression()), 
						this.cloneExpression(realCase.getExpression())
				);
				
				//Creates an AND on with the != on the left and a "true"
				CASTBinaryExpression defaultExpressionSubtree = 
						new CASTBinaryExpression(
								CASTBinaryExpression.op_logicalAnd, 
								new CASTBinaryExpression(
										CASTBinaryExpression.op_notequals, 
										this.cloneExpression(statement.getControllerExpression()), 
										this.cloneExpression(realCase.getExpression())
								), 
								cTrue.copy()
						);
				
				currentDefaultExpression.setOperand2(defaultExpressionSubtree);
				currentDefaultExpression = defaultExpressionSubtree;
			} else {
				label = "default";
				distanceCalculation = defaultExpression;
			}
			
			IASTExpression[] arguments = new IASTExpression[3];
			arguments[0] = new CASTLiteralExpression(CASTLiteralExpression.lk_integer_constant, String.valueOf(CaseEdge.retrieveUniqueId(label)).toCharArray());
			arguments[1] = this.transformDistanceExpression(distanceCalculation, false, false);
			arguments[2] = distanceCalculation.copy();
			
			substitute.addStatement(new CASTExpressionStatement(makeFunctionCall("_f_ocelot_trace_case", arguments)));
			identifier++;
		}
		
		IASTNode parent = statement.getParent();
		
		if (parent instanceof IASTStatement) {
			if (parent instanceof IASTCompoundStatement) {
				CASTCompoundStatement realParent = (CASTCompoundStatement)parent;
				realParent.replace(statement, substitute);
				//for (int i = 0; i < realParent.getStatements().length; i++)
				//	if (statement == realParent.getStatements()[i])
						
			} else if (parent instanceof IASTIfStatement) {
				IASTIfStatement realParent = (IASTIfStatement)parent;
				if (statement == realParent.getThenClause())
					realParent.setThenClause(substitute);
				else
					realParent.setElseClause(substitute);
			} else if (parent instanceof IASTSwitchStatement) {
				IASTSwitchStatement realParent = (IASTSwitchStatement)parent;
				realParent.setBody(substitute);
			} else if (parent instanceof IASTWhileStatement) {
				IASTWhileStatement realParent = (IASTWhileStatement)parent;
				realParent.setBody(substitute);
			} else if (parent instanceof IASTDoStatement) {
				IASTDoStatement realParent = (IASTDoStatement)parent;
				realParent.setBody(substitute);
			} else if (parent instanceof IASTForStatement) {
				IASTForStatement realParent = (IASTForStatement)parent;
				if (statement == realParent.getInitializerStatement())
					realParent.setInitializerStatement(substitute);
				else
					realParent.setBody(substitute);
			}
		}
		
		substitute.addStatement(statement);
	}
	
	public void visit(IASTWhileStatement statement) {
		IASTExpression[] instrArgs = new IASTExpression[3];
		instrArgs[0] = this.transformOriginalExpression(statement.getCondition().copy());
		instrArgs[1] = this.transformDistanceExpression(this.cloneExpression(statement.getCondition()), false, false);
		instrArgs[2] = this.transformDistanceExpression(this.cloneExpression(statement.getCondition()), true, false);
		
		IASTFunctionCallExpression instrFunction = makeFunctionCall("_f_ocelot_trace", instrArgs);
		statement.setCondition(instrFunction);
	}
	
	public void visit(IASTDoStatement statement) {
		IASTExpression[] instrArgs = new IASTExpression[3];
		instrArgs[0] = this.transformOriginalExpression(statement.getCondition().copy());
		instrArgs[1] = this.transformDistanceExpression(this.cloneExpression(statement.getCondition()), false, false);
		instrArgs[2] = this.transformDistanceExpression(this.cloneExpression(statement.getCondition()), true, false);
		
		IASTFunctionCallExpression instrFunction = makeFunctionCall("_f_ocelot_trace", instrArgs);
		statement.setCondition(instrFunction);
	}
	
	public void visit(IASTForStatement statement) {
		IASTExpression[] instrArgs = new IASTExpression[3];
		instrArgs[0] = this.transformOriginalExpression(statement.getConditionExpression().copy());
		instrArgs[1] = this.transformDistanceExpression(this.cloneExpression(statement.getConditionExpression()), false, false);
		instrArgs[2] = this.transformDistanceExpression(this.cloneExpression(statement.getConditionExpression()), true, false);
		
		IASTFunctionCallExpression instrFunction = makeFunctionCall("_f_ocelot_trace", instrArgs);
		statement.setConditionExpression(instrFunction);
	}
	
	public void visit(IASTCaseStatement statement) {
		this.switchExpressions.lastElement().add(statement);
	}
	
	public void visit(IASTDefaultStatement statement) {
		this.switchExpressions.lastElement().add(statement);
	}
	
	public int visit(IASTStatement statement) {
		if (statement instanceof IASTIfStatement)
			this.visit((IASTIfStatement)statement);
		else if (statement instanceof IASTSwitchStatement) {
			this.visit((IASTSwitchStatement)statement);
			return PROCESS_SKIP; //Visits the statement on its own!
		} else if (statement instanceof IASTWhileStatement)
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
		
		IASTExpression instrumentedOp1 = this.transformDistanceExpression(op1, pNegation, false);
		IASTExpression instrumentedOp2 = this.transformDistanceExpression(op2, pNegation, false);
		
		IASTExpression[] operationArgs = new IASTExpression[2];
		operationArgs[0] = instrumentedOp1;
		operationArgs[1] = instrumentedOp2;
		
		IASTFunctionCallExpression operationFunction = makeFunctionCall("_f_ocelot_" + pOperator, operationArgs);
		
		return operationFunction;
	}
	
	private IASTExpression transformComparisonExpression(IASTBinaryExpression pExpression, String pOperator, int pRealOperator) {
		pExpression.setOperator(pRealOperator);
		IASTExpression operand1 = pExpression.getOperand1();
		IASTExpression operand2 = pExpression.getOperand2();
		
		IType op1Type = getType(operand1);
		IType op2Type = getType(operand2);
		
		IASTExpression[] operationArgs = new IASTExpression[2];
		operationArgs[0] = this.castToDouble(this.transformDistanceExpression(operand1, false, true));
		operationArgs[1] = this.castToDouble(this.transformDistanceExpression(operand2, false, true));
		
		IASTFunctionCallExpression operationFunction;
		if (op1Type instanceof IBasicType && op2Type instanceof IBasicType) {
			operationFunction = makeFunctionCall("_f_ocelot_" + pOperator+ "_numeric", operationArgs);
		} else if (op1Type instanceof ICPointerType && op2Type instanceof ICPointerType) {
			operationFunction = makeFunctionCall("_f_ocelot_" + pOperator+ "_pointer", operationArgs);
		} else {
			System.err.println("NOTE: assuming argument numeric. Fix for types: " + op1Type.getClass().getSimpleName() + "-" + op2Type.getClass().getSimpleName());
			operationFunction = makeFunctionCall("_f_ocelot_" + pOperator+ "_numeric", operationArgs);
		}
		
		return operationFunction;
	}
	
	private IASTExpression cloneExpression(IASTExpression pExpression) {
		IASTExpression copy = pExpression.copy();
		copy.setParent(pExpression.getParent());
		copy.setPropertyInParent(pExpression.getPropertyInParent());
		if (copy instanceof IASTBinaryExpression) {
			IASTBinaryExpression realCopy = (IASTBinaryExpression)copy;
			IASTBinaryExpression realOrig = (IASTBinaryExpression)pExpression;
			realCopy.setOperand1(realOrig.getOperand1());
			realCopy.setOperand2(realOrig.getOperand2());
			
			return realCopy;
		} else if (copy instanceof IASTUnaryExpression) {
			IASTUnaryExpression realCopy = (IASTUnaryExpression)copy;
			IASTUnaryExpression realOrig = (IASTUnaryExpression)pExpression;
			realCopy.setOperand(realOrig.getOperand());
			
			return realCopy;
		} else if (copy instanceof IASTIdExpression) {
			IASTIdExpression realCopy = (IASTIdExpression)copy;
			IASTIdExpression realOrig = (IASTIdExpression)pExpression;
			
			realCopy.getName().setBinding(realOrig.getName().getBinding());
		}
		
		
		return copy;
	}
	
	private IASTExpression castToDouble(IASTExpression pExpression) {
		//IASTCastExpression cast = new CASTCastExpression(new CASTTypeId(null, new CASTDeclarator(new CASTName("double".toCharArray()))), pExpression);
		
		return pExpression;
	}
	
	private IType getType(IASTExpression pExpression) {
		return getType(pExpression.getExpressionType());
	}
	
	private IType getType(IType type) {
		while (type instanceof CTypedef) {
			CTypedef tdef = (CTypedef)type;
			
			type = tdef.getType();
		}
		
		return type;
	}
	
	/*
	 * NOTE:
	 * Assuming that all the structures are compose by either pointers or not pointers.
	 * If a field is a pointer, it will be assumed that this is of a C basic type.
	 * TODO Make sure that also in case of non basic C types this method works properly
	 * For example, Ocelot won't work if there is a structure with a field with type pointer
	 * to another struct, because it won't initialize the parameters of the pointed structure
	 */
	@Override
	public int visit(IASTDeclaration declaration) {
		if (declaration instanceof CASTFunctionDefinition) {
			CASTFunctionDefinition function = (CASTFunctionDefinition)declaration;
			CASTFunctionDeclarator declarator = (CASTFunctionDeclarator)function.getDeclarator();
			
			String[] callParameters = new String[declarator.getParameters().length];
			String macro = "";
			macro += "#define EXECUTE_OCELOT_TEST ";
			
			IFunction functionType = null;
			try {
				functionType = (IFunction)((CASTFunctionDefinition) declaration).getScope().getParent().getBinding(declarator.getName(), true);
			} catch (Exception e) {
				System.out.println(e);
			}
			
			int outputArgument = 0;
			int inputArgument = 0;
			
			IParameter[] parameters = functionType.getParameters();
			for (int i = 0; i < declarator.getParameters().length; i++) {
				IASTParameterDeclaration param = declarator.getParameters()[i];
				String typeString = param.getDeclSpecifier().getRawSignature();
				IType type = getType(parameters[i].getType());
				
				int pointers = param.getDeclarator().getRawSignature().length() - param.getDeclarator().getRawSignature().replaceAll("\\*", "").length();
				
				if (type instanceof CStructure) {
					macro += typeString; //Type
					macro += " __arg"+inputArgument+";\\\n";
					VarStructTree tree = new VarStructTree("__arg"+outputArgument, (CStructure)type);
					List<StructNode> basics = tree.getBasicVariables();
					for (StructNode var : basics) {
						String fieldType = var.type.toString().replaceAll("\\*", "");
						macro += fieldType;
						macro += " __str"+inputArgument;
						macro += " = (" + fieldType +")OCELOT_numeric(OCELOT_ARG(" + inputArgument + "));\\\n"; //Assign
						
						macro += var.getCompleteName() + " = " + (var.isPointer() ? "&" : "") + "__str" + inputArgument + ";\\\n";
						
						inputArgument++;
					}
					
				} else {
					macro += typeString;//Type
					macro += " __arg" +outputArgument; //Name
					macro += " = OCELOT_numeric(OCELOT_ARG(" + inputArgument + "));\\\n"; //Assign
					
					inputArgument++;
				}
				
				callParameters[outputArgument] = StringUtils.repeat('&', pointers) + "__arg" + outputArgument;
				outputArgument++;
			}
			macro += "OCELOT_TESTFUNCTION (" +StringUtils.join(callParameters, ",") + ");";
			
			this.callMacro = macro;
		}
		return super.visit(declaration);
	}
	
	public String getCallMacro() {
		return callMacro;
	}
}
