package it.unisa.ocelot.runnable;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.nodes.TargetCoverageExperiment;
import it.unisa.ocelot.genetic.paths.PathCoverageExperiment;
import it.unisa.ocelot.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

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
        
        //Sets up the output file
		File outputDirectory = new File(config.getOutputFolder());
        outputDirectory.mkdirs();
        FileOutputStream fos = new FileOutputStream(config.getOutputFolder() + "exp_res.txt");
        TeeOutputStream myOut = new TeeOutputStream(System.out, fos);
        PrintStream ps = new PrintStream(myOut);
        System.setOut(ps);
        
        //Builds the CFG
        CFG cfg = buildCFG(config.getTestFilename(), config.getTestFunction());
        
        //Sets the parameters types of the function
        //Class<Object>[] parameterTypes = config.getTestParameters();
        Class<Object>[] parameterTypes = cfg.getParameterTypes();
        
        PathCoverageExperiment exp = new PathCoverageExperiment(cfg, config, parameterTypes);

        exp.experimentName_ = "TargetCoverage";
        exp.algorithmNameList_ = new String[]{"PGGA"};
        exp.problemList_ = new String[]{"TestCoverage"};

        exp.paretoFrontFile_ = new String[2];

        exp.indicatorList_ = new String[]{"HV", "SPREAD", "EPSILON"};

        int numberOfAlgorithms = exp.algorithmNameList_.length;

        exp.experimentBaseDirectory_ = config.getResultsFolder();

        exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

        exp.independentRuns_ = config.getExperimentRuns();
        
        exp.initExperiment();
        exp.runExperiment(1);
        
        if (config.getPrintResults()) {
        	String path = config.getResultsFolder() + "data/PGGA/TestCoverage/";
        	String fun = Utils.readFile(path + "FUN.0").trim();
        	String params = Utils.readFile(path + "VAR.0").trim();
        	
        	System.out.print("Fitness function: " + fun + ". ");
        	if (fun.equals("0.0"))
        		System.out.println("Target covered!");
        	else
        		System.out.println("Target not covered...");
        	System.out.println("Parameters found: " + params);
        }
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
}
