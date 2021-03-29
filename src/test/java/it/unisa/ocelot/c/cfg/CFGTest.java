package it.unisa.ocelot.c.cfg;

import it.unisa.ocelot.util.Utils;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Simone Scalabrino.
 */
public class CFGTest {
    @Test
    public void testBuilding() throws IOException, CoreException {
        ClassLoader classLoader = getClass().getClassLoader();
        File testFile = new File(classLoader.getResource("testProject1/test1.c").getFile());

        CFG cfg = CFGBuilder.build(testFile.getCanonicalPath(), "testFunc");

        assertNotNull(cfg.getStart());
        assertNotNull(cfg.getEnd());
        assertEquals(Arrays.asList(1000.0, 1000.0), cfg.getConstantNumbers());

        assertTrue(cfg.getStart().navigate(cfg).goFlow().node().getNodes().get(0) instanceof CASTBinaryExpression);
        assertTrue(cfg.getStart().navigate(cfg).goFlow().goTrue().node().getNodes().get(0) instanceof CASTBinaryExpression);
        assertEquals(cfg.getStart().navigate(cfg).goFlow().goFalse().node().getNodes().get(0), cfg.getStart().navigate(cfg).goFlow().goTrue().goFlow().goFlow().node().getNodes().get(0));
        assertEquals(cfg.getStart().navigate(cfg).goFlow().goFalse().goFlow().node(), cfg.getEnd());

        //TODO assert also on cfg.getParameterTypes(), but only when the new instrumentation is ready
        assertNotNull(cfg);
    }
}