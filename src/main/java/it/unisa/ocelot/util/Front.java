package it.unisa.ocelot.util;

import java.util.ArrayList;
import java.util.List;

import jmetal.core.SolutionSet;

/**
 * Represent a non-dominated ranking assignment of front F
 * allFronts at position 0 represent the proposed preference criterion
 * 
 * @author giograno
 *
 */
public class Front {
	
	private List<SolutionSet> allFronts;
	
	public Front() {
		this.allFronts = new ArrayList<SolutionSet>();
	}
	
	public SolutionSet getFront(int index){
		return this.allFronts.get(index);
	}
	
	public void addFront(SolutionSet front){
		this.allFronts.add(front);
	}
	
}
