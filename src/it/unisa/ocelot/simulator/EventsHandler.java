package it.unisa.ocelot.simulator;

import java.util.ArrayList;
import java.util.List;

public class EventsHandler {
	private List<ExecutionEvent> events;
	
	public EventsHandler() {
		this.events = new ArrayList<ExecutionEvent>();
	}
	
	public void add(int pChoice, double pDistanceTrue, double pDistanceFalse) {
		this.events.add(new ExecutionEvent(pChoice, pDistanceTrue, pDistanceFalse));
	}
	
	@Override
	public String toString() {
		return this.events.toString();
	}
}
