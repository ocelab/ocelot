package it.unisa.ocelot.suites.generators.cdg;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.CaseEdge;
import it.unisa.ocelot.c.cfg.edges.EdgeComparator;
import it.unisa.ocelot.c.cfg.edges.FalseEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.edges.TrueEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.edges.CDG_BasedExperiment;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.CoverageCalculatorListener;
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

import org.eclipse.cdt.core.settings.model.util.Comparator;

public class CDG_BasedApproachGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private CoverageCalculatorListener coverageCalculatorListener;
	private final List<LabeledEdge> branches;
	private boolean satisfied;

	public CDG_BasedApproachGenerator(ConfigManager config, CFG cfg) {
		super(cfg);
		this.config = config;
		
		this.coverageCalculatorListener = new CoverageCalculatorListener(cfg);
		this.branches = cfg.getBranchesFromCFG();
	}

	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite) throws TestSuiteGenerationException {

		Set<TestCase> suite = new HashSet<>(pSuite);

		this.startBenchmarks();

		coverEdges(suite);

		calculator.calculateCoverage(suite);
		System.out.println("Coverage of Harman's method test suite = "
				+ calculator.getBranchCoverage());

		// restore all branches as uncovered for next experiments
		for (LabeledEdge edge : this.branches)
			edge.setUncovered();
		
		if (calculator.getBranchCoverage() >= this.config.getRequiredCoverage()) {
			this.satisfied = true;
		}

		return suite;
	}
	
	public boolean isSatisfied() {
		return satisfied;
	}

	private void coverEdges(Set<TestCase> suite) throws TestSuiteGenerationException {
		EdgeComparator comparator = new EdgeComparator(cfg);
		Collections.sort(branches, comparator);

		int index = 0;
		for (LabeledEdge branch : branches) {

			if (!branch.isCovered()) {

				CDG_BasedExperiment experiment = new CDG_BasedExperiment(cfg, config,
						cfg.getParameterTypes(), branches, branch);
				experiment.initExperiment();
				try {
					experiment.basicRun();
				} catch (JMException | ClassNotFoundException e) {
					throw new TestSuiteGenerationException(e.getMessage());
				}

				double fitnessValue = experiment.getFitnessValue();
				VariableTranslator translator = new VariableTranslator(experiment.getSolution());

				this.print("Fitness function: " + fitnessValue + ". ");
				if (fitnessValue == 0.0) {
					branch.setCovered();
					this.println("Target covered!");

					Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());

					markBranchCoveredByExecution(numericParams);

					TestCase testCase = this.createTestCase(numericParams, suite.size());
					suite.add(testCase);

					this.println("Parameters found: " + Arrays.toString(numericParams));
				} else {
					this.println("Target not covered... test case discarded");
				}

				System.out.println(experiment.getNumberOfEvaluation());
				this.measureBenchmarks("CDG-based approach", suite,
						experiment.getNumberOfEvaluation());
			}// end if branch is covered
		}
	}

	private void markBranchCoveredByExecution(Object[][][] arguments) {
		CBridge bridge = new CBridge();
		EventsHandler handler = new EventsHandler();
		bridge.getEvents(handler, arguments[0][0], arguments[1], arguments[2][0]);

		Simulator simulator = new Simulator(this.cfg, handler.getEvents());
		simulator.addListener(coverageCalculatorListener);
		simulator.simulate();

		List<LabeledEdge> branchCoveredByExecution = coverageCalculatorListener
				.getCoveredBranches();
		for (LabeledEdge branch : branchCoveredByExecution) {
			branch.setCovered();
		}
	}
}
