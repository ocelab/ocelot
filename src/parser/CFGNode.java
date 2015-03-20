package parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CFGNode {
	private List<IASTNode> nodes;
	
	public CFGNode(IASTNode pNode) {
		this.nodes = new ArrayList<IASTNode>();
		this.nodes.add(pNode);
	}
	
	public CFGNode(List<IASTNode> pNodes) {
		this.nodes = pNodes;
	}
	
	public CFGNode() {
		this.nodes = new ArrayList<IASTNode>();
	}
	
	public void addASTNode(IASTNode pNode) {
		this.nodes.add(pNode);
	}
	
	public void addASTNode(CFGNode pNode) {
		this.nodes.addAll(pNode.nodes);
	}
	
	public IASTNode getLeadingNode() {
		return this.nodes.get(0);
	}
	
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
		String result = "";
		for (IASTNode node : this.nodes)
			if (node != null)
				result += node.getRawSignature() + "\n";
		return result;
	}
}
