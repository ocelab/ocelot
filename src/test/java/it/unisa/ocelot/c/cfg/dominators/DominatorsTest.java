package it.unisa.ocelot.c.cfg.dominators;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.FalseEdge;
import it.unisa.ocelot.c.cfg.edges.FlowEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.edges.TrueEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Simone Scalabrino.
 */
public class DominatorsTest {
    private CFG cfg;
    private CFGNode start;
    private CFGNode n1;
    private CFGNode n2;
    private CFGNode n3;
    private CFGNode n4;
    private CFGNode n5;
    private CFGNode end;

    @Before
    public void before() {
        start = new CFGNode("start");
        n1 = new CFGNode("if edge");
        n2 = new CFGNode("expression node#1");
        n3 = new CFGNode("expression node#2");
        n4 = new CFGNode("goto 5");
        n5 = new CFGNode("expression node#3");
        end = new CFGNode("end");

        cfg = new CFG();
        cfg.addVertex(start);
        cfg.addVertex(n1);
        cfg.addVertex(n2);
        cfg.addVertex(n3);
        cfg.addVertex(n4);
        cfg.addVertex(n5);
        cfg.addVertex(end);

        cfg.setStart(start);
        cfg.setEnd(end);

        cfg.addEdge(start, n1, new FlowEdge());
        cfg.addEdge(n1, n2, new TrueEdge());
        cfg.addEdge(n1, n3, new FalseEdge());
        cfg.addEdge(n2, n4, new FlowEdge());
        cfg.addEdge(n3, n5, new FlowEdge());
        cfg.addEdge(n4, n5, new FlowEdge());
        cfg.addEdge(n5, end, new FlowEdge());
    }

    @Test
    public void dominates() throws Exception {
        Dominators<CFGNode, LabeledEdge> dominators = new Dominators<>(cfg, cfg.getStart());

        assertTrue(dominators.dominates(start, n1));
        assertTrue(dominators.dominates(n1, n2));
        assertTrue(dominators.dominates(n1, n3));
        assertTrue(dominators.dominates(n1, n4));
        assertTrue(dominators.dominates(n1, n5));
        assertTrue(dominators.dominates(n1, end));
        assertFalse(dominators.dominates(n1, start));

        assertTrue(dominators.dominates(n2, n4));
        assertFalse(dominators.dominates(n2, end));
        assertFalse(dominators.dominates(n2, n5));
        assertFalse(dominators.dominates(n2, start));
        assertFalse(dominators.dominates(n2, n1));
        assertFalse(dominators.dominates(n2, n3));

        assertFalse(dominators.dominates(n3, end));
        assertFalse(dominators.dominates(n3, n5));
        assertFalse(dominators.dominates(n3, n1));
        assertFalse(dominators.dominates(n3, n2));
        assertFalse(dominators.dominates(n3, n4));
    }

    @Test
    public void getStrictDominators() throws Exception {
        Dominators<CFGNode, LabeledEdge> dominators = new Dominators<>(cfg, cfg.getStart());

        Set<CFGNode> dominatorsEnd = dominators.getStrictDominators(end);
        Set<CFGNode> oracleDominatorsEnd = new HashSet<>(Arrays.asList(n5, end, start, n1));
        assertEquals(oracleDominatorsEnd, dominatorsEnd);

        Set<CFGNode> dominatorsN5 = dominators.getStrictDominators(n5);
        Set<CFGNode> oracleDominatorsN5 = new HashSet<>(Arrays.asList(n5, start, n1));
        assertEquals(oracleDominatorsN5, dominatorsN5);
    }

    @Test
    public void getNonDominators() throws Exception {
        Dominators<CFGNode, LabeledEdge> dominators = new Dominators<>(cfg, cfg.getStart());

        assertTrue(dominators.getNonDominators().contains(n3));
        assertTrue(dominators.getNonDominators().contains(n4));
        assertTrue(dominators.getNonDominators().contains(end));
    }

    @Test
    public void autoTest() throws Exception {
        Dominators<CFGNode, LabeledEdge> dominators = new Dominators<>(cfg, cfg.getStart());

        for (CFGNode cfgNode : cfg.vertexSet()) {
            if (dominators.getNonDominators().contains(cfgNode)) {
                for (CFGNode testNode : cfg.vertexSet()) {
                    if (testNode != cfgNode)
                        assertFalse(dominators.dominates(cfgNode, testNode));
                    else
                        assertTrue(dominators.dominates(cfgNode, testNode));
                }
            }
        }
    }
}