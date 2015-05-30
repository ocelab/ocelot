package it.unisa.ocelot.runnable;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.cfg.CFGWindow;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.nodes.NodeDistanceListener;
import it.unisa.ocelot.genetic.nodes.NodeCoverageExperiment;
import it.unisa.ocelot.genetic.paths.PathCoverageExperiment;
import it.unisa.ocelot.minimization.AdditionalGreedyMinimizer;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.CoverageCalculatorListener;
import it.unisa.ocelot.simulator.listeners.NodePrinterListener;
import it.unisa.ocelot.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Variable;
import jmetal.experiments.Settings;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class ExecuteWholeCoverage {
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
		FileOutputStream fos = new FileOutputStream(config.getOutputFolder()
				+ "exp_res.txt");
		TeeOutputStream myOut = new TeeOutputStream(System.out, fos);
		PrintStream ps = new PrintStream(myOut);
		System.setOut(ps);

		// Builds the CFG and sets the target
		CFG cfg = buildCFG(config.getTestFilename(), config.getTestFunction());
		
		if (config.getUI())
			showUI(cfg);
		
		CoverageCalculator calculator = new CoverageCalculator(cfg);
		List<Object[]> paramsList = new ArrayList<Object[]>();
		
		System.out.println("------------------------------------------------");
		System.out.println("PHASE 1 - McCabe								");
		System.out.println("------------------------------------------------");
		phase1(cfg, config, paramsList, calculator);
		
		calculator.calculateCoverage(paramsList);
		if (calculator.getBranchCoverage() < 1.0) {
			System.out.println("------------------------------------------------");
			System.out.println("PHASE 2 - Single targets						");
			System.out.println("------------------------------------------------");
			phase2(cfg, config, paramsList, calculator);
		}

		System.out.println("------------------------------------------------");
		System.out.println("PHASE 3 - Minimization (Additional greedy)		");
		System.out.println("------------------------------------------------");
		phase3(cfg, config, paramsList, calculator);
	}
	
	@SuppressWarnings("unchecked")
	public static void phase1(CFG cfg, ConfigManager config, List<Object[]> paramsList, CoverageCalculator calculator) 
			throws Exception {
		McCabeCalculator mcCabeCalculator = new McCabeCalculator(cfg);
		mcCabeCalculator.calculateMcCabePaths();
		ArrayList<ArrayList<LabeledEdge>> mcCabePaths = mcCabeCalculator
				.getMcCabeEdgePaths();

		for (ArrayList<LabeledEdge> aMcCabePath : mcCabePaths) {
			PathCoverageExperiment exp = new PathCoverageExperiment(cfg,
					config, cfg.getParameterTypes(), aMcCabePath);

			exp.initExperiment();
			//exp.runExperiment(1);
			exp.basicRun();

			if (config.getPrintResults()) {
				System.out.println(aMcCabePath.toString());
				
				double fitnessValue = exp.getFitnessValue();
				Variable[] params = exp.getVariables();
				VariableTranslator translator = new VariableTranslator(params[0]);
				
				Object[] numericParams = translator.translateArray(cfg.getParameterTypes());
				paramsList.add(numericParams);

				System.out.print("Fitness function: " + fitnessValue + ". ");
				if (fitnessValue == 0.0)
					System.out.println("Path covered!");
				else
					System.out.println("Path not covered...");
				System.out.println("Parameters found: " + Arrays.toString(numericParams));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void phase2(CFG cfg, ConfigManager config, List<Object[]> paramsList, CoverageCalculator calculator) 
			throws Exception {		
		for (LabeledEdge uncoveredEdge : calculator.getUncoveredEdges()) {
			CFGNode targetNode = cfg.getEdgeTarget(uncoveredEdge);
			NodeCoverageExperiment exp = new NodeCoverageExperiment(cfg, config, cfg.getParameterTypes(), targetNode);
			exp.initExperiment();
			exp.basicRun();
			
			if (config.getPrintResults()) {
				System.out.println(targetNode);
				
				double fitnessValue = exp.getFitnessValue();
				Variable[] params = exp.getVariables();
				VariableTranslator translator = new VariableTranslator(params[0]);
				
				System.out.print("Fitness function: " + fitnessValue + ". ");
				if (fitnessValue == 0.0) {
					System.out.println("Target covered!");
					Object[] numericParams = translator.translateArray(cfg.getParameterTypes());
					paramsList.add(numericParams);
					System.out.println("Parameters found: " + Arrays.toString(numericParams));
				} else {
					System.out.println("Target not covered... test case discarded");
				}
			}
		}
		
		calculator.calculateCoverage(paramsList);
	}
	
	public static void phase3(CFG cfg, ConfigManager config, List<Object[]> paramsList, CoverageCalculator calculator) 
			throws Exception {
		Set<TestCase> testSuite = new HashSet<TestCase>();
		int tcid = 0;
		for (Object[] params : paramsList) {
			calculator.calculateCoverage(params);
			
			TestCase testCase = new TestCase();
			testCase.setId(tcid);
			testCase.setParameters(params);
			testCase.setCoveredEdges(calculator.getCoveredEdges());
			testCase.setOracle(null);
			
			testSuite.add(testCase);
			tcid++;
		}
		
		AdditionalGreedyMinimizer minimizator = new AdditionalGreedyMinimizer();
		Set<TestCase> minimizedTestSuite = minimizator.minimize(testSuite);
		
		calculator.calculateCoverage(minimizedTestSuite);
		System.out.println("Total test cases: " + testSuite.size());
		System.out.println("Minimized test cases: " + minimizedTestSuite.size());
		System.out.println("Branch coverage: " + calculator.getBranchCoverage());
		System.out.println("Block coverage: " + calculator.getBlockCoverage());
		System.out.println("-------------------------------------------------------");
	}

	public static CFG buildCFG(String pSourceFile, String pFunctionName)
			throws Exception {
		String code = Utils.readFile(pSourceFile);
		CFG graph = new CFG();

		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(
				code.toCharArray(), pSourceFile);
		CFGVisitor visitor = new CFGVisitor(graph, pFunctionName);

		translationUnit.accept(visitor);

		return graph;
	}

	public static CFGNode getTarget(CFG pCfg, String pTarget) {
		CFGNodeNavigator navigator = pCfg.getStart().navigate(pCfg);

		String[] targets = StringUtils.split(pTarget, ",");
		for (String target : targets) {
			if (target.equalsIgnoreCase("flow"))
				navigator = navigator.goFlow();
			else if (target.equalsIgnoreCase("true"))
				navigator = navigator.goTrue();
			else if (target.equalsIgnoreCase("false"))
				navigator = navigator.goFalse();
			else
				navigator = navigator.goCase(target);
		}

		return navigator.node();
	}
	
	public static void showUI(CFG pCFG) {
		final CFGWindow window = new CFGWindow(pCFG);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				window.setVisible(true);
			}
		}).run();
	}
	
	/*private static Object[] getTestArguments(String pParametersString, Class[] pTypes) {
		String argsString = pParametersString;
		String[] args = StringUtils.split(argsString, " ");
		
		Object[] arguments = new Object[pTypes.length];
		
		for (int i = 0; i < pTypes.length; i++) {
			Double dValue = new Double(Double.parseDouble(args[i]));
			if (pTypes[i] == Integer.class) {
				arguments[i] = new Integer(dValue.intValue());
			} else if (pTypes[i] == Double.class) {
				arguments[i] = dValue;
			}
		}
		
		return arguments;
	}*/
}
