package it.unisa.ocelot.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;

/**
 * Simulates the execution of the program on the CFG in order to calculate the distance metrics and, 
 * so, the fitness function.
 * @author simone
 *
 */
public class Simulator {
	private CFG cfg;
	private List<ExecutionEvent> events;
	private List<SimulatorListener> listeners;
	private int currentEventIndex;
	
	public Simulator(CFG pCFG, List<ExecutionEvent> pEvents) {
		this.cfg = pCFG;
		this.events = pEvents;
		if (this.events.size() == 0)
			this.events.add(null); //It is useless, but it prevents ArrayIndexOutOfBound in "simulate"
		this.listeners = new ArrayList<SimulatorListener>();
	}
	
	public void setListener(SimulatorListener pListener) {
		this.listeners.clear();
		this.listeners.add(pListener);
	}
	
	public void addListener(SimulatorListener pListener) {
		this.listeners.add(pListener);
	}
	
	public void simulate() {
		this.reset();
		ExecutionEvent currentEvent = this.getNextEvent();
		
		CFGNode currentNode = cfg.getStart();
		
		while (!currentNode.strongEquals(cfg.getEnd())) {
			for (SimulatorListener listener : this.listeners)
				listener.onNodeVisit(currentNode);
			Set<LabeledEdge> edges = cfg.outgoingEdgesOf(currentNode);
			
			List<ExecutionEvent> caseEvents = null;
			if (currentNode.isSwitch()) {
				currentEvent = null;
				caseEvents = new ArrayList<ExecutionEvent>();
				ExecutionEvent currentCaseEvent;
				
				this.rewind(1);
				for (int i = 0; i < edges.size(); i++) {
					currentCaseEvent = this.getNextEvent();
					
					caseEvents.add(currentCaseEvent);
					if (((CaseExecutionEvent)currentCaseEvent).isChosen())
						currentEvent = currentCaseEvent;
					
				}
				
			}
			
			boolean newEvent = false;
			for (LabeledEdge edge : edges) {
				if (!edge.needsEvent()) {
					for (SimulatorListener listener : this.listeners)
						listener.onEdgeVisit(edge);
					currentNode = this.cfg.getEdgeTarget(edge);
					//currentEvent = this.getNextEvent();
				} else {
					if (edge.matchesExecution(currentEvent)) {
						
						if (currentNode.isSwitch())
							for (SimulatorListener listener : this.listeners)
								listener.onEdgeVisit(edge, currentEvent, caseEvents);
						else
							for (SimulatorListener listener : this.listeners)
								listener.onEdgeVisit(edge, currentEvent);
						currentNode = this.cfg.getEdgeTarget(edge);
						newEvent = true;
					}
				}
			}
			
			if (newEvent)
				currentEvent = this.getNextEvent();
		}
	}
	
	public boolean isSimulationCorrect() {
		return this.currentEventIndex == this.events.size();
	}
	
	private void reset() {
		this.currentEventIndex = -1;
	}
	
	private ExecutionEvent getNextEvent() {
		this.currentEventIndex++;
		if (this.currentEventIndex < this.events.size())
			return this.events.get(this.currentEventIndex);
		else
			return null;
	}
	
	private void rewind(int number) {
		this.currentEventIndex -= number;
	}
}
