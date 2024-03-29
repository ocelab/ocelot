package it.unisa.ocelot.c.cfg.edges;

import it.unisa.ocelot.simulator.ExecutionEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an edge with a label. The label could an object of any kind.
 * @author simone
 *
 */
public class CaseEdge extends LabeledEdge {
	private static final long serialVersionUID = 5916153862577936206L;
	private int uniqueID;
	
	private static Map<String, Integer> index;
	
	static {
		index = new HashMap<String, Integer>();
	}
	
	private static int getUniqueID(String pLabel) {
		if (index.containsKey(pLabel)) {
			return index.get(pLabel);
		} else {
			int newID = index.size();
			index.put(pLabel, newID);
			
			return newID;
		}
	}
	
	public static int retrieveUniqueId(String pLabel) {
		return index.get(pLabel);
	}
	
	public CaseEdge(String pLabel) {
		super(pLabel);
		this.uniqueID = CaseEdge.getUniqueID(pLabel);
	}
	
	@Override
	public boolean matchesExecution(ExecutionEvent pEvent) {
		if (pEvent == null && this.isDefault())
			return true;
		if (pEvent == null)
			return false;
		
		return pEvent.choice == this.uniqueID;
	}

	public boolean isDefault() {
		return this.uniqueID == CaseEdge.getUniqueID("default");
	}

	@Override
	public boolean needsEvent() {
		return true;
	}
}
