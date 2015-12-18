package it.unisa.ocelot.c.types;

public class CDouble extends CType {
	public CDouble(boolean pPointer) {
		super(pPointer);
	}
	
	@Override
	public Object getInstance(Object pOriginalValue) {
		if (pOriginalValue instanceof Number)
			return new Double(((Number)pOriginalValue).doubleValue());
		
		return null;
	}
	
	@Override
	public double getMaxValue() {
		return Double.MAX_VALUE;
	}
	
	@Override
	public double getMinValue() {
		return Double.MIN_VALUE;
	}
	
	@Override
	public boolean isDiscrete() {
		return false;
	}
}
