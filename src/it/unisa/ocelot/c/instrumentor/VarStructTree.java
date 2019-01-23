package it.unisa.ocelot.c.instrumentor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CStructure;
import org.eclipse.cdt.internal.core.dom.parser.c.CTypedef;

public class VarStructTree {
	private StructNode root;
	public VarStructTree(String pVarName, CStructure pStructure) {
		this.root = new StructNode(pVarName, pStructure);
		this.root.parent = null;
	}
	
	public StructNode getRoot() {
		return root;
	}
	
	public List<StructNode> getBasicVariables() {
		Queue<StructNode> queue = new LinkedList<>();
		List<StructNode> leaves = new ArrayList<StructNode>();
		
		queue.add(this.root);
		
		while (!queue.isEmpty()) {
			StructNode node = queue.poll();
			
			if (!node.isLeaf())
				queue.addAll(node.children);
			else
				leaves.add(node);
		}
		
		return leaves;
	}
}


