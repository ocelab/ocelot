package it.unisa.ocelot.suites.generators.many_objective;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.dominators.Dominators;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.edge_graph.EdgeGraph;
import it.unisa.ocelot.c.edge_graph.EdgeWrapper;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;

/**
 * Many-Objective test suite generator. Approach inspired by Panichella et al. (Reformulating Branch Coverage as a 
 * Many-Objective Optimization Problem). The set of branches that have to be covered is reduced: only non-dominant
 * branches are selected as targets.
 * @author simone
 *
 */
public class ReducedMOSATestSuiteGenerator extends MOSATestSuiteGenerator implements CascadeableGenerator {
	private boolean satisfied;

	public ReducedMOSATestSuiteGenerator(ConfigManager config, CFG cfg) {
		super(config, cfg);
	}

	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite)
			throws TestSuiteGenerationException {

		Set<TestCase> suite = new HashSet<TestCase>(pSuite);
		this.startBenchmarks();
		
		List<LabeledEdge> realTargets = this.getRealTargets();

		coverMultiObjectiveBranches(suite, realTargets);

		calculator.calculateCoverage(suite);
		System.out.println("Coverage of MOSA test suite = "
				+ calculator.getBranchCoverage());
		
		if (calculator.getBranchCoverage() >= this.config.getRequiredCoverage()) {
			this.satisfied = true;
		}
			
		return suite;
	}
	
	private List<LabeledEdge> getRealTargets() {
		//Creates an edge-graph for the CFG, gets all the non-dominator nodes and retrieves the original edges from
		//the wrappers in a list that will be used
		EdgeGraph<CFGNode, LabeledEdge> graph = new EdgeGraph<CFGNode, LabeledEdge>(cfg, cfg.getStart(), cfg.getEnd());
		
		Dominators<EdgeWrapper<LabeledEdge>, DefaultEdge> dominators = 
				new Dominators<EdgeWrapper<LabeledEdge>, DefaultEdge>(graph, graph.getStart());
		
		List<EdgeWrapper<LabeledEdge>> nonDominators = dominators.getNonDominators();
		nonDominators.remove(graph.getEnd());
		
		List<LabeledEdge> realTargets = new ArrayList<LabeledEdge>();
				
		int id = 0;
		for (EdgeWrapper<LabeledEdge> wrapper : nonDominators) {
			LabeledEdge target = wrapper.getWrappedEdge();
			target.setObjectiveID(id);
			realTargets.add(target);
			id++;
		}
		
		return realTargets;
	}
	
	@Override
	public boolean isSatisfied() {
		return this.satisfied;
	}
}
