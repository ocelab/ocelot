package test.simulator;

import it.unisa.ocelot.simulator.CaseExecutionEvent;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.ExecutionEvent;
import org.eclipse.cdt.utils.coff.Exe;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test case for the class it.unisa.ocelot.simulator.EventsHandler
 * @author Giovanni Grano
 */
public class EventsHandlerTest {

    private EventsHandler eventsHandler;

    @Before
    public void setUp() {
        eventsHandler = new EventsHandler();
    }

    @Test
    public void add() throws Exception {
        int choice = 0;
        double distanceTrue = 0.7571;
        double distanceFalse = 0;
        eventsHandler.add(choice, distanceTrue, distanceFalse);
        ExecutionEvent events = eventsHandler.getEvents().get(0);
        assertEquals(events.choice, 0);
        assertEquals(events.distanceTrue, 0.7571, 0.0);
        assertEquals(events.distanceFalse, 0.0, 0.0);
    }

    @Test
    public void addCase() throws Exception {
        int choice = 0;
        double distance = 0.7571;
        boolean isChosen = false;
        eventsHandler.addCase(choice, distance, isChosen);
        CaseExecutionEvent event = (CaseExecutionEvent) eventsHandler.getEvents().get(0);
        assertEquals(event.choice, 0);
        assertEquals(event.distanceTrue, 0.7571, 0.0);
        assertEquals(event.chosen, false);
    }

    @Test
    public void getEvents() throws Exception {
        eventsHandler.add(0, 0.12, 0);
        eventsHandler.addCase(0, 0.2323, false);
        assertEquals(eventsHandler.getEvents().size(), 2);
    }
}