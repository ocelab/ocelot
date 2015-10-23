package it.unisa.ocelot.suites.generators.mccabe;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.paths.PathSearchSimplex;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.many_edges.ManyEdgesCoverageExperiment;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.budget.BudgetManagerHandler;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;
import it.unisa.ocelot.util.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.util.JMException;

/**
 * Generates statically the smallest set of path that can achieve the full branch coverage and tries to cover each of
 * them. Configures the problem of covering a specific path as a multi-node coverage problem instead of a path coverage 
 * problem.
 * @author simone
 *
 */
public class ReducedMcCabePartialsTestSuiteGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private boolean satisfied;

	public ReducedMcCabePartialsTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		super(pCFG);
		this.config = pConfigManager;
	}

	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite)
			throws TestSuiteGenerationException {
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

	private void coverMcCabePaths(Set<TestCase> suite)
			throws TestSuiteGenerationException {
		McCabeCalculator mcCabeCalculator = new McCabeCalculator(cfg);
		mcCabeCalculator.calculateMcCabePaths();
		
		List<List<LabeledEdge>> mcCabePaths;
		PathSearchSimplex searchPath = new PathSearchSimplex(
				this.cfg, 
				mcCabeCalculator.getMcCabeVectorPaths(), 
				config.getReducedMcCabeCoverageTimes());
		
		searchPath.solve(this.getUncoveredEdges(suite));
		mcCabePaths = searchPath.getChosenPaths();
		
		this.setupBudgetManager(mcCabePaths.size() * 3);
		
		boolean improvement = true;
		
		double lastCoverage = 0;
		while (improvement && lastCoverage < this.config.getRequiredCoverage()) {
			try { 
				lastCoverage = calculator.getBranchCoverage();
			} catch (NullPointerException e) {
				lastCoverage = 0;
			}
			this.println("### Starting an iteration... ###");
			Set<TestCase> lastIterationTestCases = new HashSet<TestCase>();
			for (List<LabeledEdge> aMcCabePath : mcCabePaths) {
				List<LabeledEdge> uncovered = this.getUncoveredEdges(suite);
				
				for (int i = aMcCabePath.size()-1; i >= 0; i--)
					if (!uncovered.contains(aMcCabePath.get(i)))
						aMcCabePath.remove(i);
					else
						break;
				
				ManyEdgesCoverageExperiment exp = new ManyEdgesCoverageExperiment(
						cfg, config, cfg.getParameterTypes(), aMcCabePath);
	
				exp.initExperiment(this.budgetManager);
				
				this.printSeparator();
				this.print("Current target: ");
				this.println(aMcCabePath);
				
				try {
					exp.basicRun();
				} catch (JMException | ClassNotFoundException e) {
					throw new TestSuiteGenerationException(e.getMessage());
				}
	
				double fitnessValue = exp.getFitnessValue();
				VariableTranslator translator = new VariableTranslator(exp.getSolution());
	
				Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());
	
				TestCase testCase = this.createTestCase(numericParams, suite.size());
				suite.add(testCase);
				
				this.measureBenchmarks("McCabe", suite, exp.getNumberOfEvaluation());
				lastIterationTestCases.add(testCase);
				
				this.println("Fitness function: " + fitnessValue + ". ");
				if (fitnessValue == 0.0)
					this.println("Path covered!");
				else
					this.println("Path not covered...");
				this.println("Parameters found: ");
				this.println(Utils.printParameters(numericParams));
			}
			calculator.calculateCoverage(suite);
			improvement = calculator.getBranchCoverage() > lastCoverage;
			lastCoverage = calculator.getBranchCoverage();
			this.println("### Iteration ended! ###");
			if (improvement) {
				searchPath.solve(this.getUncoveredEdges(suite));
				mcCabePaths = searchPath.getChosenPaths();
			} else {
				this.removeLastBenchmark();
				suite.removeAll(lastIterationTestCases);
			}
		}
	}
}
