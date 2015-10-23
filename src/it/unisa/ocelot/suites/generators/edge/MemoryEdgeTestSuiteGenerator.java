package it.unisa.ocelot.suites.generators.edge;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.edges.EdgeCoverageExperiment;
import it.unisa.ocelot.genetic.nodes.NodeCoverageExperiment;
import it.unisa.ocelot.genetic.paths.PathCoverageExperiment;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.budget.BudgetManagerHandler;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;
import it.unisa.ocelot.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Variable;
import jmetal.util.JMException;

/**
 * Classic memory based approach. For each test case generated, all the covered edges are removed from
 * the set of targets. This approach can be run so that also serendipitous solution are added to the 
 * test suite.
 * @author simone
 *
 */
public class MemoryEdgeTestSuiteGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private boolean satisfied;
	
	public MemoryEdgeTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		super(pCFG);
		
		satisfied = false;
		
		this.config = pConfigManager;
	}
	
	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite) throws TestSuiteGenerationException {
		Set<TestCase> suite = new HashSet<TestCase>(pSuite);
		
		this.startBenchmarks();
		
		coverSingleTargets(suite);
		
		calculator.calculateCoverage(suite);
		if (calculator.getBranchCoverage() >= this.config.getRequiredCoverage()) {
			this.satisfied = true;
		}
				
		return suite;
	}
	
	@Override
	public boolean isSatisfied() {
		return satisfied;
	}
	
	private void coverSingleTargets(Set<TestCase> suite) throws TestSuiteGenerationException {
		List<LabeledEdge> uncoveredEdges = this.getUncoveredEdges(suite);

		this.setupBudgetManager(uncoveredEdges.size());
		
		Collections.shuffle(uncoveredEdges);
		while (!uncoveredEdges.isEmpty()
				&& calculator.getBranchCoverage() < config.getRequiredCoverage()) {
			LabeledEdge targetEdge = uncoveredEdges.get(0);
			uncoveredEdges.remove(0); // avoids infinite loop

			EdgeCoverageExperiment exp = new EdgeCoverageExperiment(cfg, config,
					cfg.getParameterTypes(), targetEdge);
			
			exp.initExperiment(this.budgetManager);
			
			if (config.getSerendipitousCoverage())
				exp.setSerendipitousPotentials(new HashSet<LabeledEdge>(this.getUncoveredEdges(suite)));
			
			CFGNode departingNode = cfg.getEdgeSource(targetEdge);
			this.printSeparator();
			this.println("Current target: branch " + targetEdge.toString() + " of node " + departingNode);
			
			try {
				this.print("Running... ");
				exp.basicRun();
				this.println("Done!");
			} catch (JMException | ClassNotFoundException e) {
				throw new TestSuiteGenerationException(e.getMessage());
			}
			
			if (config.getSerendipitousCoverage())
				this.addSerendipitousTestCases(exp, suite);

			double fitnessValue = exp.getFitnessValue();
			VariableTranslator translator = new VariableTranslator(exp.getSolution());
			
			Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());
			TestCase testCase = this.createTestCase(numericParams, suite.size());
				
			this.print("Fitness function: " + fitnessValue + ". ");
			if (fitnessValue == 0.0) {
				this.println("Target covered!");
				suite.add(testCase);
				this.measureBenchmarks("Single target", suite, exp.getNumberOfEvaluation());
			} else
				this.println("Target not covered...");

			this.println("Parameters found: " + Utils.printParameters(numericParams));

			calculator.calculateCoverage(suite);
			uncoveredEdges.removeAll(calculator.getCoveredEdges());
			this.budgetManager.updateTargets(uncoveredEdges.size());
		}
	}
}
