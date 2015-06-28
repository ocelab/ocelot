package it.unisa.ocelot.conf;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;
import it.unisa.ocelot.c.cfg.LabeledEdge;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.antlr.v4.tool.ast.RangeAST;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

public class ConfigManager {
	private static ConfigManager instance;
	private static String filename;
	
	private Properties properties;
	
	static {
		setFilename("config.properties");
	}
	
	/**
	 * Sets the filename of the configuration file. To be called before <code>getInstance</code>.
	 * @return
	 */
	public static void setFilename(String pFilename) {
		filename = pFilename;
	}
	
	/**
	 * Returns an instance of the configuration manager. Call <code>setFilename</code> before this method.
	 * @return Instance of the manager
	 * @throws IOException If there is an error opening the file
	 */
	public static ConfigManager getInstance() throws IOException {
		if (instance == null)
			instance = new ConfigManager();
		
		return instance;
	}
	
	private ConfigManager() throws IOException {
		this.properties = new Properties();
		this.properties.load(new FileInputStream(filename));
	}
	
	/**
	 * Returns the population size for the genetic algorithm (100 if not specified)
	 * @return
	 */
	public int getPopulationSize() {
		return Integer.parseInt(this.properties.getProperty("population.size", "100"));
	}
	
	/**
	 * Returns the maximum number of evaluations for the genetic algorithm (2500 if not specified)
	 * @return
	 */
	public int getMaxEvaluations() {
		return Integer.parseInt(this.properties.getProperty("evaluations.max", "25000"));
	}
	
	/**
	 * Returns the probability of crossover for the genetic algorithm (0.9 if not specified)
	 * @return
	 */
	public double getCrossoverProbability() {
		return Double.parseDouble(this.properties.getProperty("crossover.probability", "0.9"));
	}
	
	/**
	 * Returns the probability of mutation for the genetic algorithm (-1 if not specified)
	 * @return
	 * @throws NumberFormatException if the parameter is not specified
	 */
	public double getMutationProbability() {
		return Double.parseDouble(this.properties.getProperty("mutation.probability", "ThrowError"));
	}
	
	/**
	 * Returns the number of threads for the genetic algorithm (1 if not specified)
	 * @return
	 */
	public int getThreads() {
		return Integer.parseInt(this.properties.getProperty("threads", "1"));
	}
	
	/**
	 * Returns the name of the name of the file which contains the function to be tested (required). 
	 * It uses property "test.basedir" as base directory.  
	 * @return
	 */
	public String getTestFilename() {
		String basedir = this.getTestBasedir();
		
		return basedir + "/" + this.properties.getProperty("test.filename");
	}
	
	/**
	 * Returns the name of the function to be tested (required)
	 * @return
	 */
	public String getTestFunction() {
		return this.properties.getProperty("test.function");
	}
	
	private CFGNodeNavigator getTestTargetNavigator(CFG pCfg) {
		String targetString = this.properties.getProperty("test.target", "");
		
		CFGNodeNavigator navigator = pCfg.getStart().navigate(pCfg);
		
		String[] targets = StringUtils.split(targetString, ",");
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
		
		return navigator;
	}
	
	/**
	 * Returns the target node specified in the configuration file.  
	 * Will not be used in future.
	 * @param pCfg Control Flow Graph
	 * @return Target node to be reached by the genetic algorithm
	 */
	@Deprecated
	public CFGNode getTestTarget(CFG pCfg) {
		return getTestTargetNavigator(pCfg).node();
	}
	
	/**
	 * Returns the target path specified in the configuration file.  
	 * Will not be used in future.
	 * @param pCfg Control Flow Graph
	 * @return Target path to be reached by the genetic algorithm
	 */
	@Deprecated
	public List<LabeledEdge> getTestTargetPath(CFG pCfg) {
		return getTestTargetNavigator(pCfg).path();
	}
	
