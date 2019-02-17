package it.unisa.ocelot.simulator;

import it.unisa.ocelot.genetic.encoding.graph.Graph;

public class CBridge {
	private int coreId;
	
	public CBridge() {
		this.coreId = 0;
	}
	
	/**
	 * Creates a new CBridge on the specified core process ID.
	 * @param pCoreID
	 */
	public CBridge(int pCoreID) {
		this();
		this.coreId = pCoreID;
	}

	/**
	 * Executes the test function in order to populate the given the event handler
	 * @param pHandler Event handler, it will contain the result of the test (each non-trivial choice)
	 * @param graph Function Graph
	 */
	public void getEvents(EventsHandler pHandler, Graph graph) {
		this.getEvent(pHandler, graph);
	}

	public native void getEvent(EventsHandler pHandler, Graph graph);
}
