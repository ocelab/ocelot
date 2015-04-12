package it.unisa.ocelot.c.instrumentor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
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


class StructNode {
	public IType type;
	public String name;
	public List<StructNode> children;
	public StructNode parent;
	
	public StructNode(String pName, IType pType) {
		this.type = this.resolveType(pType);
		this.name = pName;
		this.build();
	}
	
	private void build() {
		if (this.type instanceof CStructure) {
			CStructure struct = (CStructure)this.type;
			IField[] fields = struct.getFields();
			this.children = new ArrayList<StructNode>();
			
			for (IField field : fields) {
				StructNode node = new StructNode(field.getName(), field.getType());
				node.parent = this;
				this.children.add(node);
			}
		} else {
			this.children = null;
		}
	}
	
	private IType resolveType(IType pType) {
		while (pType instanceof CTypedef) {
			CTypedef tdef = (CTypedef)pType;
			
			pType = tdef.getType();
		}
		
		return pType;
	}
	
	public boolean isLeaf() {
		return this.children == null;
	}
	
	public boolean isPointer() {
		return this.type instanceof CPointerType;
	}
	
	public String getCompleteNameWithPointers() {
		if (this.parent == null)
			return this.name;
		
		String name = this.parent.getCompleteName();
		if (this.type instanceof CPointerType) {
			name += "->" + this.name;
		} else {
			name += "." + this.name;
		}
		
		return name;
	}
	
	public String getCompleteName() {
		if (this.parent == null)
			return this.name;
		
		String name = this.parent.getCompleteName();

		name += "." + this.name;
		
		return name;
	}
}