	/**
	 * Return the types of the parameters of the function to be tested.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@Deprecated
	public Class[] getTestParameters() {
		String parametersString = this.properties.getProperty("test.parameters.types", "");
		
		String[] types = StringUtils.split(parametersString, ",");
        Class[] parameterTypes = new Class[types.length];
        for (int i = 0; i < parameterTypes.length; i++) {
        	if (types[i].startsWith("int"))
        		parameterTypes[i] = Integer.class;
        	else
        		parameterTypes[i] = Double.class;
        }
        
        return parameterTypes;
	}
	
	/**
	 * Returns the ranges for the parameters of the function to be tested. Each range should be in the form "N:M" and
	 * the ranges should be separated by a space " ".
	 * @return
	 */
	public Range<Double>[] getTestRanges() {
		String rangesString = this.properties.getProperty("test.parameters.ranges", "");
		if (rangesString == "")
			return null;
		
		String[] ranges = StringUtils.split(rangesString, " ");
		Range<Double>[] result = new Range[ranges.length];
		
		for (int i = 0; i < ranges.length; i++) {
			String rangeString = ranges[i];
			String[] rangeParts = StringUtils.split(rangeString, ":");
			double from = Double.parseDouble(rangeParts[0]);
			double to = Double.parseDouble(rangeParts[1]);
			result[i] = Range.between(from, to);
		}
		
        return result;
	}

	/**
	 * Returns the array of paths in which the include headers will be searched.
	 * @return
	 */
	public String[] getTestIncludePaths() {
		String includeStrings = this.properties.getProperty("test.includes", "");
		String[] includes = StringUtils.split(includeStrings, ",");
		String basedir = this.getTestBasedir();
		
		for (int i = 0; i < includes.length; i++) {
			if (!includes[i].startsWith("/"))
				includes[i] = basedir  + includes[i];
		}
		
		return includes;
	}
	
	/**
	 * Returns the output folder.
	 * @return
	 */
	public String getOutputFolder() {
		return this.properties.getProperty("experiment.output.folder");
	}
	
	/**
	 * Returns the list of test arguments for a simple test
	 * @return
	 */
	public Object[] getTestArguments() {
		String argsString = this.properties.getProperty("test.simple");
		String[] args = StringUtils.split(argsString, " ");
		
		Class[] types = this.getTestParameters();
		Object[] arguments = new Object[types.length];
		
		for (int i = 0; i < types.length; i++) {
			Double dValue = new Double(Double.parseDouble(args[i]));
			if (types[i] == Integer.class) {
				arguments[i] = new Integer(dValue.intValue());
			} else if (types[i] == Double.class) {
				arguments[i] = dValue;
			}
		}
		
		return arguments;
	}
	
	/**
	 * Returns if configuration file allows printing the solution on the standard output
	 * @return
	 */
	public boolean getPrintResults() {
		String print = this.properties.getProperty("experiment.results.print", "false");
		if (print.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Returns the folder that will contain the results of the genetic algorithm calculations
	 * @return
	 */
	public String getResultsFolder() {
		return this.properties.getProperty("experiment.results.folder");
	}
	
	/**
	 * Returns the number of indipendent runs of the experiment (genetic algorithm) (1 if not specified).
	 * @return
	 */
	public int getExperimentRuns() {
		return Integer.parseInt(this.properties.getProperty("experiment.runs", "1"));
	}
	
	/**
	 * Returns the base directory from which all the filenames in the config file will start.
	 * @return
	 */
	public String getTestBasedir() {
		return this.properties.getProperty("test.basedir", "./");
	}

	/**
	 * Returns <code>true</code> if debug is active, false otherwise.
	 * @return
	 */
	public boolean getDebug() {
		return this.properties.getProperty("test.debug", "false").equalsIgnoreCase("true");
	}
	
	public boolean getUI() {
		return this.properties.getProperty("test.ui", "false").equalsIgnoreCase("true");
	}
	
	public String getTestSuiteGenerator() {
		return this.properties.getProperty("suite.generator");
	}
	
	public String getTestSuiteMinimizer() {
		return this.properties.getProperty("suite.minimizer");
	}
	
	public int getReducedMcCabeCoverageTimes() {
		return Integer.parseInt(this.properties.getProperty("suite.generator.rmc.times", "1"));
	}
}