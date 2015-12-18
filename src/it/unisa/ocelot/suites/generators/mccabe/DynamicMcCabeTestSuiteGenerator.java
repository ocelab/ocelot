package it.unisa.ocelot.suites.generators.mccabe;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.DynamicMcCabeCalculator;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.edges.EdgeCoverageExperiment;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.budget.BudgetManagerHandler;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;
import it.unisa.ocelot.util.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jmetal.core.Solution;
import jmetal.util.JMException;

/**
 * Dynamic McCabe path calculator. Select the first test case randomly. In a second phase it selects as a target
 * the alternative branch of the last condition in the stack of covered conditions.
 * @author simone
 *
 */
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
		
		this.setupBudgetManager(mcCabeCalculator.extimateMissingTargets());
		
		while (currentTarget != null && calculator.getBranchCoverage() < config.getRequiredCoverage()) {
			EdgeCoverageExperiment exp = new EdgeCoverageExperiment(cfg, config, cfg.getParameterTypes(), currentTarget);
			
			exp.initExperiment(this.budgetManager);
			
			exp.setSerendipitousPotentials(new HashSet<>(this.getUncoveredEdges(suite)));
			
			CFGNode departingNode = cfg.getEdgeSource(currentTarget);
			this.printSeparator();
			this.println("Current target: branch " + currentTarget.toString() + " of node " + departingNode);
			try {
				this.print("Running... ");
				exp.basicRun();
				this.println("Done!");
			} catch (JMException | ClassNotFoundException e) {
				e.printStackTrace();
				throw new TestSuiteGenerationException(e.getMessage());
			}
			
			this.addSerendipitousTestCases(exp, suite);

			double fitnessValue = exp.getFitnessValue();
			VariableTranslator translator = new VariableTranslator(exp.getSolution());
			
			if (config.getSerendipitousCoverage())
				for (Solution solution : exp.getSerendipitousSolutions()) {
					VariableTranslator currentTranslator = new VariableTranslator(solution);
					Object[][][] serendipitousParameters = currentTranslator.translateArray(cfg.getParameterTypes());
					calculator.calculateCoverage(serendipitousParameters);
					mcCabeCalculator.addPath(calculator.getCoveredPath());
				}

			Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());

			TestCase testCase = this.createTestCase(numericParams, suite.size());
			

			this.println("Fitness function: " + fitnessValue + ". ");
			if (fitnessValue == 0.0) {
				this.println("Target covered!");
				calculator.calculateCoverage(numericParams);
				mcCabeCalculator.addPath(calculator.getCoveredPath());
				suite.add(testCase);
				this.measureBenchmarks("McCabe target", suite, exp.getNumberOfEvaluation());
			} else {
				this.println("Target not covered...");
				this.println("Useless test case. Discarded.");
			}
			
			this.println("Parameters found: " + Utils.printParameters(numericParams));
			
			currentTarget = mcCabeCalculator.getNextTarget();
			if (currentTarget != null && !getUncoveredEdges(suite).contains(currentTarget)) {
				System.out.println(this.cfg.getEdgeSource(currentTarget));
				System.out.println("ERRORE");
			}
			
			calculator.calculateCoverage(suite);
			this.println("Partial coverage: " + calculator.getBranchCoverage());
			this.budgetManager.updateTargets(mcCabeCalculator.extimateMissingTargets());
		}
	}
}
