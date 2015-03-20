package parser;

import java.util.ArrayList;
import java.util.List;

public class SubGraph {
	private List<CFGNode> input;
	private List<CFGNode> output;
	private List<CFGNode> cases;
	private List<CFGNode> breaks;
	private List<CFGNode> continues;
	
	public SubGraph() {
		this.input = new ArrayList<CFGNode>();
		this.output = new ArrayList<CFGNode>();
		this.cases = new ArrayList<CFGNode>();
		this.breaks = new ArrayList<CFGNode>();
		this.continues = new ArrayList<CFGNode>();
	}
	
	public void addInput(CFGNode pNode) {
		this.input.add(pNode);
	}
	
	public void addInput(List<CFGNode> pNodes) {
		this.input.addAll(pNodes);
	}
	
	public void inheritInput(SubGraph pGraph) {
		this.input.addAll(pGraph.getInput());
	}
	
	public void addOutput(CFGNode pNode) {
		this.output.add(pNode);
	}
	
	public void addOutput(List<CFGNode> pNodes) {
		this.output.addAll(pNodes);
	}
	
	public void inheritOutput(SubGraph pGraph) {
		this.output.addAll(pGraph.getOutput());
	}
	
	public void addCase(CFGNode pNode) {
		this.cases.add(pNode);
	}
	
	public void addCase(List<CFGNode> pNodes) {
		this.cases.addAll(pNodes);
	}
	
	public void inheritCase(SubGraph pGraph) {
		this.cases.addAll(pGraph.getCases());
	}
	
	public void addBreak(CFGNode pNode) {
		this.breaks.add(pNode);
	}
	
	public void addBreak(List<CFGNode> pNodes) {
		this.breaks.addAll(pNodes);
	}
	
	public void inheritBreak(SubGraph pGraph) {
		this.breaks.addAll(pGraph.getBreaks());
	}
	
	public void addContinue(CFGNode pNode) {
		this.continues.add(pNode);
	}
	
	public void addContinue(List<CFGNode> pNodes) {
		this.continues.addAll(pNodes);
	}
	
	public void inheritContinue(SubGraph pGraph) {
		this.continues.addAll(pGraph.getContinues());
	}
	
	public void inheritOthers(SubGraph pGraph) {
		this.inheritContinue(pGraph);
		this.inheritBreak(pGraph);
		this.inheritCase(pGraph);
	}
	
	public List<CFGNode> getInput() {
		return input;
	}
	
	public List<CFGNode> getOutput() {
		return output;
	}
	
	public List<CFGNode> getCases() {
		return cases;
	}
	
	public List<CFGNode> getBreaks() {
		return breaks;
	}
	
	public List<CFGNode> getContinues() {
		return continues;
	}
}
