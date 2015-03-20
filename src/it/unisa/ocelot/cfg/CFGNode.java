package it.unisa.ocelot.cfg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * This class represents a node of the Control Flow Graph. It references the corrinsponding syntax tree node. 
 * @author simone
 *
 */
public class CFGNode {
	private static int lastId;
	
	private int id;
	private List<IASTNode> nodes;
	
	static {
		lastId = -1;
	}
	
	private static int getID() {
		lastId++;
		return lastId;
	}
	
	/**
	 * Resets the incremental id counter to 0.
	 */
	public static void reset() {
		lastId = -1; //This is not an error! It starts from -1, it is incremented the first time, so it will be 0.
	}
	
	/**
	 * Creates a node of the Control Flow Graph and assigns it an incremental identifier.
	 * @param pNode Node of the syntax tree from which this CFG node is generated
	 */
	public CFGNode(IASTNode pNode) {
		this.nodes = new ArrayList<IASTNode>();
		this.nodes.add(pNode);
		
		this.id = CFGNode.getID();
	}
	
	/**
	 * Creates a node of the Control Flow Graph and assigns it an incremental identifier.
	 * @param pNodes List of nodes of the syntax tree from which this CFG node is generated
	 */
	public CFGNode(List<IASTNode> pNodes) {
		this.nodes = pNodes;
		
		this.id = CFGNode.getID();
	}
	
	/**
	 * Creates an empty node of the Control Flow Graph and assigns an incremental identifier to it.
	 */
	public CFGNode() {
		this.nodes = new ArrayList<IASTNode>();
		
		this.id = CFGNode.getID();
	}
	
	/**
	 * Adds a node of the syntax tree to the CFG node.
	 * @param pNode node of the Abstract Syntax Tree to be added
	 */
	public void addASTNode(IASTNode pNode) {
		this.nodes.add(pNode);
	}
	
	/**
	 * Joins another CFGNode to this one.
	 * @param pNode CFGNode to be joined
	 */
	public void addASTNode(CFGNode pNode) {
		this.nodes.addAll(pNode.nodes);
	}
	
	/**
	 * Returns the AST-node of the list
	 * @return
	 */
	public IASTNode getLeadingNode() {
		return this.nodes.get(0);
	}
	
	/**
	 * Returns all the AST-nodes of the list
	 * @return
	 */
	public List<IASTNode> getNodes() {
		return this.nodes;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CFGNode))
			return false;
		
		CFGNode node = (CFGNode)obj;
		
		return node.nodes.equals(this.nodes);
	}
	
	@Override
	public String toString() {
		String result = "" + this.id + ": ";
		for (IASTNode node : this.nodes)
			if (node != null)
				result += node.getRawSignature() + "\n";
		return result;
	}
}
