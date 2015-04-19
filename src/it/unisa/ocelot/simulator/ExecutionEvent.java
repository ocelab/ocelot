package it.unisa.ocelot.simulator;

import it.unisa.ocelot.c.cfg.LabeledEdge;

public class ExecutionEvent {
	public int choice;
	public double distanceTrue;
	public double distanceFalse;
	private LabeledEdge edge;
	
	public ExecutionEvent(int choice, double distanceTrue, double distanceFalse) {
		this(null, choice, distanceTrue, distanceFalse);
	}
	
	public ExecutionEvent(LabeledEdge pEdge, int choice, double distanceTrue, double distanceFalse) {
		this.choice = choice;
		this.distanceFalse = distanceFalse;
		this.distanceTrue = distanceTrue;
		this.edge = pEdge;
	}
	
	public void setEdge(LabeledEdge edge) {
		this.edge = edge;
	}
	
	public LabeledEdge getEdge() {
		return edge;
	}
	
	@Override
	public String toString() {
		String res = "{\n" + 
				"Choice: " + this.choice + "\n" +
				"DistanceFalse: " + this.distanceFalse + "\n" +
				"DistanceTrue: " + this.distanceTrue + "\n" +
				"}";
		return res;
	}
}
