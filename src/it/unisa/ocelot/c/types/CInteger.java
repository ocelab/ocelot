package it.unisa.ocelot.c.types;

public class CInteger extends CType {
	public CInteger(boolean pPointer) {
		super(pPointer);
	}
	
	@Override
	public Object getInstance(Object pOriginalValue) {
		if (pOriginalValue instanceof Number)
			return new Integer(((Number)pOriginalValue).intValue());
		
		return null;
	}
	
	@Override
	public double getMaxValue() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public double getMinValue() {
		return Integer.MIN_VALUE;
	}
}
