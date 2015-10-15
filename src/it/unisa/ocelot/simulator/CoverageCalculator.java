package it.unisa.ocelot.simulator;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.FlowEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
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
	private Set<LabeledEdge> coveredBranches;
	private Set<LabeledEdge> branchSet;
	
	public CoverageCalculator(CFG pCfg) {
		this.cfg = pCfg;
	}
	
	public void calculateCoverage(List<Object[][][]> pParametersList) {
		this.coverageListener = new CoverageCalculatorListener(cfg);
		
		for (Object[][][] params : pParametersList) {
			CBridge bridge = new CBridge();
			EventsHandler h = new EventsHandler();
			
			bridge.getEvents(h, params[0][0], params[1], params[2][0]);
			
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
	
	public void calculateCoverage(Object[][][] pParameters) {
		List<Object[][][]> parametersList = new ArrayList<Object[][][]>();
		parametersList.add(pParameters);
		this.calculateCoverage(parametersList);
	}
	
	public void calculateCoverage(Set<TestCase> pTestCases) {
		Set<LabeledEdge> coveredEdges = new HashSet<LabeledEdge>();
		Set<CFGNode> coveredStatements = new HashSet<CFGNode>();
		for (TestCase testCase : pTestCases) {
			coveredEdges.addAll(testCase.getCoveredEdges());
		}
		
		for (LabeledEdge edge : coveredEdges) {
			coveredStatements.add(this.cfg.getEdgeSource(edge));
			coveredStatements.add(this.cfg.getEdgeTarget(edge));
		}
		
		this.coveredEdges = coveredEdges;
		
		Set<LabeledEdge> coveredBranches = new HashSet<>();
		for (LabeledEdge edge : coveredEdges) {
			if (!(edge instanceof FlowEdge))
				coveredBranches.add(edge);
		}
		this.coveredBranches = coveredBranches;
		
		Set<LabeledEdge> branchSet = new HashSet<>();
		for (LabeledEdge edge : this.cfg.edgeSet()) {
			if (!(edge instanceof FlowEdge))
				branchSet.add(edge);
		}
		this.branchSet = branchSet;
		
		this.branchCoverage = ((double)this.coveredBranches.size()) / this.branchSet.size();
		this.blockCoverage = ((double)coveredStatements.size()) / this.cfg.vertexSet().size();
	}
	
	public double getBranchCoverage() {
		return this.branchCoverage;
	}
	
	public Set<LabeledEdge> getCoveredEdges() {
		return this.coverageListener.getCoveredEdges();
	}
	
	public List<LabeledEdge> getCoveredPath() {
		return this.coverageListener.getCoveredPath();
	}
	
	public Set<LabeledEdge> getUncoveredEdges() {
		Set<LabeledEdge> uncovered = new HashSet<LabeledEdge>(this.cfg.edgeSet());
		uncovered.removeAll(this.coveredEdges);
		return uncovered;
	}
	
	public Set<LabeledEdge> getUncoveredBranches(){
		Set<LabeledEdge> uncoveredEges = new HashSet<>(this.branchSet);
		uncoveredEges.removeAll(this.coveredBranches);
		return uncoveredEges;
	}
	
	public double getBlockCoverage() {
		return this.blockCoverage;
	}
}
