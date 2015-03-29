package it.unisa.ocelot.simulator;

import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;

import java.util.List;

public class TestSimulatorListener implements SimulatorListener {

	@Override
	public void onEdgeVisit(LabeledEdge pEdge) {
		System.out.println("--> Flow visiting " + pEdge.toString());

	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent) {
		System.out.println("--> Condition visiting " + pEdge.toString() + 
				"\n\tDistanceT:" + pEvent.distanceTrue +
				"\n\tDistanceF:" + pEvent.distanceFalse);
	}

	@Override
	public void onEdgeVisit(LabeledEdge pEdge, ExecutionEvent pEvent, List<ExecutionEvent> pCases) {
		System.out.println("--> Case choice " + pEdge.toString());
		for (ExecutionEvent pCase : pCases) {
			System.out.println("\t" + pCase.choice + ", distance:" + pCase.distanceTrue);
		}
	}

	@Override
	public void onNodeVisit(CFGNode pNode) {
		System.out.println("* In node " + pNode.toString());
	}

}
