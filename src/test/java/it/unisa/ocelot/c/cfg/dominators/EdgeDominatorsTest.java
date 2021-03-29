package it.unisa.ocelot.c.cfg.dominators;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.FalseEdge;
import it.unisa.ocelot.c.cfg.edges.FlowEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.edges.TrueEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Simone Scalabrino.
 */
public class EdgeDominatorsTest {
    private CFG cfg;
    private CFGNode start;
    private CFGNode n1;
    private CFGNode n2;
    private CFGNode n3;
    private CFGNode n4;
    private CFGNode n5;
    private CFGNode end;

    private LabeledEdge start_n1;
    private LabeledEdge n1_n2;
    private LabeledEdge n1_n3;
    private LabeledEdge n2_n4;
    private LabeledEdge n3_n5;
    private LabeledEdge n4_n5;
    private LabeledEdge n5_end;

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

        start_n1 = cfg.getEdge(start, n1);
        n1_n2 = cfg.getEdge(n1, n2);
        n1_n3 = cfg.getEdge(n1, n3);
        n2_n4 = cfg.getEdge(n2, n4);
        n3_n5 = cfg.getEdge(n3, n5);
        n4_n5 = cfg.getEdge(n4, n5);
        n5_end = cfg.getEdge(n5, end);
    }

    @Test
    public void dominates() throws Exception {
        EdgeDominators<CFGNode, LabeledEdge> dominators = new EdgeDominators<>(cfg, cfg.getStart(), cfg.getEnd());

        assertTrue(dominators.dominates(start_n1, n1_n2));
        assertTrue(dominators.dominates(start_n1, n2_n4));
        assertTrue(dominators.dominates(start_n1, n4_n5));
        assertTrue(dominators.dominates(start_n1, n1_n3));
        assertTrue(dominators.dominates(start_n1, n3_n5));
        assertTrue(dominators.dominates(start_n1, n5_end));

        assertTrue(dominators.dominates(n1_n2, n2_n4));
        assertTrue(dominators.dominates(n1_n2, n4_n5));
        assertFalse(dominators.dominates(n1_n2, n5_end));

        assertTrue(dominators.dominates(n1_n3, n3_n5));
        assertFalse(dominators.dominates(n3_n5, n5_end));
    }

    @Test
    public void getStrictDominators() throws Exception {
        EdgeDominators<CFGNode, LabeledEdge> dominators = new EdgeDominators<>(cfg, cfg.getStart(), cfg.getEnd());

        Set<LabeledEdge> dominatorsEnd = dominators.getStrictDominators(n5_end);
        Set<LabeledEdge> oracleDominatorsEnd = new HashSet<>(Arrays.asList(start_n1, n5_end));
        assertEquals(oracleDominatorsEnd, dominatorsEnd);

        Set<LabeledEdge> dominatorsN5 = dominators.getStrictDominators(n3_n5);
        Set<LabeledEdge> oracleDominatorsN5 = new HashSet<>(Arrays.asList(start_n1, n1_n3, n3_n5));
        assertEquals(oracleDominatorsN5, dominatorsN5);
    }

    @Test
    public void getNonDominators() throws Exception {
        EdgeDominators<CFGNode, LabeledEdge> dominators = new EdgeDominators<>(cfg, cfg.getStart(), cfg.getEnd());

        assertTrue(dominators.getNonDominators().contains(n3_n5));
        assertTrue(dominators.getNonDominators().contains(n4_n5));
        assertTrue(dominators.getNonDominators().contains(n5_end));
    }

    @Test
    public void autoTest() throws Exception {
        EdgeDominators<CFGNode, LabeledEdge> dominators = new EdgeDominators<>(cfg, cfg.getStart(), cfg.getEnd());

        for (LabeledEdge labeledEdge : cfg.edgeSet()) {
            if (dominators.getNonDominators().contains(labeledEdge)) {
                for (LabeledEdge testEdge : cfg.edgeSet()) {
                    if (testEdge != labeledEdge)
                        assertFalse(dominators.dominates(labeledEdge, testEdge));
                    else
                        assertTrue(dominators.dominates(labeledEdge, testEdge));
                }
            }
        }
    }
}