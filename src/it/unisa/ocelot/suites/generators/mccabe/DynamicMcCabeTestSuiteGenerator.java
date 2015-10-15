package it.unisa.ocelot.suites.generators.mccabe;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.DynamicMcCabeCalculator;
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
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Variable;
import jmetal.util.JMException;

public class DynamicMcCabeTestSuiteGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private boolean satisfied;

	public DynamicMcCabeTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		super(pCFG);
		this.config = pConfigManager;
	}

	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite) throws TestSuiteGenerationException {
		Set<TestCase> suite = new HashSet<TestCase>(pSuite);

		this.startBenchmarks();

		coverMcCabePaths(suite);

		calculator.calculateCoverage(suite);
		if (calculator.getBranchCoverage() >= this.config.getRequiredCoverage()) {
			this.satisfied = true;
		}

		return suite;
	}
	
	public boolean isSatisfied() {
		return satisfied;
	}

	private void coverMcCabePaths(Set<TestCase> suite) throws TestSuiteGenerationException {
		DynamicMcCabeCalculator mcCabeCalculator = new DynamicMcCabeCalculator(cfg);
		
		LabeledEdge currentTarget = cfg.getStart().navigate(cfg).goFlow().edge();
		
		while (currentTarget != null) {
			EdgeCoverageExperiment exp = new EdgeCoverageExperiment(cfg, config, cfg.getParameterTypes(), currentTarget);

			this.print("Current target: ");
			this.println(currentTarget);
			
			double prevCoverage = calculator.getBranchCoverage();
			
			exp.initExperiment();
			try {
				exp.basicRun();
			} catch (JMException | ClassNotFoundException e) {
				throw new TestSuiteGenerationException(e.getMessage());
			}

			double fitnessValue = exp.getFitnessValue();
			VariableTranslator translator = new VariableTranslator(exp.getSolution());

			Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());

			TestCase testCase = this.createTestCase(numericParams, suite.size());
			
			calculator.calculateCoverage(numericParams);

			this.println("Fitness function: " + fitnessValue + ". ");
			if (fitnessValue == 0.0) {
				this.println("Target covered!");
				mcCabeCalculator.addPath(calculator.getCoveredPath());
				suite.add(testCase);
			} else {
				this.println("Target not covered...");
				System.out.println("Useless test case. Discarded.");
			}
			this.measureBenchmarks("McCabe target", suite, exp.getNumberOfEvaluation());
			
			this.println("Parameters found: " + Arrays.toString(numericParams));
			this.printSeparator();
			
			currentTarget = mcCabeCalculator.getNextTarget();
		}
	}
}
