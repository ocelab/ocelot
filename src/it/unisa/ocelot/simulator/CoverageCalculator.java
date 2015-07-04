package it.unisa.ocelot.simulator;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.simulator.listeners.CoverageCalculatorListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoverageCalculator {
	private CFG cfg;
	private CoverageCalculatorListener coverageListener;
	private double branchCoverage;
	private double blockCoverage;
	
	private Set<LabeledEdge> coveredEdges;
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
		
		this.coveredEdges = this.coverageListener.getCoveredEdges();
		this.branchCoverage = this.coverageListener.getBranchCoverage();
		this.blockCoverage = this.coverageListener.getBlockCoverage();
	}
	
	public void calculateCoverage(Object[] pParameters) {
		List<Object[]> parametersList = new ArrayList<Object[]>();
		parametersList.add(pParameters);
		this.calculateCoverage(parametersList);
	}
	
	public void calculateCoverage(Set<TestCase> pTestCases) {
		Set<LabeledEdge> coveredEdges = new HashSet<LabeledEdge>();
		Set<CFGNode> coveredBlocks = new HashSet<CFGNode>();
		for (TestCase testCase : pTestCases) {
			coveredEdges.addAll(testCase.getCoveredEdges());
		}
		
		for (LabeledEdge edge : coveredEdges) {
			coveredBlocks.add(this.cfg.getEdgeSource(edge));
			coveredBlocks.add(this.cfg.getEdgeTarget(edge));
		}
		
		this.coveredEdges = coveredEdges;
		this.branchCoverage = ((double)coveredEdges.size()) / this.cfg.edgeSet().size();
		this.blockCoverage = ((double)coveredBlocks.size()) / this.cfg.vertexSet().size();
	}
	
	public double getBranchCoverage() {
		return this.branchCoverage;
	}
	
	public Set<LabeledEdge> getCoveredEdges() {
		return this.coveredEdges;
	}
	
	public Set<LabeledEdge> getUncoveredEdges() {
		Set<LabeledEdge> uncovered = new HashSet<LabeledEdge>(this.cfg.edgeSet());
		uncovered.removeAll(this.coveredEdges);
		return uncovered;
	}
	
	public double getBlockCoverage() {
		return this.blockCoverage;
	}
}
