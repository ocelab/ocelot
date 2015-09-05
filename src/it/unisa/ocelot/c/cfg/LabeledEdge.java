package it.unisa.ocelot.c.cfg;
import it.unisa.ocelot.simulator.ExecutionEvent;

import org.jgrapht.graph.DefaultWeightedEdge;


/**
 * This class represents an edge with a label. The label could an object of any kind.
 * @author simone
 *
 */
public abstract class LabeledEdge extends DefaultWeightedEdge implements Comparable<LabeledEdge> {
	private static final long serialVersionUID = -1013876340016911304L;
	
	private Object label;
	private int id;
	private static int availableId;
	private int objectiveID;
	private boolean isCovered;
	
	static {
		availableId = 0;
	}
	
	private static int nextId() {
		return availableId++; //returns non incremented availableId!
	}
	
	/**
	 * Creates an edge with an empty label
	 */
	public LabeledEdge() {
		super();
		this.id = LabeledEdge.nextId();
		this.isCovered = false;
	}
	
	/**
	 * Creates an edge with a specified label
	 * @param pLabel Label
	 */
	public LabeledEdge(Object pLabel) {
		this();
		this.setLabel(pLabel);
	}
	
	/**
	 * Sets the label of the edge
	 * @param label
	 */
	public void setLabel(Object label) {
		this.label = label;
	}
	
	/**
	 * Returns the label of the edge
	 * @return
	 */
	public Object getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		if (this.label == null)
			return "";
		return this.label.toString();
	}
	
	/**
	 * Returns true if this branch matches the execution event
	 * @return
	 */
	public abstract boolean matchesExecution(ExecutionEvent pEvent);
	
	/**
	 * Returns true if this branch needs an event to be triggered in order to execute
	 * @return
	 */
	public abstract boolean needsEvent();
	
	/**@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LabeledEdge))
			return false;
		
		LabeledEdge edge = (LabeledEdge)obj;
		if (edge.label == this.label) {
				return true;
		} else {
			if (edge.label == null || this.label == null)
				return false;
			return edge.label.equals(this.label);
		}
	}
	**/
	
	@Override
	public int compareTo(LabeledEdge o) {
		return new Integer(this.id).compareTo(o.id);
	}
	
	/**
	 * Return the ID number of this LabeledEdge
	 * @return a progressive ID number
	 */
	public int getObjectiveID() {
		return objectiveID;
	}

	/**
	 * Set the ID number for this LabeledEdge
	 * @param objectiveID the id to set
	 */
	public void setObjectiveID(int objectiveID) {
		this.objectiveID = objectiveID;
	}
	
	
	public boolean isCovered() {
		return isCovered;
	}

	public void setCovered() {
		this.isCovered = true;
	}
	
	public void setUncovered() {
		this.isCovered = false;
	}
}
