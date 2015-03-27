package it.unisa.ocelot.c.cfg;

import it.unisa.ocelot.simulator.ExecutionEvent;

/**
 * This class represents an edge with a label. The label could an object of any kind.
 * @author simone
 *
 */
public class TrueEdge extends LabeledEdge {
	private static final long serialVersionUID = 5916153862577936206L;
	
	public TrueEdge() {
		super(true);
	}
	
	@Override
	public boolean matchesExecution(ExecutionEvent pEvent) {
		return pEvent.choice == 1;
	}

	@Override
	public boolean needsEvent() {
		return true;
	}
}
