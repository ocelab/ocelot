package it.unisa.ocelot.c.instrumentor;

import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CStructure;
import org.eclipse.cdt.internal.core.dom.parser.c.CTypedef;

import java.util.ArrayList;
import java.util.List;

public class StructNode {
	public IType type;
	public String name;
	public List<StructNode> children;
	public StructNode parent;
	public boolean pointer;

	public StructNode(String pName, IType pType) {
		this.type = this.resolveType(pType);
		this.name = pName;
		this.build();
		this.pointer = false;
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
		return this.type instanceof CPointerType || this.pointer;
	}

	public String getCompleteNameWithPointers() {
		if (this.parent == null)
			return this.name;

		String name = this.parent.getCompleteName();
		if (this.parent.isPointer()) {
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
