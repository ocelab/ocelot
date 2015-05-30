package it.unisa.ocelot.genetic;

import jmetal.core.Variable;
import jmetal.encodings.variable.ArrayReal;

public class VariableTranslator {
	private Variable variable;
	
	public VariableTranslator(Variable pVariable) {
		this.variable = pVariable;
	}
	
	public Object translate() {
		throw new RuntimeException("Not yet implemented");
	}
	
	public Object[] translateArray(Class[] pTypes) {
		if (this.variable.getVariableType().equals(ArrayReal.class)) {
			ArrayReal array = (ArrayReal)this.variable;
			
			Object[] arguments = new Object[array.array_.length];
			for (int i = 0; i < array.array_.length; i++) {
				arguments[i] = this.getInstance(array.array_[i], pTypes[i]);
			}
			
			return arguments;
		}
		
		throw new RuntimeException("Variable of type " + this.variable.getVariableType().toString() + " not "
				+ "handled yet");
	}
	
	private Object getInstance(double pValue, Class<Object> pType) {
		if (pType.equals(Integer.class)) {
			return new Integer((int) pValue);
		} else if (pType.equals(Double.class)) {
			return new Double(pValue);
		}

		return new Double(pValue);
	}
}
