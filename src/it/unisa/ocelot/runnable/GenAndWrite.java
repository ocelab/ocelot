package it.unisa.ocelot.runnable;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGBuilder;
import it.unisa.ocelot.c.cfg.CFGWindow;
import it.unisa.ocelot.conf.ConfigManager;
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
import it.unisa.ocelot.writer.TestFramework;
import it.unisa.ocelot.writer.check.CheckFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.io.output.TeeOutputStream;

public class GenAndWrite {
	private static final String CONFIG_FILENAME = "config.properties";

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

		TestSuiteGenerator generator = TestSuiteGeneratorHandler.getInstance(config, cfg);
		TestSuiteMinimizer minimizer = TestSuiteMinimizerHandler.getInstance(config);
		
		System.out.println("Generator: " + generator.getClass().getSimpleName());
		System.out.println("Minimizer: " + minimizer.getClass().getSimpleName());
		Set<TestCase> suite = generator.generateTestSuite();

		Set<TestCase> minimizedSuite = minimizer.minimize(suite);
		CoverageCalculator calculator = new CoverageCalculator(cfg);
		
		calculator.calculateCoverage(minimizedSuite);

		System.out.println("-------------------------------------------------------");
		System.out.println("Minimized test cases: " + minimizedSuite.size());
		System.out.println("Branch coverage achieved: " + calculator.getBranchCoverage());
		System.out.println("Statement coverage achieved: " + calculator.getBlockCoverage());
		System.out.println("-------------------------------------------------------");
		
		String formattedFilename = config.getTestFilename();
		formattedFilename = formattedFilename.replaceAll("[^A-Za-z0-9]", "_");
		String filename = "_Test_" + config.getTestFunction() + "_" + formattedFilename + ".c";
		System.out.println("Writing test suite on " + filename + "...");
		
		TestFramework framework = new TestFramework(new CheckFactory());
		
		String content = framework.writeTestSuite(minimizedSuite);
		Utils.writeFile(filename, content);
		
		System.out.println("Operation completed!");
	}
}
