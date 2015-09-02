package it.unisa.ocelot.runnable;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGBuilder;
import it.unisa.ocelot.c.cfg.CFGWindow;
import it.unisa.ocelot.conf.ConfigManager;
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

public class ExecuteWholeCoverage {
	private static final String CONFIG_FILENAME = "config.properties";
	private static final String OUTPUT_FOLDER = "results";

	static {
		System.loadLibrary("Test");
	}

	public static void main(String[] args) throws Exception {
		ConfigManager.setFilename(CONFIG_FILENAME);
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

		BenchmarkCalculator timeBenchmark = new TimeBenchmarkCalculator();
		BenchmarkCalculator coverageBenchmark = new BranchCoverageBenchmarkCalculator(cfg);
		BenchmarkCalculator sizeBenchmark = new TestSuiteSizeBenchmarkCalculator(cfg);

		generator.addBenchmark(timeBenchmark);
		generator.addBenchmark(coverageBenchmark);
		generator.addBenchmark(sizeBenchmark);

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
		System.out.println("-------------------------------------------------------");
	}

	public static void showUI(final CFG pCFG) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				CFGWindow window = new CFGWindow(pCFG);
				window.setVisible(true);
			}
		});
	}
}
