package it.unisa.ocelot.conf;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.cfg.nodes.CFGNodeNavigator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

public class ConfigManager {
	private static final String STATIC_CONTENT = "---------STATIC+CONTENT";
	private static ConfigManager instance;
	private static String filename;
	private static String content;
	
	private String myFilename;
	
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
		content = null;
	}
	
	public static void setDirectContent(String pContent) {
		content = pContent;
		filename = STATIC_CONTENT + UUID.randomUUID();
	}
	
	/**
	 * Returns an instance of the configuration manager. Call <code>setFilename</code> before this method.
	 * @return Instance of the manager
	 * @throws IOException If there is an error opening the file
	 */
	public static ConfigManager getInstance() throws IOException {
		if (instance == null || !instance.myFilename.equals(filename)) {
			if (!filename.startsWith(STATIC_CONTENT))
				instance = new ConfigManager(filename);
			else {
				instance = new ConfigManager();
				instance.directLoadContent(filename, content);
			}
		}
		
		return instance;
	}
	
	private ConfigManager(String filename) throws IOException {
		this.properties = new Properties();
		this.properties.load(new FileInputStream(filename));
		
		this.myFilename = filename;
	}
	
	public ConfigManager() {
		this.myFilename = "";
	}
	
	public void directLoadContent(String pFilename, String pContent) throws IOException {
		this.properties = new Properties();
		this.properties.load(new StringReader(pContent));
		
		this.myFilename = pFilename;
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
	 * Returns true if the mutation probability has to be dynamic. False by default.
	 * @return
	 * @throws NumberFormatException if the parameter is not specified
	 */
	public boolean isDynamicMutation() {
		return this.properties.getProperty("mutation.dynamic", "false").equalsIgnoreCase("true");
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
	
	public int getRandomSizeLimit() {
		return Integer.parseInt(this.properties.getProperty("suite.generator.random.limit.tc", "-1"));
	}
	
	public int getRandomTimeLimit() {
		return Integer.parseInt(this.properties.getProperty("suite.generator.random.limit.time", "-1"));
	}
	
	public int getRandomGranularity() {
		return Integer.parseInt(this.properties.getProperty("suite.generator.random.granularity", "100"));
	}
	
	public double getRequiredCoverage() {
		return Double.parseDouble(this.properties.getProperty("suite.coverage", "1.0"));
	}

	public int getTestArraysSize() {
		return Integer.parseInt(this.properties.getProperty("test.arrays.size", "100"));
	}
	
	public String getAlgorithm() {
		return this.properties.getProperty("suite.generator.algorithm", "GeneticAlgorithm");
	}
	
	public double getAvmEpsilon() {
		return Double.parseDouble(this.properties.getProperty("avm.epsilon", "1"));
	}
	
	public double getAvmDelta() {
		return Double.parseDouble(this.properties.getProperty("avm.delta", "1"));
	}
	
	public boolean getSerendipitousCoverage() {
		return this.properties.getProperty("suite.generator.serendipitous", "false").equalsIgnoreCase("true");
	}
	
	public String getBudgetManager() {
		return this.properties.getProperty("suite.generator.budgetmanager", "Basic");
	}
	
	public List<String> getCascadeGenerators() {
		String cascade = this.properties.getProperty("suite.generator.cascade", "");
		String[] array = cascade.split("\\,");
		
		List<String> list = new ArrayList<>();
		for (String generator : array)
			list.add(generator);
		
		return list;
	}
	
	public boolean isMetaMutatorEnabled() {
		return this.properties.getProperty("operators.mutator.metamutator", "false").equalsIgnoreCase("true");
	}
	
	public void setProperty(String pProperty, String pValue) {
		this.properties.setProperty(pProperty, pValue);
	}
	
	public String[] getTestLink() {
		String link = this.properties.getProperty("test.link", "");
		if (link.equals(""))
			return new String[0];
		else
			return link.split("\\,");
	}
	
	public boolean isDMCSeed() {
		return this.properties.getProperty("suite.generator.dmc.seed", "false").equalsIgnoreCase("true");
	}

	public int getDMCSeedSize() {
		return Integer.parseInt(this.properties.getProperty("suite.generator.dmc.seed.size", "50"));
	}
	
	public String[] getExperimentGenerators() {
		String generators = this.properties.getProperty("experiment.generators", "");
		String[] array = generators.split("\\,");
		return array;
	}
	
	public boolean isExperimentMinimization() {
		return this.properties.getProperty("experiment.minimization", "false").equalsIgnoreCase("true");
	}
	

	public String getJavaHome() {
		return this.properties.getProperty("config.javaHome", "/usr/lib/jvm/java-8-openjdk");
	}
	
	public String getSystemInclude() {
		return this.properties.getProperty("config.systemInclude", "/usr/include");
	}
	
	public String[] getGlib2Paths() {
		String paths = this.properties.getProperty("config.glib2Paths", "");
		if (paths.equals(""))
			return new String[0];
		else
			return paths.split("\\,");
	}
	
	public String[] getJavaPaths() {
		String paths = this.properties.getProperty("config.javaPaths", "");
		if (paths.equals(""))
			return new String[0];
		else
			return paths.split("\\,");
	}
	
	public String getCFlags() {
		return this.properties.getProperty("config.cflags", "-shared -fpic");
	}
	
	public String getMakeCommand() {
		return this.properties.getProperty("config.make", "make");
	}
	
	public String getMoreOptions() {
		return this.properties.getProperty("config.options", "");
	}
	
}