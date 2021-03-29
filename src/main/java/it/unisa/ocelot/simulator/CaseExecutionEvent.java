package it.unisa.ocelot.simulator;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;

public class CaseExecutionEvent extends ExecutionEvent {
	public boolean chosen;
	
	public CaseExecutionEvent(int choice, double distanceTrue, boolean chosen) {
		this(null, choice, distanceTrue, chosen);
	}
	
	public CaseExecutionEvent(LabeledEdge pEdge, int choice, double distanceTrue, boolean chosen) {
		super(pEdge, choice, distanceTrue, 0.0D);
		
		this.chosen = chosen;
	}
	
	public boolean isChosen() {
		return chosen;
	}
	
	@Override
	public String toString() {
		String res = "{\n" + 
				"\tBranchID: " + this.choice + "\n" +
				"\tDistanceTrue: " + this.distanceTrue + "\n" +
				"\tChosen: " + this.chosen +
				"\n}";
		return res;
	}
}
