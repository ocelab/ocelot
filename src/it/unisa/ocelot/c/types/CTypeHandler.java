package it.unisa.ocelot.c.types;

import java.util.ArrayList;
import java.util.List;

public class CTypeHandler {
	private CType[] types;
	
	private List<CType> pointers;
	private List<CType> values;
	

	public CTypeHandler(CType[] pTypes) {
		this.types = pTypes;
		this.pointers = new ArrayList<CType>();
		this.values = new ArrayList<CType>();
		this.generateInfo();
	}
	
	private void generateInfo() {
		for (CType type : this.types) {
			if (type.isPointer())
				this.pointers.add(type);
			else
				this.values.add(type);
		}
	}
	
	public List<CType> getPointers() {
		return pointers;
	}
	
	public List<CType> getValues() {
		return values;
	}
}
