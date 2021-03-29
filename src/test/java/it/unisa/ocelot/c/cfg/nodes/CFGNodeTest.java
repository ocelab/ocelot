package it.unisa.ocelot.c.cfg.nodes;

import it.unisa.ocelot.c.cfg.ASTScaffolding;
import org.eclipse.cdt.internal.core.dom.parser.c.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Simone Scalabrino.
 */
public class CFGNodeTest {
    @Test
    public void testIfStatement() {
        CASTIfStatement astNode = ASTScaffolding.Statement.ifStatement(
                ASTScaffolding.Expression.Comparison.equals(ASTScaffolding.id("test"), ASTScaffolding.id("test2")),
                ASTScaffolding.Statement.functionCall("f")
        );

        CFGNode cfgNode = new CFGNode(astNode);
        CFGNode cfgNode2 = new CFGNode(astNode);

        assertFalse(cfgNode.isBreak());
        assertFalse(cfgNode.isCase());
        assertFalse(cfgNode.isContinue());
        assertFalse(cfgNode.isGoto());
        assertFalse(cfgNode.isSwitch());
        assertFalse(cfgNode.isVisited());
        cfgNode.setVisited(true);
        assertTrue(cfgNode.isVisited());

        assertEquals(astNode, cfgNode.getLeadingNode());

        assertFalse(cfgNode == cfgNode2);
        assertEquals(cfgNode, cfgNode2);
    }

    @Test
    public void testSwitchStatement() {
        CASTSwitchStatement switchWithoutDefault = ASTScaffolding.Statement.switchStatement(
                ASTScaffolding.Expression.functionCall("test"),
                new String[] {"10", "20"},
                ASTScaffolding.Statement.functionCall("a1"),
                ASTScaffolding.Statement.functionCall("a2")
        );

        CASTSwitchStatement switchWithDefault = ASTScaffolding.Statement.switchStatement(
                ASTScaffolding.Expression.functionCall("test"),
                new String[] {"10", "20"},
                ASTScaffolding.Statement.functionCall("a1"),
                ASTScaffolding.Statement.functionCall("a2"),
                ASTScaffolding.Statement.functionCall("a3")
        );

        CFGNode cfgNodeWithDefault = new CFGNode(switchWithDefault);
        cfgNodeWithDefault.setSwitch(true);
        CFGNode cfgNodeWithoutDefault = new CFGNode(switchWithoutDefault);
        cfgNodeWithoutDefault.setSwitch(true);

        assertFalse(cfgNodeWithDefault.isBreak());
        assertFalse(cfgNodeWithDefault.isCase());
        assertFalse(cfgNodeWithDefault.isContinue());
        assertFalse(cfgNodeWithDefault.isGoto());
        assertTrue(cfgNodeWithDefault.isSwitch());
        assertFalse(cfgNodeWithDefault.isVisited());

        assertFalse(cfgNodeWithoutDefault.isBreak());
        assertFalse(cfgNodeWithoutDefault.isCase());
        assertFalse(cfgNodeWithoutDefault.isContinue());
        assertFalse(cfgNodeWithoutDefault.isGoto());
        assertTrue(cfgNodeWithoutDefault.isSwitch());
        assertFalse(cfgNodeWithoutDefault.isVisited());

        assertNotEquals(cfgNodeWithDefault, cfgNodeWithoutDefault);
    }
}