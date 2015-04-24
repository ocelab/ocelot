package it.unisa.ocelot.c.genetic;


import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;

public class TargetCoverageExperiment extends Experiment {
	
	public String sourceFilename;
	public String functionName;
	public Class<Object>[] parametersTypes;
	
	@Override
	public void algorithmSettings(String problemName, int problemId,
			Algorithm[] algorithm) throws ClassNotFoundException {		
		try {
			TargetCoverageProblem problem = new TargetCoverageProblem(this.sourceFilename, this.functionName, this.parametersTypes);
			algorithm[0] = new TargetCoverageSettings(problem).configure();
		} catch (Exception e) {
			System.err.println("An error occurred while instantiating problem: " + e.getMessage());
			return;
		}
	}
	
	
}
