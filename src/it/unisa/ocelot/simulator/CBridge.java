package it.unisa.ocelot.simulator;

public class CBridge {
	public native synchronized void getEvents(EventsHandler pHandler, Object[] arguments);
}
