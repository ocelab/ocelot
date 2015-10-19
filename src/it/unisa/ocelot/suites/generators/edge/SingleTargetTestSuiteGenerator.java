package it.unisa.ocelot.suites.generators.edge;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.edges.EdgeCoverageExperiment;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;
import it.unisa.ocelot.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.util.JMException;

/**
 * Test suite generators which selects, in turn, each single branch and tries to cover it.
 * @author simone
 *
 */
public class SingleTargetTestSuiteGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private boolean satisfied;
	
	public SingleTargetTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
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
		List<LabeledEdge> edgesToCover = new ArrayList<LabeledEdge>(cfg.edgeSet());
		Collections.shuffle(edgesToCover);
		while (!edgesToCover.isEmpty()) {
			LabeledEdge targetEdge = edgesToCover.remove(0); //avoids infinite loop
			
			EdgeCoverageExperiment exp = new EdgeCoverageExperiment(cfg, config, cfg.getParameterTypes(), targetEdge);
			exp.initExperiment();
			
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
			
			double fitnessValue = exp.getFitnessValue();
			VariableTranslator translator = new VariableTranslator(exp.getSolution());
				
			this.print("Fitness function: " + fitnessValue + ". ");
			if (fitnessValue == 0.0)
				this.println("Target covered!");
			else
				this.println("Target not covered...");
			
			Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());
			TestCase testCase = this.createTestCase(numericParams, suite.size());
			suite.add(testCase);
			
			this.measureBenchmarks("Target", suite, 0);
			
			this.println("Parameters found: " + Utils.printParameters(numericParams));
		}
	}
}
