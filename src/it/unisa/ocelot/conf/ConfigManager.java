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
	
	public static void setFilename(String pFilename) {
		filename = pFilename;
	}
	
	public static ConfigManager getInstance() throws IOException {
		if (instance == null)
			instance = new ConfigManager();
		
		return instance;
	}
	
	public ConfigManager() throws IOException {
		this.properties = new Properties();
		this.properties.load(new FileInputStream(filename));
	}
	
	public int getPopulationSize() {
		return Integer.parseInt(this.properties.getProperty("population.size", "100"));
	}
	
	public int getMaxEvolutions() {
		return Integer.parseInt(this.properties.getProperty("evolutions.max", "25000"));
	}
	
	public double getCrossoverProbability() {
		return Double.parseDouble(this.properties.getProperty("crossover.probability", "0.9"));
	}
	
	public int getThreads() {
		return Integer.parseInt(this.properties.getProperty("threads", "1"));
	}
	
	public String getTestFilename() {
		String basedir = this.getTestBasedir();
		
		return basedir + "/" + this.properties.getProperty("test.filename");
	}
	
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
	
	public CFGNode getTestTarget(CFG pCfg) {
		return getTestTargetNavigator(pCfg).node();
	}
	
	public List<LabeledEdge> getTestTargetPath(CFG pCfg) {
		return getTestTargetNavigator(pCfg).path();
	}
	
	@SuppressWarnings("rawtypes")
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
	
	public String getOutputFolder() {
		return this.properties.getProperty("experiment.output.folder");
	}
	
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
	
	public boolean getPrintResults() {
		String print = this.properties.getProperty("experiment.results.print", "false");
		if (print.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}
	
	public String getResultsFolder() {
		return this.properties.getProperty("experiment.results.folder");
	}
	
	public int getExperimentRuns() {
		return Integer.parseInt(this.properties.getProperty("experiment.runs", "1"));
	}
	
	public String getTestBasedir() {
		return this.properties.getProperty("test.basedir", "./");
	}
}