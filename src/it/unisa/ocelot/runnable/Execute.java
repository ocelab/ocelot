package it.unisa.ocelot.runnable;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.c.genetic.TargetCoverageExperiment;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import jmetal.experiments.Settings;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class Execute {
	private static final String OUTPUT_PATH = "./outputs/";
	private static final String EXP_PATH = "./experiments/";
	
	private static final String CONFIG_FILENAME = "config.properties";
	
	static {
		System.loadLibrary("Test");
	}
	
	public static void main(String[] args) throws Exception {
		ConfigManager.setFilename(CONFIG_FILENAME);
		ConfigManager config = ConfigManager.getInstance();
        
        
        //Loads all the configuration file information
        /*
        String testFilename = properties.getProperty("test.filename");
        String testFunction = properties.getProperty("test.function");
        String testParameters = properties.getProperty("test.parameters");
        String testTarget = properties.getProperty("test.target");
        String indipendentRuns = properties.getProperty("experiment.runs", "20");
        
        String experimentOutput = properties.getProperty("experiment.output.folder", OUTPUT_PATH);
        String experimentResults = properties.getProperty("experiment.results.folder", EXP_PATH);
        */
        
        //Sets up the output file
		File outputDirectory = new File(config.getOutputFolder());
        outputDirectory.mkdirs();
        FileOutputStream fos = new FileOutputStream(config.getOutputFolder() + "exp_res.txt");
        TeeOutputStream myOut = new TeeOutputStream(System.out, fos);
        PrintStream ps = new PrintStream(myOut);
        System.setOut(ps);
        
        //Builds the CFG and sets the target
        CFG cfg = buildCFG(config.getTestFilename(), config.getTestFunction());
        CFGNode target = config.getTestTarget(cfg);
        cfg.setTarget(target);
        
        //Sets the parameters types of the function
        Class<Object>[] parameterTypes = config.getTestParameters();
        
        TargetCoverageExperiment exp = new TargetCoverageExperiment(cfg, config, parameterTypes);

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
	
	public static Class<Object>[] getParameters(String pParameters) {
		String[] types = StringUtils.split(pParameters, ",");
        Class[] parameterTypes = new Class[types.length];
        for (int i = 0; i < parameterTypes.length; i++) {
        	if (types[i].equalsIgnoreCase("integer"))
        		parameterTypes[i] = Integer.class;
        	else
        		parameterTypes[i] = Double.class;
        }
        
        return parameterTypes;
	}
}
