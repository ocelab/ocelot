package it.unisa.ocelot.c.types;

public abstract class CType {
	protected boolean pointer;
	
	protected CType(boolean pPointer) {
		this.pointer = pPointer;
	}
	
	public boolean isPointer() {
		return pointer;
	}
	
	public abstract Object getInstance(Object pOriginalValue);
	public abstract double getMaxValue();
	public abstract double getMinValue();
	
	
}
