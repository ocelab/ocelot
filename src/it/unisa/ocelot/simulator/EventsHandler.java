package it.unisa.ocelot.simulator;

import it.unisa.ocelot.c.cfg.LabeledEdge;

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
	
	public void addCase(int pChoice, double pDistance, boolean isChosen) {
		this.events.add(new CaseExecutionEvent(pChoice, pDistance, isChosen));
	}
	
	public List<ExecutionEvent> getEvents() {
		return events;
	}
	
	@Override
	public String toString() {
		return this.events.toString();
	}
}
