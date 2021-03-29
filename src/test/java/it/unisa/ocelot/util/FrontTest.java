package it.unisa.ocelot.util;

import jmetal.core.SolutionSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FrontTest {

    private Front front;

    @Before
    public void setUp() throws Exception {
        front = new Front();
    }

    @Test
    public void getFront() throws Exception {
        SolutionSet solution = new SolutionSet(5);
        front.addFront(solution);
        assertEquals(5, front.getFront(0).getCapacity());
    }

    @Test
    public void addFront() throws Exception {
        SolutionSet solution = new SolutionSet();
        front.addFront(solution);
        assertNotNull(front.getFront(0));
    }

}