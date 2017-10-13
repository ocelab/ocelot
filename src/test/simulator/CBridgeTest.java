package test.simulator;

import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;

/**
 * Test case for the it.unisa.ocelot.simulator.CBridge class
 * @author Giovanni Granog
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CBridge.class)
public class CBridgeTest {

    private int values;
    private int arrays;
    private int points;

    private CBridge cBridge;

    @Before
    public void setUp() {
        this.values     = 2;
        this.arrays     = 0;
        this.points     = 0;
        this.cBridge    = new CBridge(0);
    }

    @Test
    public void testInitialize() throws Exception {
        PowerMockito.spy(CBridge.class);
        boolean initialized = Whitebox.getInternalState(CBridge.class, "initialized");
        assertEquals(initialized, false);
        PowerMockito.doNothing().when(CBridge.class, "privInit", values, arrays, points);
        CBridge.initialize(values, arrays, points);
        initialized = Whitebox.getInternalState(CBridge.class, "initialized");
        assertEquals(initialized, true);
    }

    @Test
    public void testGetEvents() throws Exception {
        EventsHandler eventsHandler = new EventsHandler();
        Object[] pValues    = {0.34234, 0.3424, 0.2352342, 0.42342};
        Object[][] pArrays  = {
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0}
        };
        Object[] pPointers  = {1.0, 1.0, 1.0};

        CBridge c = PowerMockito.mock(CBridge.class);
        // Check initialization
        Whitebox.setInternalState(CBridge.class, "initialized", true);
        c.getEvents(eventsHandler, pValues, pArrays, pPointers);

        double[] values = new double[pValues.length];
        for (int i = 0; i < pValues.length; i++)
            values[i] = ((Number)(pValues[i])).doubleValue();
        assertEquals(values[1], 0.3424, 0.0);
    }

    @Test(expected = RuntimeException.class)
    public void testGetEventNoInitialization() throws Exception {
        PowerMockito.mockStatic(CBridge.class);
        this.cBridge.getEvents(null, (Object[]) null, null, null);
    }
}