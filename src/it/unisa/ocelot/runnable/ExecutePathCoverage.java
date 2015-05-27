package it.unisa.ocelot.runnable;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.McCabeCalculator;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.nodes.NodeDistanceListener;
import it.unisa.ocelot.genetic.paths.PathCoverageExperiment;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.CoverageSimulatorListener;
import it.unisa.ocelot.simulator.listeners.TestSimulatorListener;
import it.unisa.ocelot.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import jmetal.experiments.Settings;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class ExecutePathCoverage {
	private static final String CONFIG_FILENAME = "config.properties";

	static {
		System.loadLibrary("Test");
	}

	@SuppressWarnings("unchecked")
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

		McCabeCalculator mcCabeCalculator = new McCabeCalculator(cfg);
		mcCabeCalculator.calculateMcCabePaths();
		ArrayList<ArrayList<LabeledEdge>> mcCabePaths = mcCabeCalculator
				.getMcCabeEdgePaths();

		List<Object[]> paramsList = new ArrayList<Object[]>();
		for (ArrayList<LabeledEdge> aMcCabePath : mcCabePaths) {
			PathCoverageExperiment exp = new PathCoverageExperiment(cfg,
					config, cfg.getParameterTypes(), aMcCabePath);

			exp.experimentName_ = "TargetCoverage";
			exp.algorithmNameList_ = new String[] { "PGGA" };
			exp.problemList_ = new String[] { "TestCoverage" };

			exp.paretoFrontFile_ = new String[2];

			exp.indicatorList_ = new String[] { "HV", "SPREAD", "EPSILON" };

			int numberOfAlgorithms = exp.algorithmNameList_.length;

			exp.experimentBaseDirectory_ = config.getResultsFolder();

			exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

			exp.independentRuns_ = config.getExperimentRuns();

			exp.initExperiment();
			exp.runExperiment(1);

			if (config.getPrintResults()) {
				System.out.println(aMcCabePath.toString());
				String path = config.getResultsFolder()
						+ "data/PGGA/TestCoverage/";
				String fun = Utils.readFile(path + "FUN.0").trim();
				String params = Utils.readFile(path + "VAR.0").trim();
				
				paramsList.add(getTestArguments(params, cfg.getParameterTypes()));

				System.out.print("Fitness function: " + fun + ". ");
				if (fun.equals("0.0"))
					System.out.println("Path covered!");
				else
					System.out.println("Path not covered...");
				System.out.println("Parameters found: " + params);
			}
		}
		
		CoverageSimulatorListener coverageListener = new CoverageSimulatorListener(cfg);
		
		for (Object[] params : paramsList) {
			CBridge bridge = new CBridge();
			EventsHandler h = new EventsHandler();
			
			bridge.getEvents(h, params);
			
			Simulator simulator = new Simulator(cfg, h.getEvents());
			
			simulator.addListener(coverageListener);
			simulator.simulate();
			
			if (!simulator.isSimulationCorrect())
				System.out.println("Simulation error!");
		}
		
		System.out.println("Branch coverage: " + coverageListener.getBranchCoverage());
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
	
	private static Object[] getTestArguments(String pParametersString, Class[] pTypes) {
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
	}
}
