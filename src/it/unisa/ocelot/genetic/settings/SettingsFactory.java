package it.unisa.ocelot.genetic.settings;

import jmetal.core.Problem;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.StandardSettings;

public class SettingsFactory {
	public static final String GA = "GeneticAlgorithm";
	public static final String AVM = "AVM";
	public static final String MEMETIC = "MemeticAlgorithm";
	
	public static StandardSettings getSettings(String pSettingsName, Problem pProblem, ConfigManager pConfig) {
		if (pSettingsName.equalsIgnoreCase(GA)) {
			return new GASettings(pProblem, pConfig);
		//} //else if (pSettingsName.equalsIgnoreCase(AVM)) {
			//return new AVMSettings(pProblem, pConfig);
		} else if (pSettingsName.equalsIgnoreCase(MEMETIC)){
			return new MemeticSettings(pProblem, pConfig);
		} else 
			throw new RuntimeException("Wrong algorithm name: \"" + pSettingsName + "\"");
	}
}
