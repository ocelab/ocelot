package it.unisa.ocelot.simulator;

import java.util.List;
import java.util.Set;

import it.unisa.ocelot.cfg.CFG;
import it.unisa.ocelot.cfg.CFGNode;
import it.unisa.ocelot.cfg.LabeledEdge;

/**
 * Simulates the execution of the program on the CFG in order to calculate the distance metrics and, 
 * so, the fitness function.
 * @author simone
 *
 */
public class Simulator {
	private CFG cfg;
	private List<ExecutionEvent> events;
	private SimulatorListener listener;
	
	public Simulator(CFG pCFG, List<ExecutionEvent> pEvents) {
		this.cfg = pCFG;
		this.events = pEvents;
		if (this.events.size() == 0)
			this.events.add(null); //It is useless, but it prevents ArrayIndexOutOfBound in "simulate"
	}
	
	public void setListener(SimulatorListener pListener) {
		this.listener = pListener;
	}
	
	public void simulate() {
		CFGNode currentNode = cfg.getStart();
		ExecutionEvent currentEvent = this.events.get(0);
		int currentEventIndex = 0;
		while (!currentNode.equals(cfg.getEnd())) {
			this.listener.onNodeVisit(currentNode);
			Set<LabeledEdge> edges = cfg.outgoingEdgesOf(currentNode);
			
			for (LabeledEdge edge : edges) {
				if (!edge.needsEvent()) {
					this.listener.onEdgeVisit(edge, null);
					currentNode = this.cfg.getEdgeTarget(edge);
					currentEvent = this.events.get(currentEventIndex++);
				} else {
					if (edge.matchesExecution(currentEvent)) {
						this.listener.onEdgeVisit(edge, currentEvent);
						currentNode = this.cfg.getEdgeTarget(edge);
						currentEvent = this.events.get(currentEventIndex++);
					}
				}
			}
		}
	}
}
