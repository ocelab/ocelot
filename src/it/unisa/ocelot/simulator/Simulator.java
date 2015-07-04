package it.unisa.ocelot.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CaseEdge;
import it.unisa.ocelot.c.cfg.LabeledEdge;

/**
 * Simulates the execution of the program on the CFG in order to calculate the
 * distance metrics and, so, the fitness function.
 * 
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
		//It is useless, but it prevents ArrayIndexOutOfBound in "simulate" when there is no event.
		if (this.events.size() == 0)
			this.events.add(null); //It is useless, but it prevents ArrayIndexOutOfBound in "simulate"
		this.listeners = new ArrayList<SimulatorListener>();
	}
	
	/**
	 * Set a single listener, removing the previous ones.
	 * @param pListener
	 */
	public void setListener(SimulatorListener pListener) {
		this.listeners.clear();
		this.listeners.add(pListener);
	}
	
	/**
	 * Add a listener to the simulator
	 * @param pListener
	 */
	public void addListener(SimulatorListener pListener) {
		this.listeners.add(pListener);
	}
	
	/**
	 * Perform the simulation
	 */
	public void simulate() {
		this.reset();
		//Consumes the first event
		ExecutionEvent currentEvent = this.getNextEvent();
		
		//Takes the first node
		CFGNode currentNode = cfg.getStart();
		
		//Loop until we don't reach the end of the execution
		while (!currentNode.strongEquals(cfg.getEnd())) {
			//Notifies the visit of the node to each listener
			for (SimulatorListener listener : this.listeners)
				listener.onNodeVisit(currentNode);
			Set<LabeledEdge> edges = cfg.outgoingEdgesOf(currentNode);
			
			//If we have a switch, we get all the execution events related to this switch
			//(one for each edge) and we set currentEvent as the event related to the chosen branch.
			List<ExecutionEvent> caseEvents = null;
			if (currentNode.isSwitch()) {
				currentEvent = null;
				caseEvents = new ArrayList<ExecutionEvent>();
				ExecutionEvent currentCaseEvent;
				
				//Goes back of one event
				this.rewind(1);
				for (LabeledEdge edge : edges) {
					currentCaseEvent = this.getNextEvent();
					
					caseEvents.add(currentCaseEvent);
					currentCaseEvent.setEdge(edge);
					if (((CaseExecutionEvent) currentCaseEvent).isChosen()) {
						currentEvent = currentCaseEvent;
						currentEvent.setEdge(edge);
					}
					
				} //for each edge
			} //if is a switch
			
			//New event not needed
			boolean newEvent = false;
			
			//For each outgoing edge from the current node
			for (LabeledEdge edge : edges) {
				
				//If the edge is a FlowEdge, notifies to the listeners and changes the current node
				if (!edge.needsEvent()) {
					for (SimulatorListener listener : this.listeners)
						listener.onEdgeVisit(edge);
					currentNode = this.cfg.getEdgeTarget(edge);
					
				} else {
					//Take the edge matching the execution event
					if (edge.matchesExecution(currentEvent)) {
						currentEvent.setEdge(edge);
						
						//Notifies the visit of the edge to the listeners.
						if (currentNode.isSwitch())
							for (SimulatorListener listener : this.listeners)
								listener.onEdgeVisit(edge, currentEvent, caseEvents);
						else
							for (SimulatorListener listener : this.listeners)
								listener.onEdgeVisit(edge, currentEvent);
						
						//Changes the current node
						currentNode = this.cfg.getEdgeTarget(edge);
						
						//Need a new event!
						newEvent = true;
					} //if matches the execution event
				} //if doesn't need events
			} //while end not reached
			
			//If needs a new event, consumes a new event.
			if (newEvent)
				currentEvent = this.getNextEvent();
		}
	}
	
	/**
	 * Checks if the simulation performed is correct.
	 * @return true if the simulation is correct, false otherwise
	 */
	public boolean isSimulationCorrect() {
		return this.currentEventIndex == this.events.size();
	}
	
	/**
	 * Resets the simulation
	 */
	private void reset() {
		this.currentEventIndex = -1;
	}
	
	/**
	 * Consumes an execution event
	 * @return
	 */
	private ExecutionEvent getNextEvent() {
		this.currentEventIndex++;
		if (this.currentEventIndex < this.events.size())
			return this.events.get(this.currentEventIndex);
		else
			return null;
	}
	
	/**
	 * Goes back of "number" execution events
	 * @param number Number of execution events to go back
	 */
	private void rewind(int number) {
		this.currentEventIndex -= number;
	}
}
