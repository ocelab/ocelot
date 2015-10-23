package it.unisa.ocelot.genetic.algorithms;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AlgorithmStats {
	private Map<String, Object> stats;
	private String log;
	private int evaluations;
	
	public AlgorithmStats() {
		this.stats = new HashMap<String, Object>();
	}
	
	public void setStat(String pKey, Object pValue) {
		this.stats.put(pKey, pValue);
	}
	
	public Object getStat(String pKey) {
		return this.stats.get(pKey);
	}
	
	public void setEvaluations(int evaluations) {
		this.evaluations = evaluations;
	}
	
	public int getEvaluations() {
		return evaluations;
	}
	
	public void log(String pString) {
		this.log += "---------------------------------------------------\n";
		this.log += "Log entry " + new Date().toString()+"\n";
		this.log += pString + "\n";
	}
	
	public String getLog() {
		return log;
	}
}
