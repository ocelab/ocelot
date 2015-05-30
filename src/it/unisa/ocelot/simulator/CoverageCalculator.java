package it.unisa.ocelot.simulator;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.simulator.listeners.CoverageCalculatorListener;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CoverageCalculator {
	private CFG cfg;
	private CoverageCalculatorListener coverageListener;
	public CoverageCalculator(CFG pCfg) {
		this.cfg = pCfg;
	}
	
	public void calculateCoverage(List<Object[]> pParametersList) {
		this.coverageListener = new CoverageCalculatorListener(cfg);
		
		for (Object[] params : pParametersList) {
			CBridge bridge = new CBridge();
			EventsHandler h = new EventsHandler();
			
			bridge.getEvents(h, params);
			
			Simulator simulator = new Simulator(cfg, h.getEvents());
			
			simulator.addListener(this.coverageListener);
			simulator.simulate();
			
			if (!simulator.isSimulationCorrect())
				throw new RuntimeException("Simulation error for parameters " + Arrays.toString(params));
		}
	}
	
	public double getBranchCoverage() {
		return this.coverageListener.getBranchCoverage();
	}
	
	public Set<LabeledEdge> getUncoveredEdges() {
		return this.coverageListener.getUncoveredEdges();
	}
	
	public double getBlockCoverage() {
		return this.coverageListener.getBlockCoverage();
	}
}
