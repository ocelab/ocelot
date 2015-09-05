package it.unisa.ocelot.suites.generators.paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.util.JMException;
import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.TooManyInfeasiblePathsException;
import it.unisa.ocelot.c.cfg.WeightedCFG;
import it.unisa.ocelot.c.cfg.paths.DynamicPathsFinder;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.paths.PathCoverageExperiment;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;

public class DynamicPathsTestSuiteGenerator extends TestSuiteGenerator {	
	public DynamicPathsTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		super(pCFG);
		this.config = pConfigManager;
	}
	
	@Override
	public Set<TestCase> generateTestSuite() throws TestSuiteGenerationException {
		WeightedCFG weighted = new WeightedCFG(this.cfg);
		Set<TestCase> suite = new HashSet<TestCase>();
		
		DynamicPathsFinder pathFinder = new DynamicPathsFinder(weighted);
		
		while (this.calculator.getBranchCoverage() < this.config.getRequiredCoverage()) {
			List<LabeledEdge> targetPath = pathFinder.getMaxCoveragePath();
		
			PathCoverageExperiment exp = new PathCoverageExperiment(cfg, config, cfg.getParameterTypes(), targetPath);
	
			this.print("Current target: ");
			this.println(targetPath);
			
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
	
			this.println("Fitness function: " + fitnessValue + ". ");
			if (fitnessValue == 0.0) {
				pathFinder.setExecutedPath(targetPath);
				suite.add(testCase);
				this.println("Path covered!");
			} else {
				this.println("Path not covered...");
				try {
					pathFinder.notifyInfeasiblePath();
				} catch (TooManyInfeasiblePathsException e) {
					break;
				}
			}
			this.println("Parameters found: " + Arrays.toString(numericParams));
			this.printSeparator();
	
			this.measureBenchmarks("Dynamic Path", suite, exp.getNumberOfEvaluation());
		}

		return suite;
	}

}
