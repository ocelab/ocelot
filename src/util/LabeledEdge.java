package util;
import org.jgrapht.graph.DefaultEdge;


public class LabeledEdge extends DefaultEdge {
	private Object label;
	
	public LabeledEdge() {
		super();
	}
	
	public LabeledEdge(Object pLabel) {
		this();
		this.setLabel(pLabel);
	}
	
	
	public void setLabel(Object label) {
		this.label = label;
	}
	
	public Object getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return this.label.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LabeledEdge))
			return false;
		
		LabeledEdge edge = (LabeledEdge)obj;
		
		return edge.label.equals(this.label);
	}
}
