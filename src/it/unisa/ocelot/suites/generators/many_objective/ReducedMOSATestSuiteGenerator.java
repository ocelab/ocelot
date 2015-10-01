package it.unisa.ocelot.suites.generators.many_objective;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.dominators.Dominators;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.edge_graph.EdgeGraph;
import it.unisa.ocelot.c.edge_graph.EdgeWrapper;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.many_objective.MOSABranchCoverageExperiment;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;

public class ReducedMOSATestSuiteGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private ConfigManager config;
	private boolean satisfied;

	public ReducedMOSATestSuiteGenerator(ConfigManager config, CFG cfg) {
		super(cfg);
		this.config = config;
	}

	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite)
			throws TestSuiteGenerationException {

		Set<TestCase> suite = new HashSet<TestCase>(pSuite);

		coverMultiObjectiveBranches(suite);
//		this.measureBenchmarks("MOSA algorithm", suite);

		calculator.calculateCoverage(suite);
		System.out.println("Coverage of MOSA test suite = "
				+ calculator.getBranchCoverage());
		
		if (calculator.getBranchCoverage() >= this.config.getRequiredCoverage()) {
			this.satisfied = true;
		}
			
		return suite;
	}
	
	public boolean isSatisfied() {
		return satisfied;
	}

	private void coverMultiObjectiveBranches(Set<TestCase> suite)
			throws TestSuiteGenerationException {

		SolutionSet archiveSolutions = new SolutionSet();
		
		
		//Creates an edge-graph for the CFG, gets all the non-dominator nodes and retrieves the original edges from
		//the wrappers in a list that will be used
		EdgeGraph<CFGNode, LabeledEdge> graph = new EdgeGraph<CFGNode, LabeledEdge>(cfg, cfg.getStart(), cfg.getEnd());
		
		Dominators<EdgeWrapper<LabeledEdge>, DefaultEdge> dominators = 
				new Dominators<EdgeWrapper<LabeledEdge>, DefaultEdge>(graph, graph.getStart());
		
		List<EdgeWrapper<LabeledEdge>> nonDominators = dominators.getNonDominators();
		nonDominators.remove(graph.getEnd());
		
		List<LabeledEdge> realTargets = new ArrayList<LabeledEdge>();
		
		this.startBenchmarks();
		
		int id = 0;
		for (EdgeWrapper<LabeledEdge> wrapper : nonDominators) {
			LabeledEdge target = wrapper.getWrappedEdge();
			target.setObjectiveID(id);
			realTargets.add(target);
			id++;
		}
		
		//Starts the experiment
		MOSABranchCoverageExperiment mosaExperiment = new MOSABranchCoverageExperiment(
				cfg, realTargets, config, cfg.getParameterTypes());
 
		mosaExperiment.initExperiment();
		try {
			archiveSolutions = mosaExperiment.multiObjectiveRun();
		} catch (JMException | ClassNotFoundException e) {
			throw new TestSuiteGenerationException(e.getMessage());
		}

		Solution currentSolution;
		List<Integer> numberOfEvaluations = mosaExperiment.getNumberOfEvaluations();
		for (int i = 0; i < archiveSolutions.size(); i++) {
			currentSolution = archiveSolutions.get(i);
			VariableTranslator translator = new VariableTranslator(currentSolution);

			Object[][][] numericParams = translator.translateArray(cfg
					.getParameterTypes());

			TestCase testCase = this.createTestCase(numericParams, suite.size());
			suite.add(testCase);
			
			this.measureBenchmarks("MOSA Target", suite, numberOfEvaluations.get(i));
		}

	}
}
