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
		List<CType> tmpTypes = new ArrayList<>();
		for (CType type : this.types) {
			tmpTypes.add(type);
		}


		//Linearization of Struct
		for (int i = 0; i < tmpTypes.size(); i++) {
			if (tmpTypes.get(i) instanceof CStruct) {
				for (CType structType : ((CStruct) tmpTypes.get(i)).getStructVariables()) {
					tmpTypes.add(structType);
				}
				tmpTypes.remove(i--);
			}

			else if (tmpTypes.get(i) instanceof CPointer) {
				CType realType = getRealType(tmpTypes.get(i));
				if (realType instanceof CStruct) {
					if (((CStruct) realType).getStructVariables() != null) {
						for (CType structType : ((CStruct) realType).getStructVariables()) {
							tmpTypes.add(structType);
						}
					}

					tmpTypes.remove(i--);
				}
			}
		}


		for (CType type : tmpTypes) {
			if (type instanceof CPointer) {
				this.pointers.add(type);
			} else {
				this.values.add(type);
			}
		}
	}
	
	public List<CType> getPointers() {
		return pointers;
	}
	
	public List<CType> getValues() {
		return values;
	}

	private CType getRealType (CType type) {
		CType realType = type;
		while (realType instanceof CPointer) {
			realType = ((CPointer) realType).getType();
		}

		return realType;
	}
}
