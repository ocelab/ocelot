package it.unisa.ocelot.cfg;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides information about a sub-graph. More precisely, it indicates which are the input nodes, the
 * output nodes, the nodes representing case statements, the "break"s and the "continue"s of a sub-graph.
 * @author simone
 *
 */
public class SubGraph {
	private List<CFGNode> input;
	private List<CFGNode> output;
	private List<CFGNode> cases;
	private List<CFGNode> breaks;
	private List<CFGNode> continues;
	
	/**
	 * Creates an empty sub-graph
	 */
	public SubGraph() {
		this.input = new ArrayList<CFGNode>();
		this.output = new ArrayList<CFGNode>();
		this.cases = new ArrayList<CFGNode>();
		this.breaks = new ArrayList<CFGNode>();
		this.continues = new ArrayList<CFGNode>();
	}
	
	/**
	 * Adds a CFGNode to the list of input nodes of the sub-graph.
	 * @param pNode Node to be added
	 */
	public void addInput(CFGNode pNode) {
		this.input.add(pNode);
	}
	
	/**
	 * Adds a list of CFGNode to the list of input nodes of the sub-graph.
	 * @param pNodes Nodes to be added
	 */
	public void addInput(List<CFGNode> pNodes) {
		this.input.addAll(pNodes);
	}
	
	/**
	 * Inherits all the input nodes from another sub-graph
	 * @param pNode Sub-graph from which inherit the nodes.
	 */
	public void inheritInput(SubGraph pGraph) {
		this.input.addAll(pGraph.getInput());
	}
	
	/**
	 * Adds a CFGNode to the list of output nodes of the sub-graph.
	 * @param pNode Node to be added
	 */
	public void addOutput(CFGNode pNode) {
		this.output.add(pNode);
	}
	
	/**
	 * Adds a list of CFGNode to the list of output nodes of the sub-graph.
	 * @param pNodes Nodes to be added
	 */
	public void addOutput(List<CFGNode> pNodes) {
		this.output.addAll(pNodes);
	}
	
	/**
	 * Inherits all the output nodes from another sub-graph
	 * @param pNode Sub-graph from which inherit the nodes.
	 */
	public void inheritOutput(SubGraph pGraph) {
		this.output.addAll(pGraph.getOutput());
	}
	
	/**
	 * Adds a CFGNode to the list of cases nodes of the sub-graph.
	 * @param pNode Node to be added
	 */
	public void addCase(CFGNode pNode) {
		this.cases.add(pNode);
	}
	
	/**
	 * Adds a list of CFGNode to the list of case nodes of the sub-graph.
	 * @param pNodes Nodes to be added
	 */
	public void addCase(List<CFGNode> pNodes) {
		this.cases.addAll(pNodes);
	}
	
	/**
	 * Inherits all the case nodes from another sub-graph
	 * @param pNode Sub-graph from which inherit the nodes.
	 */
	public void inheritCase(SubGraph pGraph) {
		this.cases.addAll(pGraph.getCases());
	}
	
	/**
	 * Adds a CFGNode to the list of break nodes of the sub-graph.
	 * @param pNode Node to be added
	 */
	public void addBreak(CFGNode pNode) {
		this.breaks.add(pNode);
	}
	
	/**
	 * Adds a list of CFGNode to the list of break nodes of the sub-graph.
	 * @param pNodes Nodes to be added
	 */
	public void addBreak(List<CFGNode> pNodes) {
		this.breaks.addAll(pNodes);
	}
	
	/**
	 * Inherits all the break nodes from another sub-graph
	 * @param pNode Sub-graph from which inherit the nodes.
	 */
	public void inheritBreak(SubGraph pGraph) {
		this.breaks.addAll(pGraph.getBreaks());
	}
	
	/**
	 * Adds a CFGNode to the list of continue nodes of the sub-graph.
	 * @param pNode Node to be added
	 */
	public void addContinue(CFGNode pNode) {
		this.continues.add(pNode);
	}
	
	/**
	 * Adds a list of CFGNode to the list of continue nodes of the sub-graph.
	 * @param pNodes Nodes to be added
	 */
	public void addContinue(List<CFGNode> pNodes) {
		this.continues.addAll(pNodes);
	}
	
	/**
	 * Inherits all the continue nodes from another sub-graph
	 * @param pNode Sub-graph from which inherit the nodes.
	 */
	public void inheritContinue(SubGraph pGraph) {
		this.continues.addAll(pGraph.getContinues());
	}
	
	/**
	 * Inherits all the break, continue and case nodes from another sub-graph
	 * @param pNode Sub-graph from which inherit the nodes.
	 */
	public void inheritOthers(SubGraph pGraph) {
		this.inheritContinue(pGraph);
		this.inheritBreak(pGraph);
		this.inheritCase(pGraph);
	}
	
	/**
	 * Returns the list of input nodes of the sub-graph
	 * @return
	 */
	public List<CFGNode> getInput() {
		return input;
	}
	
	/**
	 * Returns the list of output nodes of the sub-graph
	 * @return
	 */
	public List<CFGNode> getOutput() {
		return output;
	}
	
	/**
	 * Returns the list of case nodes of the sub-graph
	 * @return
	 */
	public List<CFGNode> getCases() {
		return cases;
	}
	
	/**
	 * Returns the list of break nodes of the sub-graph
	 * @return
	 */
	public List<CFGNode> getBreaks() {
		return breaks;
	}
	
	/**
	 * Returns the list of continue nodes of the sub-graph
	 * @return
	 */
	public List<CFGNode> getContinues() {
		return continues;
	}
}
