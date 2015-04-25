package it.unisa.ocelot.conf;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGNodeNavigator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class ConfigManager {
	private static ConfigManager instance;
	private static String filename;
	
	private Properties properties;
	
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
		return this.properties.getProperty("test.filename");
	}
	
	public String getTestFunction() {
		return this.properties.getProperty("test.function");
	}
	
	public CFGNode getTestTarget(CFG pCfg) {
		String targetString = this.properties.getProperty("test.target");
		
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
		
		return navigator.node();
	}
	
	public Class[] getTestParameters() {
		String parametersString = this.properties.getProperty("test.parameters");
		
		String[] types = StringUtils.split(parametersString, ",");
        Class[] parameterTypes = new Class[types.length];
        for (int i = 0; i < parameterTypes.length; i++) {
        	if (types[i].equalsIgnoreCase("integer"))
        		parameterTypes[i] = Integer.class;
        	else
        		parameterTypes[i] = Double.class;
        }
        
        return parameterTypes;
	}
	
	public String getOutputFolder() {
		return this.properties.getProperty("experiment.output.folder");
	}
	
	public String getResultsFolder() {
		return this.properties.getProperty("experiment.results.folder");
	}
	
	public int getExperimentRuns() {
		return Integer.parseInt(this.properties.getProperty("experiment.runs", "1"));
	}
}