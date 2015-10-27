package it.unisa.ocelot.runnable;

import java.io.IOException;

import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.runnable.runners.ExecuteExperiment;
import it.unisa.ocelot.runnable.runners.ExecuteWholeCoverage;
import it.unisa.ocelot.runnable.runners.GenAndWrite;

public class Run {
	private static final String CONFIG_FILENAME = "config.properties";
	
	private static final int RUNNER_ILLEGAL = -1;
	private static final int RUNNER_SIMPLE_EXECUTE = 0;
	private static final int RUNNER_EXPERIMENT = 1;
	private static final int RUNNER_WRITE = 2;
	
	static {
		System.loadLibrary("Test");
	}
	
	private int runnerType;
	private String[] experimentGenerators;
	
	public static void main(String[] args) throws Exception {
		Run runner = new Run(args);
		runner.run();
	}
	
	public Run(String[] args) {
		this.runnerType = RUNNER_ILLEGAL;
		this.experimentGenerators = null;
		
		ConfigManager.setFilename(CONFIG_FILENAME);
		for (String arg : args) {
			interpret(arg);
		}
		
		if (this.runnerType == RUNNER_ILLEGAL) {
			throw new IllegalArgumentException("Please, specify the type of runner (simple, experiment or write)");
		}
	}
	
	public void run() throws Exception {
		switch (this.runnerType) {
		case RUNNER_SIMPLE_EXECUTE:
			System.out.println("Running simple coverage test");
			new ExecuteWholeCoverage().run();
			break;
		case RUNNER_EXPERIMENT:
			System.out.println("Running experiment");
			if (this.experimentGenerators == null)
				new ExecuteExperiment().run();
			else
				new ExecuteExperiment(this.experimentGenerators).run();
			break;
		case RUNNER_WRITE:
			System.out.println("Running coverage and writing");
			new GenAndWrite().run();
			break;
		}
	}
	
	public void interpret(String arg) {
		String[] parts = arg.split("\\=");
		if (parts.length != 2)
			throw new IllegalArgumentException("The passed parameter is not valid: " + arg);
		
		String property = parts[0];
		String value = parts[1];
		
		boolean changedProperty = false;
		
		if (property.equalsIgnoreCase("type")) {
			if (value.equalsIgnoreCase("simple")) {
				this.runnerType = RUNNER_SIMPLE_EXECUTE;
			} else if (value.equalsIgnoreCase("experiment")) {
				this.runnerType = RUNNER_EXPERIMENT;
			} else if (value.equalsIgnoreCase("write")) {
				this.runnerType = RUNNER_WRITE;
			} else
				throw new IllegalArgumentException("Illegal run type '" + value + "'. Use 'simple', 'experiment' or 'write'.");
		} else if (property.equalsIgnoreCase("config")) {
			if (changedProperty)
				throw new IllegalArgumentException("Illegal config position: set the configuration file before editing specific properties.");
			
			ConfigManager.setFilename(value);
		} else if (property.equalsIgnoreCase("experiment.generators")) {
			String[] generators = value.split("\\,");
			this.experimentGenerators = generators;
		} else {
			try {
				ConfigManager.getInstance().setProperty(property, value);
				changedProperty = true;
			} catch (IOException e) {
				throw new RuntimeException("Error: unable to open configuration file. " + e.getMessage());
			}
		}
	}
}
