package it.unisa.ocelot.simulator;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.simulator.listeners.CoverageCalculatorListener;

import java.util.ArrayList;
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
	
	public void calculateCoverage(Object[] pParameters) {
		List<Object[]> parametersList = new ArrayList<Object[]>();
		parametersList.add(pParameters);
		this.calculateCoverage(parametersList);
	}
	
	public void calculateCoverage(Set<TestCase> pTestCases) {
		List<Object[]> parametersList = new ArrayList<Object[]>();
		for (TestCase testCase : pTestCases) {
			parametersList.add(testCase.getParameters());
		}
		this.calculateCoverage(parametersList);
	}
	
	public double getBranchCoverage() {
		return this.coverageListener.getBranchCoverage();
	}
	
	public Set<LabeledEdge> getCoveredEdges() {
		return this.coverageListener.getCoveredEdges();
	}
	
	public Set<LabeledEdge> getUncoveredEdges() {
		return this.coverageListener.getUncoveredEdges();
	}
	
	public double getBlockCoverage() {
		return this.coverageListener.getBlockCoverage();
	}
}
