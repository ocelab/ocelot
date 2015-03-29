package it.unisa.ocelot.c.cfg;

import it.unisa.ocelot.simulator.ExecutionEvent;


/**
 * This class represents an edge with a label. The label could an object of any kind.
 * @author simone
 *
 */
public class FlowEdge extends LabeledEdge {
	private static final long serialVersionUID = -6097816767281519267L;
	
	public FlowEdge() {
		super("");
	}

	@Override
	public boolean matchesExecution(ExecutionEvent pEvent) {
		return true;
	}

	@Override
	public boolean needsEvent() {
		return false;
	}
	
	@Override
	public String toString() {
		return "FlowEdge";
	}
}
