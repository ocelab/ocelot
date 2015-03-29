package it.unisa.ocelot.simulator;

public class CaseExecutionEvent extends ExecutionEvent {
	public boolean chosen;
	
	public CaseExecutionEvent(int choice, double distanceTrue, boolean chosen) {
		super(choice, distanceTrue, 0.0D);
		
		this.chosen = chosen;
	}
	
	public boolean isChosen() {
		return chosen;
	}
	
	@Override
	public String toString() {
		String res = "{\n" + 
				"\tBranchID: " + this.choice + "\n" +
				"\tDistanceTrue: " + this.distanceFalse + "\n" +
				"\tChosen: " + this.chosen +
				"\n}";
		return res;
	}
}
