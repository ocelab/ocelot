package it.unisa.ocelot.c.cfg;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.c.*;

/**
 * @author Simone Scalabrino.
 */
public class ASTScaffolding {
    public static CASTIdExpression id(String name) {
        return new CASTIdExpression(new CASTName(name.toCharArray()));
    }

    public static class Expression {
        public static class Comparison {
            public static CASTBinaryExpression equals(IASTExpression a, IASTExpression b) {
                return new CASTBinaryExpression(IASTBinaryExpression.op_equals, a, b);
            }

            public static CASTBinaryExpression greaterThan(IASTExpression a, IASTExpression b) {
                return new CASTBinaryExpression(IASTBinaryExpression.op_greaterThan, a, b);
            }

            public static CASTBinaryExpression lessThan(IASTExpression a, IASTExpression b) {
                return new CASTBinaryExpression(IASTBinaryExpression.op_lessThan, a, b);
            }
        }

        public static IASTExpression functionCall(String name, IASTInitializerClause... parameters) {
            return new CASTFunctionCallExpression(id(name), parameters);
        }
    }

    public static class Statement {
        public static CASTIfStatement ifStatement(IASTExpression condition, IASTStatement trueBranch) {
            return new CASTIfStatement(condition, trueBranch);
        }

        public static CASTSwitchStatement switchStatement(IASTExpression condition, String[] labels, IASTStatement... statements) {
            assert statements.length == labels.length || statements.length == labels.length + 1;

            CASTSwitchStatement switchStatement = new CASTSwitchStatement();
            switchStatement.setControllerExpression(condition);
            CASTCompoundStatement body = new CASTCompoundStatement();

            for (int i = 0; i < labels.length; i++) {
                body.addStatement(new CASTCaseStatement(ASTScaffolding.id(labels[i])));
                body.addStatement(statements[i]);
            }

            if (statements.length > labels.length) {
                body.addStatement(new CASTDefaultStatement());
                body.addStatement(statements[statements.length-1]);
            }

            switchStatement.setBody(body);
            return switchStatement;
        }

        public static CASTIfStatement ifStatement(IASTExpression condition, IASTStatement trueBranch, IASTStatement falseBranch) {
            return new CASTIfStatement(condition, trueBranch, falseBranch);
        }

        public static IASTStatement functionCall(String name, IASTInitializerClause... parameters) {
            return new CASTExpressionStatement(ASTScaffolding.Expression.functionCall(name, parameters));
        }
    }
}
