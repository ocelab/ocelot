package it.unisa.ocelot.suites.generators.many_objective;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.many_objective.MOSABranchCoverageExperiment;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;

public class MOSATestSuiteGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private ConfigManager config;
	private boolean satisfied;

	public MOSATestSuiteGenerator(ConfigManager config, CFG cfg) {
		super(cfg);
		this.config = config;
	}

	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite)
			throws TestSuiteGenerationException {

		Set<TestCase> suite = new HashSet<TestCase>(pSuite);
		this.startBenchmarks();

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
		
		MOSABranchCoverageExperiment mosaExperiment = new MOSABranchCoverageExperiment(
				cfg, cfg.getBranchesFromCFG(), config, cfg.getParameterTypes());
 
		mosaExperiment.initExperiment();
		try {
			archiveSolutions = mosaExperiment.multiObjectiveRun();
		} catch (JMException | ClassNotFoundException e) {
			e.printStackTrace();
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