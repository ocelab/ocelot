package it.unisa.ocelot.runnable.runners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGBuilder;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.cfg.nodes.CFGNodeNavigator;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.nodes.NodeCoverageExperiment;
import it.unisa.ocelot.util.Utils;
import jmetal.experiments.Settings;

@Deprecated
public class Execute {
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
		CFG cfg = CFGBuilder.build(config.getTestFilename(), config.getTestFunction());
		CFGNode target = config.getTestTarget(cfg);

		NodeCoverageExperiment exp = new NodeCoverageExperiment(cfg,
				config, cfg.getParameterTypes(), target);

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
