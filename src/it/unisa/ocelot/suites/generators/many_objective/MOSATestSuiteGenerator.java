package it.unisa.ocelot.suites.generators.many_objective;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.many_objective.MOSABranchCoverageExperiment;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.budget.BasicBudgetManager;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

/**
 * Many-Objective test suite generator. Approach as described in Panichella et al. (Reformulating Branch Coverage as a 
 * Many-Objective Optimization Problem).
 * @author simone
 *
 */
public class MOSATestSuiteGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private boolean satisfied;
	
	private int evaluations;

	public MOSATestSuiteGenerator(ConfigManager config, CFG cfg) {
		super(cfg, config);
	}

	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite)
			throws TestSuiteGenerationException {

		Set<TestCase> suite = new HashSet<TestCase>(pSuite);
		this.startBenchmarks();

		coverMultiObjectiveBranches(suite, cfg.getIdBranchesFromCFG());
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

	protected void coverMultiObjectiveBranches(Set<TestCase> suite, List<LabeledEdge> pTargets)
			throws TestSuiteGenerationException {

		SolutionSet archiveSolutions = new SolutionSet();
		MOSABranchCoverageExperiment mosaExperiment = new MOSABranchCoverageExperiment(cfg, this.graphList, pTargets, config);
		//MOSABranchCoverageExperiment mosaExperiment = new MOSABranchCoverageExperiment(cfg, this.graphList, pTargets, config, cfg.getParameterTypes());
		//MOSABranchCoverageExperiment mosaExperiment = null;
		
		this.setupBudgetManager(1);
		try {
			this.budgetManager = this.budgetManager.changeTo(BasicBudgetManager.class);
		} catch (InstantiationException e) {
			throw new TestSuiteGenerationException(e.getMessage());
		}
 
		mosaExperiment.initExperiment(this.budgetManager);
		try {
			archiveSolutions = mosaExperiment.multiObjectiveRun();
			System.out.println(mosaExperiment.getAlgorithmStats().getLog());
			this.evaluations = mosaExperiment.getNumberOfEvaluation();
		} catch (JMException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new TestSuiteGenerationException(e.getMessage());
		}

		Solution currentSolution;
		List<Integer> numberOfEvaluations = mosaExperiment.getNumberOfEvaluations();
		for (int i = 0; i < archiveSolutions.size(); i++) {
			currentSolution = archiveSolutions.get(i);
			VariableTranslator translator = new VariableTranslator(this.graphList, this.scalarNodeIndexMap);

			//Object[][][] numericParams = translator.translateArray(cfg
			//		.getParameterTypes());
			//Object[][][] numericParams = null;
			Graph graph = translator.getGraphFromSolution(currentSolution);

			TestCase testCase = this.createTestCase(graph, suite.size());
			suite.add(testCase);
			
			this.measureBenchmarks("MOSA Target", suite, numberOfEvaluations.get(i));
		}
	}
	
	@Override
	public int getNumberOfEvaluations() {
		return this.evaluations;
	}
}