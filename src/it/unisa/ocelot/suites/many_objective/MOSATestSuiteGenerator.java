package it.unisa.ocelot.suites.many_objective;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.many_objective.MOSABranchCoverageExperiment;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;

public class MOSATestSuiteGenerator extends TestSuiteGenerator {
	private ConfigManager config;
	private CFG cfg;
	private CoverageCalculator calculator;

	public MOSATestSuiteGenerator(ConfigManager config, CFG cfg) {
		this.config = config;
		this.cfg = cfg;
		this.calculator = new CoverageCalculator(cfg);
	}

	@Override
	public Set<TestCase> generateTestSuite()
			throws TestSuiteGenerationException {

		Set<TestCase> suite = new HashSet<TestCase>();
		this.startBenchmarks();

		coverMultiObjectiveBranches(suite);
//		this.measureBenchmarks("MOSA algorithm", suite);

		calculator.calculateCoverage(suite);
		System.out.println("Coverage of MOSA test suite = "
				+ calculator.getBranchCoverage());
			
		return suite;
	}

	@SuppressWarnings("unchecked")
	private void coverMultiObjectiveBranches(Set<TestCase> suite)
			throws TestSuiteGenerationException {

		SolutionSet archiveSolutions = new SolutionSet();
		
		MOSABranchCoverageExperiment mosaExperiment = new MOSABranchCoverageExperiment(
				cfg, config, cfg.getParameterTypes());
 
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
	
	private TestCase createTestCase(Object[][][] pParams, int id) {
		this.calculator.calculateCoverage(pParams);

		TestCase tc = new TestCase();
		tc.setId(id);
		tc.setCoveredEdges(calculator.getCoveredEdges());
		tc.setParameters(pParams);

		return tc;
	}
}
