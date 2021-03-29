package it.unisa.ocelot.c.cfg;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Simone Scalabrino.
 */
public class McCabeCalculatorTest {
    @Test
    public void testMcCabe() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File testFile = new File(classLoader.getResource("testProject1/test1.c").getFile());

        CFG cfg = CFGBuilder.build(testFile.getCanonicalPath(), "testFunc");

        McCabeCalculator calculator = new McCabeCalculator(cfg);
        calculator.calculateMcCabePaths();

        ArrayList<ArrayList<LabeledEdge>> mcCabeEdgePaths = calculator.getMcCabeEdgePaths();
        List<String> stringed = new ArrayList<>();

        assertEquals(3, mcCabeEdgePaths.size());

        stringed.add(mcCabeEdgePaths.get(0).toString());
        stringed.add(mcCabeEdgePaths.get(1).toString());
        stringed.add(mcCabeEdgePaths.get(2).toString());

        assertTrue(stringed.contains("[FlowEdge, true, false, FlowEdge, FlowEdge]"));
        assertTrue(stringed.contains("[FlowEdge, true, true, FlowEdge, FlowEdge, FlowEdge]"));
        assertTrue(stringed.contains("[FlowEdge, false, FlowEdge]"));
    }
}