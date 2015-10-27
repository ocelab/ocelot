package it.unisa.ocelot.runnable.runners;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGBuilder;
import it.unisa.ocelot.c.cfg.CFGWindow;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.types.CTypeHandler;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.benchmarks.BenchmarkCalculator;
import it.unisa.ocelot.suites.benchmarks.BranchCoverageBenchmarkCalculator;
import it.unisa.ocelot.suites.benchmarks.TestSuiteSizeBenchmarkCalculator;
import it.unisa.ocelot.suites.benchmarks.TimeBenchmarkCalculator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGeneratorHandler;
import it.unisa.ocelot.suites.minimization.TestSuiteMinimizer;
import it.unisa.ocelot.suites.minimization.TestSuiteMinimizerHandler;
import it.unisa.ocelot.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.io.output.TeeOutputStream;

public class ExecuteWholeCoverage implements Runnable {
	private static final String OUTPUT_FOLDER = "RESULTS";

	public void run() {
		try {
			ConfigManager config = ConfigManager.getInstance();
	
			// Sets up the output file
			File outputDirectory = new File(config.getOutputFolder());
			outputDirectory.mkdirs();
			FileOutputStream fos = new FileOutputStream(config.getOutputFolder() + "exp_res.txt");
			TeeOutputStream myOut = new TeeOutputStream(System.out, fos);
			PrintStream ps = new PrintStream(myOut);
			System.setOut(ps);
	
			// Builds the CFG and sets the target
			CFG cfg = CFGBuilder.build(config.getTestFilename(), config.getTestFunction());
	
			
			int mcCabePaths = cfg.edgeSet().size() - cfg.vertexSet().size() + 1;
			System.out.println("Cyclomatic complexity: " + mcCabePaths);
	
			if (config.getUI())
				showUI(cfg);
	
			TestSuiteGenerator generator = TestSuiteGeneratorHandler.getInstance(config, cfg);
			TestSuiteMinimizer minimizer = TestSuiteMinimizerHandler.getInstance(config);
			
			System.out.println("Generator: " + generator.getClass().getSimpleName());
			System.out.println("Minimizer: " + minimizer.getClass().getSimpleName());
	
			BenchmarkCalculator timeBenchmark = new TimeBenchmarkCalculator();
			BenchmarkCalculator coverageBenchmark = new BranchCoverageBenchmarkCalculator(cfg);
			BenchmarkCalculator sizeBenchmark = new TestSuiteSizeBenchmarkCalculator(cfg);
	
			generator.addBenchmark(timeBenchmark);
			generator.addBenchmark(coverageBenchmark);
			generator.addBenchmark(sizeBenchmark);
			
			CTypeHandler typeHandler = new CTypeHandler(cfg.getParameterTypes());
			CBridge.initialize(
					typeHandler.getValues().size(), 
					typeHandler.getPointers().size(),
					typeHandler.getPointers().size());
	
			Set<TestCase> suite = generator.generateTestSuite();
	
			if (config.getPrintResults()) {
				String preFilename = config.getTestFunction() + "_" + config.getTestSuiteGenerator();
	
				String cumulativeResult = "";
				cumulativeResult += timeBenchmark.getPrintableCumulativeResults() + "\n";
				cumulativeResult += coverageBenchmark.getPrintableCumulativeResults() + "\n";
				cumulativeResult += sizeBenchmark.getPrintableCumulativeResults() + "\n";
				Utils.writeFile(OUTPUT_FOLDER + "/" + preFilename + "_cumulative.txt", cumulativeResult);
	
				String result = "";
				result += timeBenchmark.getPrintableResults() + "\n";
				result += coverageBenchmark.getPrintableResults() + "\n";
				result += sizeBenchmark.getPrintableResults() + "\n";
				Utils.writeFile(OUTPUT_FOLDER + "/" + preFilename + "_normal.txt", result);
			}
	
			System.out.println(timeBenchmark);
			System.out.println(coverageBenchmark);
			System.out.println(sizeBenchmark);
	
			Set<TestCase> minimizedSuite = minimizer.minimize(suite);
	
			System.out.println("-------------------------------------------------------");
			System.out.println("Total test cases: " + suite.size());
			System.out.println("Minimized test cases: " + minimizedSuite.size());
			System.out
					.println("-------------------------------------------------------");
			
			System.out.println("Minimised test suite:");
			for (TestCase tc : minimizedSuite) {
				System.out.println(tc.getCoveredPath());
				System.out.println(Utils.printParameters(tc.getParameters()));
				System.out.println("#########");
			}
			
			System.out.println("-----------------------------------------------------");
			System.out.println("Still uncovered:");
			
			CoverageCalculator calculator = new CoverageCalculator(cfg);
			calculator.calculateCoverage(minimizedSuite);
			
			for (LabeledEdge uncoveredEdge : calculator.getUncoveredBranches()) {
				System.out.println("Branch " + uncoveredEdge.toString() 
						+ " from node " + cfg.getEdgeSource(uncoveredEdge).toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void showUI(final CFG pCFG) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				CFGWindow window = new CFGWindow(pCFG);
				window.setVisible(true);
			}
		});
	}
}
