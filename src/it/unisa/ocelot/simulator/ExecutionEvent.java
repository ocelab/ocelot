package it.unisa.ocelot.simulator;

public class ExecutionEvent {
	public int choice;
	public double distanceTrue;
	public double distanceFalse;
	
	public ExecutionEvent(int choice, double distanceTrue, double distanceFalse) {
		this.choice = choice;
		this.distanceFalse = distanceFalse;
		this.distanceTrue = distanceTrue;
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
