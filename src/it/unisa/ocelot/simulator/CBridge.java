package it.unisa.ocelot.simulator;

public class CBridge {
	/**
	 * Executes the test function in order to populate the given the event handler
	 * @param pHandler Event handler, it will contain the result of the test (each non-trivial choice)
	 * @param arguments Parameters of the function
	 */
	public native synchronized void getEvents(EventsHandler pHandler, Object[] arguments);
}
