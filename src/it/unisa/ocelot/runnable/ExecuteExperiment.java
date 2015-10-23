package it.unisa.ocelot.runnable;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGBuilder;
import it.unisa.ocelot.c.types.CTypeHandler;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.simulator.CBridge;
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
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;

public class ExecuteExperiment {
	private static final String CONFIG_FILENAME = "config.properties";
	private static final String[] EXPERIMENT_GENERATORS = new String[] {
		TestSuiteGeneratorHandler.MOSA_TEST_SUITE_GENERATOR,
		TestSuiteGeneratorHandler.CDG_BASED_APPROACH_SUITE_GENERATOR,
		TestSuiteGeneratorHandler.CASCADE_APPROACH
	};

	static {
		System.loadLibrary("Test");
	}
	
	private static CFG cfg;
	private static ConfigManager config;

	public static void main(String[] args) throws Exception {
		ConfigManager.setFilename(CONFIG_FILENAME);
		config = ConfigManager.getInstance();

		// Sets up the output file
		File outputDirectory = new File(config.getOutputFolder());
		outputDirectory.mkdirs();
		FileOutputStream fos = new FileOutputStream(config.getOutputFolder()
				+ "exp_res.txt");
		TeeOutputStream myOut = new TeeOutputStream(System.out, fos);
		PrintStream ps = new PrintStream(myOut);
		System.setOut(ps);

		// Builds the CFG and sets the target
		cfg = CFGBuilder.build(config.getTestFilename(),
				config.getTestFunction());
		
		int mcCabePaths = cfg.edgeSet().size() - cfg.vertexSet().size() + 1;
		System.out.println("Cyclomatic complexity: " + mcCabePaths);
		System.out.println("Number of branches: " + cfg.edgeSet().size());
		System.out.println("Number of nodes: " + cfg.vertexSet().size());
		
		CTypeHandler typeHandler = new CTypeHandler(cfg.getParameterTypes());
		CBridge.initialize(
				typeHandler.getValues().size(), 
				typeHandler.getPointers().size(),
				typeHandler.getPointers().size());

		for (int i = 0; i < config.getExperimentRuns(); i++) {
			runOnce(i);
		}
	}
	
	private static void runOnce(int pTime) throws Exception {
		String folderPath = "RESULTS/"+config.getTestFunction()+"/";
		File folder = new File(folderPath);
		folder.mkdirs();
		
		for (String generatorName : EXPERIMENT_GENERATORS) {
			System.out.println("RUNNING " + generatorName);
			TestSuiteGenerator generator = TestSuiteGeneratorHandler.getInstance(
					generatorName, config, cfg);
//			TestSuiteMinimizer minimizer = TestSuiteMinimizerHandler
//					.getInstance(config);
	
			BenchmarkCalculator<Integer> timeBenchmark = new TimeBenchmarkCalculator();
			BenchmarkCalculator<Double> coverageBenchmark = new BranchCoverageBenchmarkCalculator(cfg);
			BenchmarkCalculator<Integer> sizeBenchmark = new TestSuiteSizeBenchmarkCalculator(cfg);
	
			generator.addBenchmark(timeBenchmark);
			generator.addBenchmark(coverageBenchmark);
			generator.addBenchmark(sizeBenchmark);
	
			Set<TestCase> suite = generator.generateTestSuite();
			
			List<BenchmarkCalculator> benchmarks = new ArrayList<BenchmarkCalculator>();
			benchmarks.add(timeBenchmark);
			benchmarks.add(sizeBenchmark);
			benchmarks.add(coverageBenchmark);
			
			String[] parts = config.getTestFilename().split("[./]");
			String preFilename = parts[parts.length-2]
					+ "_" + config.getTestFunction() 
					+ "_" + generatorName
					+ "_" + pTime;
			
			exportCSV(folderPath + preFilename, benchmarks);
			
//			Set<TestCase> minimizedSuite = minimizer.minimize(suite);
	
			System.out
					.println("-------------------------------------------------------");
			System.out.println("Total test cases: " + suite.size());
//			System.out.println("Minimized test cases: " + minimizedSuite.size());
			System.out
					.println("-------------------------------------------------------");
			
			System.out.println("Minimised test suite:");
			for (TestCase tc : suite) {
				System.out.println(tc.getCoveredEdges());
				System.out.println(Utils.printParameters(tc.getParameters()));
				System.out.println("#########");
			}
		}
	}
	
	private static void exportCSV(String csvFilename, List<BenchmarkCalculator> benchmarks) throws IOException {
		if (benchmarks.size() == 0)
			return;
		
		List<String> labels = benchmarks.get(0).getLabels();
		List<Map<String, Object>> cumulativeResults = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		
		String csv = "";
		String csvCumulative = "";
		
		String[] row = new String[benchmarks.size()+1];
		String[] rowCumulative = new String[benchmarks.size()+1];
		
		row[0] = "labels";
		int i = 1;
		for (BenchmarkCalculator benchmark : benchmarks) {
			cumulativeResults.add(benchmark.getCumulativeResults());
			results.add(benchmark.getResults());
			row[i] = benchmark.getSignature();
			i++;
		}
		
		csv = StringUtils.join(row, ",") + "\n";
		csvCumulative = StringUtils.join(row, ",") + "\n";
		for (String label : labels) {
			row[0] = label;
			rowCumulative[0] = label;
			
			for (i = 0; i < results.size(); i++) {
				row[i+1] = results.get(i).get(label).toString();
				rowCumulative[i+1] = cumulativeResults.get(i).get(label).toString();
			}
			
			csv += StringUtils.join(row, ",") + "\n";
			csvCumulative += StringUtils.join(rowCumulative, ",") + "\n";
		}
		
		Utils.writeFile(csvFilename+".csv", csv);
		Utils.writeFile(csvFilename+"_cumulative.csv", csvCumulative);
	}
}
