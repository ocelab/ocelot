package it.unisa.ocelot.suites;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.paths.PathCoverageExperiment;
import it.unisa.ocelot.simulator.CoverageCalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jmetal.core.Variable;
import jmetal.util.JMException;

/**
 * This class generates a Test Suite derived by a series of
 * PathCoverageExperiments performed on McCabe Linearly Independent Paths
 * 
 * @author giograno
 *
 */
public class VanillaMcCabeTestSuiteGenerator extends TestSuiteGenerator {
	private ConfigManager manager;
	private CFG cfg;
	private CoverageCalculator coverageCalculator;

	public VanillaMcCabeTestSuiteGenerator(ConfigManager configManager, CFG cfg) {
		this.manager = configManager;
		this.cfg = cfg;
		this.coverageCalculator = new CoverageCalculator(cfg);
	}

	@Override
	public Set<TestCase> generateTestSuite()
			throws TestSuiteGenerationException {
		Set<TestCase> vanillaMcCabeSuite = new HashSet<>();

		this.startBenchmarks();
		fillVanillaMcCabePaths(vanillaMcCabeSuite);
		this.measureBenchmarks("Only McCabe Paths", vanillaMcCabeSuite);

		return vanillaMcCabeSuite;
	}

	private void fillVanillaMcCabePaths(Set<TestCase> suite)
			throws TestSuiteGenerationException {
		McCabeCalculator mcCabeCalculator = new McCabeCalculator(cfg);
		mcCabeCalculator.calculateMcCabePaths();
		ArrayList<ArrayList<LabeledEdge>> mcCabePaths = mcCabeCalculator
				.getMcCabeEdgePaths();

		for (ArrayList<LabeledEdge> aMcCabePath : mcCabePaths) {
			@SuppressWarnings("unchecked")
			PathCoverageExperiment exp = new PathCoverageExperiment(cfg,
					manager, cfg.getParameterTypes(), aMcCabePath);

			exp.initExperiment();

			try {
				exp.basicRun();
			} catch (ClassNotFoundException | JMException e) {
				throw new TestSuiteGenerationException(e.getMessage());
			}

			this.printSeparator();
			this.print("Current McCabe Path: ");
			this.println(aMcCabePath);

			double fitnessValue = exp.getFitnessValue();
			Variable[] params = exp.getVariables();
			VariableTranslator translator = new VariableTranslator(params[0]);

			Object[] numericParams = translator.translateArray(cfg
					.getParameterTypes());

			// Creation of test case and addition to test suite
			TestCase testCase = this
					.createTestCase(numericParams, suite.size());
			suite.add(testCase);

			System.out.print("Fitness function: " + fitnessValue + ". ");
			if (fitnessValue == 0.0)
				System.out.println("Path covered!");
			else
				System.out.println("Path not covered...");
			System.out.println("Parameters found: "
					+ Arrays.toString(numericParams));
			System.out
					.println("-------------------------------------------------------");

		}
	}

	private TestCase createTestCase(Object[] pParams, int id) {
		this.coverageCalculator.calculateCoverage(pParams);

		TestCase tc = new TestCase();
		tc.setId(id);
		tc.setCoveredEdges(coverageCalculator.getCoveredEdges());
		tc.setParameters(pParams);

		return tc;
	}

	private void printSeparator() {
		if (this.manager.getPrintResults())
			System.out.println("------------------------------------------");
	}

	private void print(Object pObject) {
		if (this.manager.getPrintResults())
			System.out.print(pObject);
	}

	private void println(Object pObject) {
		if (this.manager.getPrintResults())
			System.out.println(pObject);
	}

}
