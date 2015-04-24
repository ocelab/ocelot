package it.unisa.ocelot.runnable;

import it.unisa.ocelot.c.genetic.TargetCoverageExperiment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import jmetal.experiments.Settings;

import org.apache.commons.io.output.TeeOutputStream;

public class Execute {
	private static final String OUTPUT_PATH = "./outputs/";
	private static final String EXP_PATH = "./experiments/";
	
	private static final String SOURCE_FILENAME = "testobject/main.c";
	private static final String SOURCE_TESTFUNCTION = "gimp_rgb_to_hsv_int";
	private static final Class[] SOURCE_PARAMETERS = new Class[] {Double.class, Double.class, Double.class};
	
	static {
		System.loadLibrary("Test");
	}
	
	public static void main(String[] args) throws Exception {
		File outputDirectory = new File(OUTPUT_PATH);
        outputDirectory.mkdirs();
        FileOutputStream fos = new FileOutputStream(OUTPUT_PATH + "exp_res.txt");
        TeeOutputStream myOut = new TeeOutputStream(System.out, fos);
        PrintStream ps = new PrintStream(myOut);
        System.setOut(ps);
        
        TargetCoverageExperiment exp = new TargetCoverageExperiment();

        exp.experimentName_ = "TargetCoverage";
        exp.algorithmNameList_ = new String[]{"PGGA"};
        exp.problemList_ = new String[]{"TestCoverage"};

        exp.paretoFrontFile_ = new String[2];

        exp.indicatorList_ = new String[]{"HV", "SPREAD", "EPSILON"};

        int numberOfAlgorithms = exp.algorithmNameList_.length;

        exp.experimentBaseDirectory_ = EXP_PATH;

        exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

        exp.independentRuns_ = 20;
        
        exp.sourceFilename = SOURCE_FILENAME;
        exp.functionName = SOURCE_TESTFUNCTION;
        exp.parametersTypes = SOURCE_PARAMETERS;
        
        exp.initExperiment();
        exp.runExperiment(1);
	}
}
