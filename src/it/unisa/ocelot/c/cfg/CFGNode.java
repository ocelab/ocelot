package it.unisa.ocelot.c.cfg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

/**
 * This class represents a node of the Control Flow Graph. It references the
 * corrinsponding syntax tree node.
 * 
 * @author simone
 *
 */
public class CFGNode implements Comparable<CFGNode> {
	private static int lastId;

	private int id;
	private List<IASTNode> nodes;
	private boolean isSwitch;

	/* */
	private boolean isVisited;

	private String name;

	public boolean isVisited() {
		return this.isVisited;
	}

	public void setVisited() {
		this.isVisited = true;
	}

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
		lastId = -1; // This is not an error! It starts from -1, it is
						// incremented the first time, so it will be 0.
	}

	/**
	 * Creates a node of the Control Flow Graph and assigns it an incremental
	 * identifier.
	 * 
	 * @param pNode
	 *            Node of the syntax tree from which this CFG node is generated
	 */
	public CFGNode(IASTNode pNode) {
		this.nodes = new ArrayList<IASTNode>();
		this.nodes.add(pNode);
		
		this.name = this.toString();

		this.id = CFGNode.getID();
	}

	/**
	 * Creates a node of the Control Flow Graph and assigns it an incremental
	 * identifier.
	 * 
	 * @param pNodes
	 *            List of nodes of the syntax tree from which this CFG node is
	 *            generated
	 */
	public CFGNode(List<IASTNode> pNodes) {
		this.nodes = pNodes;
		
		this.name = this.toString();

		this.id = CFGNode.getID();
	}

	/**
	 * Creates an empty node of the Control Flow Graph and assigns an
	 * incremental identifier to it. It requires a string as a unique
	 * identifier of the node.
	 */
	public CFGNode(String pName) {
		this.nodes = new ArrayList<IASTNode>();

		this.id = CFGNode.getID();
		this.name = pName;
	}

	/**
	 * Adds a node of the syntax tree to the CFG node.
	 * 
	 * @param pNode
	 *            node of the Abstract Syntax Tree to be added
	 */
	public void addASTNode(IASTNode pNode) {
		this.nodes.add(pNode);
	}

	/**
	 * Joins another CFGNode to this one.
	 * 
	 * @param pNode
	 *            CFGNode to be joined
	 */
	public void join(CFGNode pNode) {
		this.nodes.addAll(pNode.nodes);
	}

	/**
	 * Returns the AST-node of the list
	 * 
	 * @return
	 */
	public IASTNode getLeadingNode() {
		return this.nodes.get(0);
	}

	/**
	 * Returns all the AST-nodes of the list
	 * 
	 * @return
	 */
	public List<IASTNode> getNodes() {
		return this.nodes;
	}

	/**
	 * Returns the ID of the node
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	public boolean isContinue() {
		if (this.nodes.size() == 1
				&& this.nodes.get(0) instanceof IASTContinueStatement)
			return true;
		return false;
	}

	public boolean isBreak() {
		if (this.nodes.size() == 1
				&& this.nodes.get(0) instanceof IASTBreakStatement)
			return true;
		return false;
	}

	public boolean isGoto() {
		if (this.nodes.size() == 1
				&& this.nodes.get(0) instanceof IASTGotoStatement)
			return true;
		return false;
	}

	public boolean isCase() {
		if (this.nodes.size() > 0
				&& this.nodes.get(0) instanceof IASTCaseStatement)
			return true;
		return false;
	}

	public boolean isSwitch() {
		return isSwitch;
	}

	public void setSwitch(boolean isSwitch) {
		this.isSwitch = isSwitch;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CFGNode))
			return false;

		CFGNode node = (CFGNode) obj;
		if (this.nodes.size() > 0)
			return node.nodes.equals(this.nodes);
		else
			return node.name.equals(this.name);
	}

	public boolean strongEquals(CFGNode pNode) {
		return pNode.id == this.id;
	}

	public CFGNodeNavigator navigate(CFG cfg) {
		CFGNodeNavigator navigator = new CFGNodeNavigator(cfg, this);

		return navigator;
	}

	@Override
	public String toString() {
		String result = "" + this.id + ": ";
		if (this.nodes.size() > 0) {
			for (IASTNode node : this.nodes)
				if (node != null)
					result += node.getRawSignature() + "\n";
		} else {
			result += this.name;
		}
					
		return result;
	}

	@Override
	public int compareTo(CFGNode o) {
		return new Integer(this.id).compareTo(o.id);
	}
}
