package it.unisa.ocelot.c.cfg;

import org.jgrapht.graph.ListenableDirectedGraph;

/**
 * Control Flow Graph, a graph which nodes are CFGNode and which edges are LabeledEdge, so edges
 * that has an object as a label to identify the condition in which the the control flows from a
 * vertex to another one. For instance, a condition will have two edges: an edge labeled true and
 * an edge labeled false.
 * @see LabeledEdge
 * @author simone
 *
 */
public class CFG extends ListenableDirectedGraph<CFGNode, LabeledEdge> {
	private static final long serialVersionUID = 6995672951065896769L;
	
	private CFGNode start;
	private CFGNode end;
	private CFGNode target;
	@SuppressWarnings("rawtypes")
	private Class[] parameterTypes;
	
	/**
	 * Creates an empty CFG
	 */
	public CFG() {
		super(LabeledEdge.class);
	}
	
	/**
	 * Sets the starting node
	 * @param pStartNode
	 */
	public void setStart(CFGNode pStartNode) {
		this.start = pStartNode;
	}
	
	/**
	 * Sets the ending node
	 * @param pEndNode
	 */
	public void setEnd(CFGNode pEndNode) {
		this.end = pEndNode;
	}
	
	/**
	 * Returns the starting node
	 * @return
	 */
	public CFGNode getStart() {
		return start;
	}
	
	/**
	 * Returns the ending node
	 * @return
	 */
	public CFGNode getEnd() {
		return end;
	}
	
	public void setTarget(CFGNode target) {
		this.target = target;
	}
	
	public CFGNode getTarget() {
		return target;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	@SuppressWarnings("rawtypes")
	public void setParameterTypes(Class[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	
	
}
